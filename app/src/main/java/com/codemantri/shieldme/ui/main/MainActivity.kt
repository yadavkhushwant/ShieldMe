package com.codemantri.shieldme.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import com.codemantri.shieldme.R
import com.codemantri.shieldme.data.ContactDAO
import com.codemantri.shieldme.data.ContactEntity
import com.codemantri.shieldme.databinding.ActivityMainBinding
import com.codemantri.shieldme.ui.addcontact.AddContactBottomSheet
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var contactDao: ContactDAO
    private lateinit var adapter: ContactsAdapter
    private lateinit var binding: ActivityMainBinding
    private val permissionContract =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ContactsAdapter(::showOptionsMenu)
        binding.rvContacts.adapter = adapter

        permissionContract.launch(
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        binding.cvSendNotification.setOnClickListener {
            sendEmergencyMessage()
        }

        binding.fabAddNew.setOnClickListener {
            val bottomSheet = AddContactBottomSheet()
            bottomSheet.show(supportFragmentManager, "MainActivity")
        }

        contactDao.getAllContacts().asLiveData(Dispatchers.Default).observe(this@MainActivity) {
            if (it.isEmpty()) binding.tvContactsLabel.text = buildString {append("No Contact found") }

            adapter.submitList(it)
        }

        if (intent.getBooleanExtra("launchedFromWidget", false))
            sendEmergencyMessage()

    }

    private fun showOptionsMenu(view: View, contact: ContactEntity) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.contact_options, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    deleteContact(contact)
                    return@setOnMenuItemClickListener true
                }

                R.id.edit -> {
                    val bottomSheet = AddContactBottomSheet(contact)
                    bottomSheet.show(supportFragmentManager, "MainActivity")
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
        }
        popup.show()
    }

    private fun deleteContact(contact: ContactEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Contact")
            .setMessage(contact.name)
            .setPositiveButton("OK") { dialog, _ ->
                contactDao.deleteContact(contact)
                dialog.dismiss()
                Snackbar.make(binding.root, "Deleted successfully", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendEmergencyMessage() {
        val hasPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionContract.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            return
        }
        val contacts = contactDao.getContactNumbers()
        if (contacts.isEmpty()){
            Snackbar.make(binding.root, "No emergency contact found", Snackbar.LENGTH_SHORT).show()
            return
        }
        binding.cvSendNotification.isEnabled = false
        binding.ivCardIcon.isVisible = false
        binding.progressbar.isVisible = true
        binding.tvCardMessage.text = buildString { append("Acquiring your location...") }

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    binding.tvCardMessage.text = buildString { append("Sending SMS...") }
                    sendSMS(location.latitude, location.longitude, contacts)
                    Snackbar.make(binding.root,"Your location has been sent to your contacts",Snackbar.LENGTH_SHORT).show()
                } else
                    Snackbar.make(binding.root,"Failed to get your location",Snackbar.LENGTH_SHORT).show()

                binding.cvSendNotification.isEnabled = true
                binding.ivCardIcon.isVisible = true
                binding.progressbar.visibility = View.INVISIBLE
                binding.tvCardMessage.text = resources.getString(R.string.send_emergency_message)
            }
            .addOnFailureListener {
                binding.cvSendNotification.isEnabled = true
                binding.ivCardIcon.isVisible = true
                binding.progressbar.visibility = View.INVISIBLE
                binding.tvCardMessage.text = resources.getString(R.string.send_emergency_message)
                Snackbar.make(binding.root, "Failed to get your location", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun sendSMS(latitude: Double, longitude: Double, contacts: List<String>) {
        val smsManager: SmsManager = getSystemService(SmsManager::class.java)
        val link = "https://maps.google.com/?q=$latitude,$longitude"
        val message = "Hi,\n" +
                "I am in an emergency situation.\n" +
                "Please check my current location: $link"

        contacts.forEach { contact ->
            try {
                smsManager.sendTextMessage(contact, null, message, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}