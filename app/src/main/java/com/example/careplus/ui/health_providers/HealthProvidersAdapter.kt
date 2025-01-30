package com.example.careplus.ui.health_providers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.model.Data
import com.example.careplus.databinding.ItemCaregiverBinding

class HealthProvidersAdapter : ListAdapter<Data, HealthProvidersAdapter.CaregiverViewHolder>(CaregiverDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaregiverViewHolder {
        val binding = ItemCaregiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaregiverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaregiverViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CaregiverViewHolder(private val binding: ItemCaregiverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(caregiver: Data) {
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

    private class CaregiverDiffCallback : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }
}