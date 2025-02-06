package com.example.careplus.ui.medications

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.databinding.DialogMedicationScheduleBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Data class for the schedule request
data class CreateScheduleRequest(
    val medication_id: Int,
    val schedules: List<String>,
    val start_datetime: String
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
        setupObservers()

        dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
            }

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

    private fun setupObservers() {
        viewModel.scheduleTimeSlots.observe(lifecycleOwner) { result ->
            result.onSuccess { times ->
                Log.d("MedicationScheduleDialog", "Received time slots: $times")
                timeSlotAdapter.setTimeSlots(times)
                selectedTimeSlots = times.toMutableList()
            }.onFailure { exception ->
                Log.e("MedicationScheduleDialog", "Error fetching time slots", exception)
                // Show error message to user
            }
        }

        viewModel.scheduleCreated.observe(lifecycleOwner) { result ->
            result.onSuccess {
                if (isNearNowSchedule) {
                    showTakeMedicationNowDialog()
                } else {
                    dialog.dismiss()
                    onScheduleCreated(false)
                }
            }.onFailure { exception ->
                Log.e("MedicationScheduleDialog", "Error creating schedule", exception)
                // Show error message to user
            }
        }
    }

    private fun showDatePicker() {
        val today = LocalDate.now()
        val todayMillis = System.currentTimeMillis() // This will give current date/time in milliseconds

        MaterialDatePicker.Builder.datePicker()
            .setSelection(todayMillis)
            .setTitleText("Select Start Date")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(todayMillis)
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
            .build()
            .apply {
                addOnPositiveButtonClickListener { selection ->
                    val selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    
                    if (selectedDate.isEqual(today)) {
                        val currentTime = LocalDateTime.now()
                        selectedDateTime = LocalDateTime.of(selectedDate, currentTime.toLocalTime())
                    } else {
                        selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(0, 0))
                    }
                    updateDateTimeButtons()
                    if(binding.defaultScheduleRadio.isChecked){
                        fetchDefaultSchedule()
                    }


                    // If today is selected, immediately show time picker
                    if (selectedDate.isEqual(today)) {
                        showTimePicker()
                    }
                }
            }
            .show((context as FragmentActivity).supportFragmentManager, "datePicker")
    }

    private fun showTimePicker() {
        val now = LocalDateTime.now()
        val isToday = selectedDateTime.toLocalDate().isEqual(LocalDate.now())
        
        // Set initial hour and minute for today
        val initialHour = if (isToday) {
            now.hour
        } else {
            selectedDateTime.hour
        }
        
        val initialMinute = if (isToday) {
            now.minute + 1
        } else {
            selectedDateTime.minute
        }

        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialHour)
            .setMinute(initialMinute)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    val selectedTime = LocalTime.of(hour, minute)
                    
                    if (isToday) {
                        val selectedDateTime = LocalDateTime.of(LocalDate.now(), selectedTime)
                        val currentDateTime = LocalDateTime.now()
                        
                        if (selectedDateTime.isBefore(currentDateTime)) {
                            MaterialAlertDialogBuilder(this@MedicationScheduleDialog.context)
                                .setTitle("Invalid Time")
                                .setMessage("Please select a future time")
                                .setPositiveButton("OK") { _, _ ->
                                    // Show time picker again
                                    showTimePicker()
                                }
                                .show()
                            return@addOnPositiveButtonClickListener
                        }
                    }
                    
                    // If we get here, the time is valid
                    this@MedicationScheduleDialog.selectedDateTime = this@MedicationScheduleDialog.selectedDateTime
                        .withHour(hour)
                        .withMinute(minute)
                    updateDateTimeButtons()
                    if(binding.defaultScheduleRadio.isChecked){
                        fetchDefaultSchedule()
                    }                }
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
        Log.d("MedicationScheduleDialog", "Fetching default schedule")
        val startDateTime = if (binding.nowRadioButton.isChecked) {
            LocalDateTime.now()
        } else {
            selectedDateTime
        }

        viewModel.generateScheduleTimes(
            medicationId = medicationDetails.id.toInt(),
            startDateTime = startDateTime
        )
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

    private fun validateDateTime(): Boolean {
        val now = LocalDateTime.now()
        val selectedDT = if (binding.nowRadioButton.isChecked) {
            now
        } else {
            selectedDateTime
        }
        
        if (selectedDT.isBefore(now)) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Invalid Date/Time")
                .setMessage("Please select a future date and time")
                .setPositiveButton("OK") { _, _ ->
                    // Show date picker again
                    showDatePicker()
                }
                .show()
            return false
        }
        return true
    }

    private fun createSchedule() {
        if (!validateDateTime()) return

        val startDateTime = if (binding.nowRadioButton.isChecked) {
            LocalDateTime.now()
        } else {
            selectedDateTime
        }

        isNearNowSchedule = isTimeNearNow(startDateTime)

        val request = CreateScheduleRequest(
            medication_id = medicationDetails.id.toInt(),
            schedules = selectedTimeSlots,
            start_datetime = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        Log.d("MedicationScheduleDialog", request.toString())

//        viewModel.createSchedule(request)
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