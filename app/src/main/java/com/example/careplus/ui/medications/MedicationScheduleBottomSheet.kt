package com.example.careplus.ui.medications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.databinding.DialogMedicationScheduleBinding
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
data class CreateScheduleRequest_1(
    val medication_id: Int,
    val schedules: List<String>,
    val start_datetime: String
)

class MedicationScheduleBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: DialogMedicationScheduleBinding
    private lateinit var medicationDetails: MedicationDetails
    private lateinit var viewModel: MedicationScheduleViewModel
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var onError: (String) -> Unit
    private lateinit var onScheduleCreated: (Boolean) -> Unit
    private val timeSlotAdapter = TimeSlotAdapter()
    private val customTimeSlotAdapter = TimeSlotAdapter()
    private var selectedDateTime: LocalDateTime = LocalDateTime.now()
    private var selectedTimeSlots = mutableListOf<String>()
    private var isNearNowSchedule = false
    private lateinit var fragment: FragmentManager

    private val allowedCustomSchedule = listOf(
        "Once a day",
        "Twice a day",
        "Three times a day",
        "Four times a day",
        "Once every other day"
    )

    companion object {
        fun newInstance(
            medicationDetails: MedicationDetails,
            viewModel: MedicationScheduleViewModel,
            lifecycleOwner: LifecycleOwner,
            onError: (String) -> Unit,
            onScheduleCreated: (Boolean) -> Unit,
            fragment: FragmentManager
        ) = MedicationScheduleBottomSheet().apply {
            this.medicationDetails = medicationDetails
            this.viewModel = viewModel
            this.lifecycleOwner = lifecycleOwner
            this.onError = onError
            this.onScheduleCreated = onScheduleCreated
            this.fragment = fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMedicationScheduleBinding.inflate(inflater, container, false)
        setupViews()
        setupListeners()
        setupObservers()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        BottomSheetUtils.setupBottomSheetDialog(this, dialog)
        return dialog
    }

    private fun setupViews() {
        // Setup RecyclerViews
        binding.timeSlotsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timeSlotAdapter
        }

        binding.customTimeSlotsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
            }
        }

        binding.startButton.setOnClickListener {
            createSchedule()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(lifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.scheduleTimeSlots.observe(lifecycleOwner) { result ->
            result.onSuccess { times ->
                Log.d("MedicationScheduleBottomSheet", "Received time slots: $times")
                timeSlotAdapter.setTimeSlots(times)
                selectedTimeSlots = times.toMutableList()
            }.onFailure { exception ->
                Log.e("MedicationScheduleBottomSheet", "Error fetching time slots", exception)
                onError(exception.message ?: "Failed to fetch time slots")
            }
        }

        viewModel.scheduleCreated.observe(lifecycleOwner) { result ->
            result.onSuccess { res ->
                if (isNearNowSchedule) {
                    showTakeMedicationNowDialog()
                } else {
                    dismiss()
                    onScheduleCreated(false)
                }
            }.onFailure { exception ->
                Log.e("MedicationScheduleBottomSheet", "Error creating schedule", exception)
                onError(exception.localizedMessage ?: exception.message ?: "Failed to create schedule")
            }
        }
    }

    private fun showDatePicker() {
        val today = LocalDate.now()
        val todayMillis = System.currentTimeMillis()

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
                show(fragment, "datePicker")
            }
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
                            MaterialAlertDialogBuilder(requireContext())
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
                    this@MedicationScheduleBottomSheet.selectedDateTime = this@MedicationScheduleBottomSheet.selectedDateTime
                        .withHour(hour)
                        .withMinute(minute)
                    updateDateTimeButtons()
                    if(binding.defaultScheduleRadio.isChecked){
                        fetchDefaultSchedule()
                    }
                }
                show(fragment, "timePicker")
            }
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
        Log.d("MedicationScheduleBottomSheet", "Fetching default schedule")
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
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Duplicate Time")
                            .setMessage("This time is already selected")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
                show(fragment, "timeSlotPicker")
            }
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
            MaterialAlertDialogBuilder(requireContext())
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
            MaterialAlertDialogBuilder(requireContext())
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
            MaterialAlertDialogBuilder(requireContext())
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
                    MaterialAlertDialogBuilder(requireContext())
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
                    MaterialAlertDialogBuilder(requireContext())
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
        Log.d("MedicationScheduleBottomSheet", "Creating schedule with request: $request")

        viewModel.createSchedule(request)
        dismiss()
    }

    private fun isTimeNearNow(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        val diff = Duration.between(now, dateTime).abs()
        return diff.toMinutes() <= 5 // Consider "near now" if within 5 minutes
    }

    private fun showTakeMedicationNowDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Take Medication Now?")
            .setMessage("Would you like to take the medication now and mark it as taken?")
            .setPositiveButton("Yes") { _, _ ->
                dismiss()
                onScheduleCreated(true)
            }
            .setNegativeButton("No") { _, _ ->
                dismiss()
                onScheduleCreated(false)
            }
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.startButton.isEnabled = !isLoading
        binding.cancelButton.isEnabled = !isLoading
    }
}