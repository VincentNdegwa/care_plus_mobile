package com.example.careplus.ui.medications

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
    private val onError: (String) -> Unit,
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
                    selectedTimeSlots = emptyList<String>().toMutableList()
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
        viewModel.isLoading.observe(lifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.scheduleTimeSlots.observe(lifecycleOwner) { result ->
            result.onSuccess { times ->
                Log.d("MedicationScheduleDialog", "Received time slots: $times")
                timeSlotAdapter.setTimeSlots(times)
                selectedTimeSlots = times.toMutableList()
            }.onFailure { exception ->
                Log.e("MedicationScheduleDialog", "Error fetching time slots", exception)
                onError(exception.message ?: "Failed to fetch time slots")
            }
        }

        viewModel.scheduleCreated.observe(lifecycleOwner) { result ->
            result.onSuccess { res ->
                if (isNearNowSchedule) {
                    showTakeMedicationNowDialog()
                } else {
                    dialog.dismiss()
                    onScheduleCreated(false)
                }
            }.onFailure { exception ->
                Log.e("MedicationScheduleDialog", "Error creating schedule", exception)
                onError(exception.localizedMessage ?: exception.message ?: "Failed to create schedule")
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

    private fun validateData(request: CreateScheduleRequest): Boolean {
        // Check if schedules list is empty
        if (request.schedules.isEmpty()) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Invalid Schedule")
                .setMessage("Please add at least one time slot for the medication schedule")
                .setPositiveButton("OK", null)
                .show()
            return false
        }

        // Check if number of schedules matches the frequency
        val expectedSlots = when (medicationDetails.frequency) {
            "Once a day" -> 1
            "Twice a day" -> 2
            "Three times a day" -> 3
            "Four times a day" -> 4
            "Once every other day" -> 1
            else -> 0
        }

        if (request.schedules.size != expectedSlots) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Invalid Schedule")
                .setMessage("Please add ${expectedSlots} time slot${if (expectedSlots > 1) "s" else ""} for ${medicationDetails.frequency?.lowercase()}")
                .setPositiveButton("OK", null)
                .show()
            return false
        }

        // For custom schedules, validate time slots are not too close together
        if (binding.customScheduleRadio.isChecked) {
            val sortedTimes = request.schedules.map { 
                LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
            }.sorted()

            // Check for minimum gap between doses (e.g., 2 hours)
            for (i in 0 until sortedTimes.size - 1) {
                val duration = Duration.between(sortedTimes[i], sortedTimes[i + 1])
                if (duration.toHours() < 2) {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid Time Slots")
                        .setMessage("Please ensure there is at least 2 hours between medication times")
                        .setPositiveButton("OK", null)
                        .show()
                    return false
                }
            }

            // For medications taken multiple times a day, check if times are well distributed
            if (sortedTimes.size > 1) {
                val firstToLast = Duration.between(sortedTimes.first(), sortedTimes.last()).toHours()
                if (firstToLast < 6) {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid Distribution")
                        .setMessage("Please distribute medication times more evenly throughout the day")
                        .setPositiveButton("OK", null)
                        .show()
                    return false
                }
            }
        }

        // Validate medication_id
        if (request.medication_id <= 0) {
            onError("Invalid medication ID")
            return false
        }

        // Validate start_datetime format
        try {
            LocalDateTime.parse(
                request.start_datetime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
        } catch (e: Exception) {
            onError("Invalid date format")
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
        if (!validateData(request)) return
        Log.d("MedicationScheduleDialog", "Creating schedule with request: $request")

        viewModel.createSchedule(request)
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

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.startButton.isEnabled = !show
        binding.cancelButton.isEnabled = !show
    }
} 