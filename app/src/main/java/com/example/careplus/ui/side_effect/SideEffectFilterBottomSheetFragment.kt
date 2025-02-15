package com.example.careplus.ui.side_effect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.side_effect.FetchSideEffectsRequest
import com.example.careplus.databinding.FragmentSideEffectFilterBinding
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.app.DatePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.careplus.data.model.side_effect.SideEffectMedication

class SideEffectFilterBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSideEffectFilterBinding? = null
    private val binding get() = _binding!!
    private var filterListener: FilterListener? = null
    private var currentFilter: FetchSideEffectsRequest? = null
    private var medications = listOf<SideEffectMedication>()
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private var fromDate: LocalDate? = null
    private var toDate: LocalDate? = null

    interface FilterListener {
        fun onFiltersApplied(filter: FetchSideEffectsRequest?)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSideEffectFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetUtils.setupBottomSheetDialog(this, dialog as com.google.android.material.bottomsheet.BottomSheetDialog)
        setupViews()
    }

    private fun setupViews() {
        // Setup severity dropdown
        val severities = arrayOf("Mild", "Moderate", "Severe")
        val severityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            severities
        )
        binding.severityInput.setAdapter(severityAdapter)

        // Setup medication dropdown
        val medicationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            medications.map { it.medication_name }
        )
        binding.medicationDropdown.setAdapter(medicationAdapter)

        // Setup date pickers
        binding.fromDateInput.setOnClickListener { showDatePicker(true) }
        binding.toDateInput.setOnClickListener { showDatePicker(false) }

        // Load current filters if any
        currentFilter?.let { filter ->
            binding.severityInput.setText(filter.severity ?: "All", false)
            
            // Set medication if exists
            filter.medication_id?.let { medId ->
                val medication = medications.find { it.id == medId }
                binding.medicationDropdown.setText(medication?.medication_name ?: "", false)
            }

            // Set dates if they exist
            filter.from_datetime?.let {
                fromDate = LocalDate.parse(it.split("T")[0])
                binding.fromDateInput.setText(fromDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
            }
            filter.to_datetime?.let {
                toDate = LocalDate.parse(it.split("T")[0])
                binding.toDateInput.setText(toDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
            }
        }

        // Apply filters
        binding.applyButton.setOnClickListener {
            applyFilters()
        }

        // Clear filters
        binding.clearButton.setOnClickListener {
            filterListener?.onFiltersApplied(null)
            dismiss()
        }
    }

    private fun applyFilters() {
        val severity = binding.severityInput.text.toString().takeIf { it != "All" }
        val medicationName = binding.medicationDropdown.text.toString()
        val medicationId = medications.find { it.medication_name == medicationName }?.id

        val filter = if (severity == null && medicationId == null && fromDate == null && toDate == null) {
            null
        } else {
            SessionManager(requireContext()).getUser()?.patient?.id?.let { patientId ->
                FetchSideEffectsRequest(
                    patient_id = patientId,
                    severity = severity,
                    medication_id = medicationId,
                    from_datetime = fromDate?.atStartOfDay()?.format(dateFormatter),
                    to_datetime = toDate?.atStartOfDay()?.format(dateFormatter),
                    page_number = 1,
                    per_page = 20
                )
            }
        }

        filterListener?.onFiltersApplied(filter)
        dismiss()
    }

    private fun showDatePicker(isFromDate: Boolean) {
        val calendar = java.util.Calendar.getInstance()
        val currentDate = if (isFromDate) fromDate else toDate
        
        currentDate?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = LocalDate.of(year, month + 1, day)
                
                if (isFromDate) {
                    if (toDate != null && selectedDate.isAfter(toDate)) {
                        // Show error message
                        binding.startDateLayout.error = "Start date cannot be after end date"
                        return@DatePickerDialog
                    }
                    fromDate = selectedDate
                    binding.startDateLayout.error = null
                    binding.fromDateInput.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                } else {
                    // When selecting to_date, ensure it's not before from_date
                    if (fromDate != null && selectedDate.isBefore(fromDate)) {
                        // Show error message
                        binding.endDateLayout.error = "End date cannot be before start date"
                        return@DatePickerDialog
                    }
                    toDate = selectedDate
                    binding.endDateLayout.error = null
                    binding.toDateInput.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                }
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )

        // Set min/max dates
        if (!isFromDate && fromDate != null) {
            // For to_date picker, set minimum date to from_date
            val minDate = calendar.clone() as java.util.Calendar
            minDate.set(fromDate!!.year, fromDate!!.monthValue - 1, fromDate!!.dayOfMonth)
            datePicker.datePicker.minDate = minDate.timeInMillis
        } else if (isFromDate && toDate != null) {
            // For from_date picker, set maximum date to to_date
            val maxDate = calendar.clone() as java.util.Calendar
            maxDate.set(toDate!!.year, toDate!!.monthValue - 1, toDate!!.dayOfMonth)
            datePicker.datePicker.maxDate = maxDate.timeInMillis
        }

        datePicker.show()
    }

    fun setFilterListener(listener: FilterListener) {
        filterListener = listener
    }

    fun setMedications(medications: List<SideEffectMedication>) {
        this.medications = medications
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PATIENT_ID = "patient_id"
        private const val ARG_CURRENT_FILTER = "current_filter"

        fun newInstance(
            patientId: Int?, 
            currentFilter: FetchSideEffectsRequest? = null,
            medicationList: List<SideEffectMedication>
        ): SideEffectFilterBottomSheetFragment {
            return SideEffectFilterBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    patientId?.let { putInt(ARG_PATIENT_ID, it) }
                }
                this.currentFilter = currentFilter
                this.medications = medicationList
            }
        }
    }
} 