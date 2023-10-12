package com.codemantri.shieldme.ui.addcontact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codemantri.shieldme.data.ContactDAO
import com.codemantri.shieldme.data.ContactEntity
import com.codemantri.shieldme.databinding.BottomSheetAddContactBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddContactBottomSheet(private val updateContact: ContactEntity? = null): BottomSheetDialogFragment() {
    @Inject
    lateinit var contactDao: ContactDAO
    private lateinit var binding: BottomSheetAddContactBinding
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetAddContactBinding.inflate(layoutInflater, container, false)

        if (updateContact != null) {
            binding.etName.setText(updateContact.name)
            binding.etMobileNumber.setText(updateContact.number)
            binding.toolBar.title = "Update Contact"
        } else
            binding.toolBar.title = "Add Contact"

        binding.btnSaveContact.setOnClickListener {
            val name = binding.etName.text.toString()
            val number = binding.etMobileNumber.text.toString()

            if (name.isEmpty()){
                binding.etName.error = "Name is mandatory"
                return@setOnClickListener
            }
            if (number.isEmpty()){
                binding.etMobileNumber.error = "Number is mandatory"
                return@setOnClickListener
            }
            if (number.length != 10){
                binding.etMobileNumber.error = "Invalid number"
                return@setOnClickListener
            }

            if (updateContact != null){
                updateContact.name = name
                updateContact.number = number
                contactDao.updateContact(updateContact)
                Snackbar.make(binding.root, "Updated successfully", Snackbar.LENGTH_SHORT).show()
            } else{
                val contact = ContactEntity(name = name, number = number)
                contactDao.insertContact(contact)
                Snackbar.make(binding.root, "Added successfully", Snackbar.LENGTH_SHORT).show()
            }

            dismiss()
        }
        binding.ibCloseBottomSheet.setOnClickListener { dismiss() }
        return binding.root
    }

}