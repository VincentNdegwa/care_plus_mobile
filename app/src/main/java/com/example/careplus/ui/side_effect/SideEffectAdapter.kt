package com.example.careplus.ui.side_effect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.model.side_effect.SideEffect
import com.example.careplus.databinding.ItemSideEffectBinding
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SideEffectAdapter(
    private val onItemClick: (SideEffect) -> Unit
) : ListAdapter<SideEffect, SideEffectAdapter.ViewHolder>(SideEffectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSideEffectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSideEffectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(sideEffect: SideEffect) {
            binding.apply {
                sideEffectName.text = sideEffect.side_effect
                severityText.text = "Severity: ${sideEffect.severity}"
                dateTimeText.text = formatDateTime(sideEffect.datetime)
                notesText.text = sideEffect.notes ?: "No notes"
            }
        }

        private fun formatDateTime(dateTime: String): String {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val utcDateTime = LocalDateTime.parse(dateTime, formatter)
            val localDateTime = utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
            
            return localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
        }
    }

    private class SideEffectDiffCallback : DiffUtil.ItemCallback<SideEffect>() {
        override fun areItemsTheSame(oldItem: SideEffect, newItem: SideEffect): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SideEffect, newItem: SideEffect): Boolean {
            return oldItem == newItem
        }
    }
} 