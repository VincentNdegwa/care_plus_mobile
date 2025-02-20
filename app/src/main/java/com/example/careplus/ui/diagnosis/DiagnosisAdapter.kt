package com.example.careplus.ui.diagnosis

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.model.diagnosis.Diagnosis
import com.example.careplus.databinding.ItemDiagnosisBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiagnosisAdapter(
    private val onItemClick: (Diagnosis) -> Unit
) : ListAdapter<Diagnosis, DiagnosisAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiagnosisBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDiagnosisBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(diagnosis: Diagnosis) {
            binding.apply {
                diagnosisNameText.text = diagnosis.diagnosis_name
                doctorNameText.text = "Dr. ${diagnosis.doctor.name}"
                
                // Format date
                val date = LocalDate.parse(diagnosis.date_diagnosed)
                val formattedDate = date.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy")
                )
                dateText.text = formattedDate
                
                medicationCountText.text = "${diagnosis.medication_counts} medications"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Diagnosis>() {
        override fun areItemsTheSame(oldItem: Diagnosis, newItem: Diagnosis): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Diagnosis, newItem: Diagnosis): Boolean {
            return oldItem == newItem
        }
    }
} 