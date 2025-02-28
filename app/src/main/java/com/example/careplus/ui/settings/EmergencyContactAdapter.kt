package com.example.careplus.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.R
import com.example.careplus.data.model.settings.EmergencyContact

class EmergencyContactAdapter(
    private val contacts: MutableList<EmergencyContact>,
    private val fragment: SettingsFragment // Reference to the fragment
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        val phoneTextView: TextView = itemView.findViewById(R.id.textViewPhone)
        val emailTextView: TextView = itemView.findViewById(R.id.textViewEmail)
        val deleteIcon: ImageView = itemView.findViewById(R.id.iconDelete)
        val editIcon: ImageView = itemView.findViewById(R.id.iconEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phone
        holder.emailTextView.text = contact.email

        holder.editIcon.setOnClickListener {
            fragment.showAddEmergencyContactDialog(contact, position)
        }

        holder.deleteIcon.setOnClickListener {
            contacts.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = contacts.size
}