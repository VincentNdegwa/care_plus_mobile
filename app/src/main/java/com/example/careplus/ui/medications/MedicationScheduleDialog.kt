package com.example.careplus.ui.medications

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.databinding.DialogMedicationScheduleBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Data class for the schedule request
data class CreateScheduleRequest(
    val medicationId: Int,
    val schedules: List<String>,
    val startDatetime: String
)

class MedicationScheduleDialog(
    private val context: Context,
    private val medicationDetails: MedicationDetails,
    private val viewModel: MedicationScheduleViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onScheduleCreated: (Boolean) -> Unit
) {
    private lateinit var binding: DialogMedicationScheduleBinding
    private lateinit var dialog: AlertDialog
    private val timeSlotAdapter = TimeSlotAdapter()
    private val customTimeSlotAdapter = TimeSlotAdapter()
    private var selectedDateTime: LocalDateTime = LocalDateTime.now()
    private var selectedTimeSlots = mutableListOf<String>()
    private var isNearNowSchedule = false

    private val allowedCustomSchedule = listOf(
        "Once a day",
        "Twice a day",
        "Three times a day",
        "Four times a day",
        "Once every other day"
    )

    fun show() {
        binding = DialogMedicationScheduleBinding.inflate(LayoutInflater.from(context))
        setupViews()
        setupListeners()

        dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .create()

        dialog.show()
    }

    private fun setupViews() {
        // Setup RecyclerViews
        binding.timeSlotsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timeSlotAdapter
        }

        binding.customTimeSlotsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customTimeSlotAdapter
        }

        // Show/Hide custom schedule option based on frequency
        binding.customScheduleRadio.isVisible = 
            allowedCustomSchedule.contains(medicationDetails.frequency)
    }

    private fun setupListeners() {
        binding.startTimeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.customTimeLayout.isVisible = checkedId == R.id.customTimeRadioButton
            if (checkedId == R.id.nowRadioButton) {
                selectedDateTime = LocalDateTime.now()
            }
        }

        binding.dateButton.setOnClickListener { showDatePicker() }
        binding.timeButton.setOnClickListener { showTimePicker() }

        binding.scheduleModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.defaultScheduleRadio -> {
                    fetchDefaultSchedule()
                    binding.timeSlotsList.isVisible = true
                    binding.customTimeSlotsLayout.isVisible = false
                }
                R.id.customScheduleRadio -> {
                    binding.timeSlotsList.isVisible = false
                    binding.customTimeSlotsLayout.isVisible = true
                }
            }
        }

        binding.addTimeSlotButton.setOnClickListener {
            if (canAddMoreTimeSlots()) {
                showTimeSlotPicker()
            } else {
//                showMaxTimeSlotsMessage()
            }
        }

        binding.startButton.setOnClickListener {
            createSchedule()
        }

        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showDatePicker() {
        val currentDate = LocalDate.now()
        MaterialDatePicker.Builder.datePicker()
            .setSelection(System.currentTimeMillis())
            .build()
            .apply {
                addOnPositiveButtonClickListener { selection ->
                    val selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    selectedDateTime = selectedDateTime.with(selectedDate)
                    updateDateTimeButtons()
                }
            }
            .show((context as FragmentActivity).supportFragmentManager, "datePicker")
    }

    private fun showTimePicker() {
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(selectedDateTime.hour)
            .setMinute(selectedDateTime.minute)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    selectedDateTime = selectedDateTime
                        .withHour(hour)
                        .withMinute(minute)
                    updateDateTimeButtons()
                }
            }
            .show((context as FragmentActivity).supportFragmentManager, "timePicker")
    }

    private fun updateDateTimeButtons() {
        binding.dateButton.text = selectedDateTime.format(
            DateTimeFormatter.ofPattern("MMM dd, yyyy")
        )
        binding.timeButton.text = selectedDateTime.format(
            DateTimeFormatter.ofPattern("HH:mm")
        )
    }

    private fun fetchDefaultSchedule() {
        // Make API call to get default schedule
        viewModel.generateScheduleTimes(medicationDetails.id.toInt()).observe(lifecycleOwner) { result ->
            result.onSuccess { times ->
                timeSlotAdapter.setTimeSlots(times)
                selectedTimeSlots = times.toMutableList()
            }.onFailure {
                // Handle error
            }
        }
    }

    private fun showTimeSlotPicker() {
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    val timeSlot = String.format("%02d:%02d", hour, minute)
                    if (!selectedTimeSlots.contains(timeSlot)) {
                        selectedTimeSlots.add(timeSlot)
                        customTimeSlotAdapter.setTimeSlots(selectedTimeSlots)
                    } else {
                        // Show error that time is already selected
                    }
                }
            }
            .show((context as FragmentActivity).supportFragmentManager, "timeSlotPicker")
    }

    private fun canAddMoreTimeSlots(): Boolean {
        val maxSlots = when (medicationDetails.frequency) {
            "Once a day" -> 1
            "Twice a day" -> 2
            "Three times a day" -> 3
            "Four times a day" -> 4
            "Once every other day" -> 1
            else -> 0
        }
        return selectedTimeSlots.size < maxSlots
    }

    private fun createSchedule() {
        val startDateTime = if (binding.nowRadioButton.isChecked) {
            LocalDateTime.now()
        } else {
            selectedDateTime
        }

        isNearNowSchedule = isTimeNearNow(startDateTime)

        val request = CreateScheduleRequest(
            medicationId = medicationDetails.id.toInt(),
            schedules = selectedTimeSlots,
            startDatetime = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )

        viewModel.createSchedule(request).observe(lifecycleOwner) { result ->
            result.onSuccess {
                if (isNearNowSchedule) {
                    showTakeMedicationNowDialog()
                } else {
                    dialog.dismiss()
                    onScheduleCreated(false)
                }
            }.onFailure {
                // Handle error
            }
        }
    }

    private fun isTimeNearNow(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        val diff = Duration.between(now, dateTime).abs()
        return diff.toMinutes() <= 5 // Consider "near now" if within 5 minutes
    }

    private fun showTakeMedicationNowDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle("Take Medication Now?")
            .setMessage("Would you like to take the medication now and mark it as taken?")
            .setPositiveButton("Yes") { _, _ ->
                dialog.dismiss()
                onScheduleCreated(true)
            }
            .setNegativeButton("No") { _, _ ->
                dialog.dismiss()
                onScheduleCreated(false)
            }
            .show()
    }
} 