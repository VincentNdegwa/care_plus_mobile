package com.example.careplus.ui.medications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.databinding.ItemTimeSlotBinding

class TimeSlotAdapter : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    private var timeSlots = listOf<String>()

    fun setTimeSlots(slots: List<String>) {
        timeSlots = slots
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount() = timeSlots.size

    class TimeSlotViewHolder(private val binding: ItemTimeSlotBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(timeSlot: String) {
            binding.timeText.text = timeSlot
        }
    }
} 