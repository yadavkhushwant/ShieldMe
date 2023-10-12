package com.codemantri.shieldme.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codemantri.shieldme.R
import com.codemantri.shieldme.data.ContactEntity
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import java.util.Random

class ContactsAdapter(
    private val onShowMenuCallBack: (View, ContactEntity) -> Unit,
): RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {
    private val contactsList = ArrayList<ContactEntity>()
    private val random = Random()
    private val colorArray = arrayOf(
        R.color.midnight_blue,
        R.color.teal,
        R.color.indigo,
        R.color.steel_gray,
        R.color.forest_green,
        R.color.royal_purple,
        R.color.slate_gray,
        R.color.deep_red,
        R.color.dark_cyan,
        R.color.navy_blue,
        R.color.olive_green,
        R.color.plum
    )

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val cvFirstLetter: CircularRevealCardView = itemView.findViewById(R.id.cv_icon)
        val tvFirstLetter: TextView = itemView.findViewById(R.id.tv_first_letter)
        val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tvContactNumber: TextView = itemView.findViewById(R.id.tv_contact_number)
        val cvCall: CircularRevealCardView = itemView.findViewById(R.id.cv_call)
        val btnMoreOptions: AppCompatImageButton = itemView.findViewById(R.id.btn_more_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
    )

    override fun getItemCount() = contactsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index = random.nextInt(colorArray.size)
        holder.cvFirstLetter.setCardBackgroundColor(ContextCompat.getColor(holder.cvFirstLetter.context, colorArray[index]))
        val current = contactsList[position]
        holder.tvFirstLetter.text = current.name[0].toString()
        holder.tvContactName.text =current.name
        holder.tvContactNumber.text = current.number

        holder.cvCall.setOnClickListener {
            makePhoneCall(current.number, it.context)
        }

        holder.btnMoreOptions.setOnClickListener { onShowMenuCallBack(it, current) }
    }

    private fun makePhoneCall(phoneNumber: String, context: Context) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(callIntent)
        }else{
            Toast.makeText(context, "Don't have call permission", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(contactList: List<ContactEntity>){
        this.contactsList.apply {
            clear()
            addAll(contactList)
        }
        notifyDataSetChanged()
    }

}