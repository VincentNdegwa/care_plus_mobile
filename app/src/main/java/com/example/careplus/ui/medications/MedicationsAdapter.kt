package com.example.careplus.ui.medications

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.R
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.databinding.ItemMedicationBinding

class MedicationsAdapter(
    private val onMedicationClick: (MedicationDetails) -> Unit
) : ListAdapter<MedicationDetails, MedicationsAdapter.ViewHolder>(MedicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMedicationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMedicationClick(getItem(position))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(medication: MedicationDetails) {
            binding.apply {
                medicationName.text = medication.medication_name
                dosage.text = "${medication.dosage_quantity} ${medication.dosage_strength}"
                frequency.text = "Take ${medication.frequency} for ${medication.duration}"
                statusChip.apply {
                    if (medication.active == 1) {
                        text = "Active"
                        setChipBackgroundColorResource(R.color.success_light)
                        setTextColor(context.getColor(R.color.success))
                        chipIconTint = ColorStateList.valueOf(context.getColor(R.color.success))
                    } else {
                        text = "Inactive"
                        setChipBackgroundColorResource(R.color.error_light)
                        setTextColor(context.getColor(R.color.error))
                        chipIconTint = ColorStateList.valueOf(context.getColor(R.color.error))
                    }
                }
            }
        }
    }

    private class MedicationDiffCallback : DiffUtil.ItemCallback<MedicationDetails>() {
        override fun areItemsTheSame(oldItem: MedicationDetails, newItem: MedicationDetails): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MedicationDetails, newItem: MedicationDetails): Boolean {
            return oldItem == newItem
        }
    }
} 