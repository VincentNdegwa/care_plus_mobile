package com.example.careplus.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.databinding.ItemMedicationScheduleBinding
import com.example.careplus.R
import com.example.careplus.data.model.Schedule
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.core.content.ContextCompat
import com.example.careplus.data.model.ScheduledMedication

class MedicationScheduleAdapter : 
    RecyclerView.Adapter<MedicationScheduleAdapter.ScheduleViewHolder>() {
    
    private var schedules: List<Schedule> = emptyList()

    fun submitList(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemMedicationScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount() = schedules.size

    class ScheduleViewHolder(
        private val binding: ItemMedicationScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(schedule: Schedule) {
            binding.apply {
                medicationName.text = schedule.medication.medication_name
                dosage.text = "${schedule.medication.dosage_quantity} ${schedule.medication.dosage_strength}"
                
                // Convert UTC time to local time
                val utcDateTime = LocalDateTime.parse(
                    schedule.dose_time.replace(" ", "T")
                )
                val localDateTime = utcDateTime
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault())
                
                // Format time in 12-hour format with AM/PM
                time.text = localDateTime.format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                )
                
                status.text = schedule.status
                
                // Set status color
                status.setTextColor(when(schedule.status.lowercase()) {
                    "pending" -> ContextCompat.getColor(root.context, R.color.primary)
                    "completed" -> ContextCompat.getColor(root.context, R.color.success)
                    else -> ContextCompat.getColor(root.context, R.color.error)
                })

                // Add relative time indicator (e.g., "In 2 hours", "30 minutes ago")
                val now = ZonedDateTime.now(ZoneId.systemDefault())
                val timeDiff = java.time.Duration.between(now, localDateTime)
                
                val relativeTime = when {
                    timeDiff.toHours() > 0 -> {
                        "In ${timeDiff.toHours()} hour${if (timeDiff.toHours() > 1) "s" else ""}"
                    }
                    timeDiff.toMinutes() > 0 -> {
                        "In ${timeDiff.toMinutes()} minute${if (timeDiff.toMinutes() > 1) "s" else ""}"
                    }
                    timeDiff.toMinutes() < 0 -> {
                        val ago = -timeDiff.toMinutes()
                        "${ago} minute${if (ago > 1) "s" else ""} ago"
                    }
                    else -> "Now"
                }
                
                timeRelative.text = relativeTime
            }
        }
    }
} 