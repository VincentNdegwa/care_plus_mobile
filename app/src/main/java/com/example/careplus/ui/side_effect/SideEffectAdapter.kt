package com.example.careplus.ui.side_effect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.PopupMenu
import com.example.careplus.R
import com.example.careplus.data.model.side_effect.SideEffect
import com.example.careplus.databinding.ItemSideEffectBinding
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SideEffectAdapter(
    private val onItemClick: (SideEffect) -> Unit,
    private val onEditClick: (SideEffect) -> Unit,
    private val onDeleteClick: (SideEffect) -> Unit
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
                
                // Set severity chip color based on severity level
                severityText.apply {
                    text = "Severity: ${sideEffect.severity}"
                    setChipBackgroundColorResource(when(sideEffect.severity.lowercase()) {
                        "mild" -> R.color.success
                        "moderate" -> R.color.warning
                        "severe" -> R.color.error
                        else -> R.color.primary
                    })
                }
                
                // Set severity indicator color
                severityIndicator.setBackgroundResource(when(sideEffect.severity.lowercase()) {
                    "mild" -> R.color.success
                    "moderate" -> R.color.warning
                    "severe" -> R.color.error
                    else -> R.color.primary
                })

                dateTimeText.text = formatDateTime(sideEffect.datetime)
                notesText.text = sideEffect.notes ?: "No notes"

                menuButton.setOnClickListener { view ->
                    showPopupMenu(view, sideEffect)
                }
            }
        }

        private fun formatDateTime(dateTime: String): String {
            return try {
                // Parse the input format "yyyy-MM-dd HH:mm:ss"
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                
                val localDateTime = LocalDateTime.parse(dateTime, inputFormatter)
                    .atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime()
                
                localDateTime.format(outputFormatter)
            } catch (e: Exception) {
                // Fallback in case of parsing error
                dateTime
            }
        }

        private fun showPopupMenu(view: View, sideEffect: SideEffect) {
            PopupMenu(view.context, view).apply {
                inflate(R.menu.side_effect_item_menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_view -> {
                            onItemClick(sideEffect)
                            true
                        }
                        R.id.action_edit -> {
                            onEditClick(sideEffect)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(sideEffect)
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
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