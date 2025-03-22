package com.example.careplus.ui.health_providers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.model.CaregiverData
import com.example.careplus.databinding.ItemCaregiverBinding

class HealthProvidersAdapter(
    private val onProviderClick: (CaregiverData) -> Unit, // Callback to handle item clicks
) : ListAdapter<CaregiverData, HealthProvidersAdapter.CaregiverViewHolder>(CaregiverDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaregiverViewHolder {
        val binding = ItemCaregiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaregiverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaregiverViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CaregiverViewHolder(private val binding: ItemCaregiverBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val caregiver = getItem(position)
                    onProviderClick(caregiver) // Trigger the click callback
                }
            }
        }

        fun bind(caregiver: CaregiverData) {
            binding.apply {
                caregiverName.text = caregiver.name
                caregiverEmail.text = caregiver.email
                caregiverRole.text = caregiver.role
                if (caregiver.user_role.specialization != null){
                    caregiverSpeciality.text = caregiver.user_role.specialization?.toString()
                }
            }
        }
    }

    private class CaregiverDiffCallback : DiffUtil.ItemCallback<CaregiverData>() {
        override fun areItemsTheSame(oldItem: CaregiverData, newItem: CaregiverData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CaregiverData, newItem: CaregiverData): Boolean {
            return oldItem == newItem
        }
    }
}