package com.example.careplus.ui.diagnosis

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.careplus.R
import com.example.careplus.data.SessionManager
import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.model.diagnosis.Diagnosis
import com.example.careplus.data.model.diagnosis.DiagnosisDoctor
import com.example.careplus.data.model.diagnosis.DiagnosisFilterRequest
import com.example.careplus.databinding.BottomSheetDiagnosisFilterBinding
import com.example.careplus.ui.medications.MedicationFilterBottomSheet.FilterListener
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiagnosisFilterBottomSheet() : BottomSheetDialogFragment() {
    private var _binding: BottomSheetDiagnosisFilterBinding? = null
    private val binding get() = _binding!!

    private var dateFrom: LocalDate? = null
    private var dateTo: LocalDate? = null
    
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private var selectedDoctorId: Int? = null
    private var patientId:Int? = null
    private val doctors = mutableListOf<DiagnosisDoctor>()
    private var pendingDiagnoses: List<Diagnosis>? = null


    var currentFilter: DiagnosisFilterRequest? = null
    private var filterListener: FilterListener? = null


    interface FilterListener {
        fun onFiltersApplied(filter: DiagnosisFilterRequest?)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDiagnosisFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            BottomSheetUtils.setupBottomSheetDialog(this, it as BottomSheetDialog)
        }
        
        setupListeners()
        
        // Set up doctors first
        pendingDiagnoses?.let {
            setupDoctors(it)
            pendingDiagnoses = null
        }
        
        // Then restore filter state after doctors are set up
        currentFilter?.let { filter ->
            restoreFilterState(filter)
        }
    }

    private fun setupListeners() {
        binding.apply {
            clearButton.setOnClickListener {
                clearFilters()
            }

            dateFromInput.setOnClickListener {
                showDatePicker(isDateFrom = true)
            }

            dateToInput.setOnClickListener {
                showDatePicker(isDateFrom = false)
            }

            applyButton.setOnClickListener {
                applyFilters()
            }
        }
    }

    private fun showDatePicker(isDateFrom: Boolean) {
        val today = LocalDate.now()
        val initialDate = if (isDateFrom) dateFrom else dateTo ?: today
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = LocalDate.of(year, month + 1, day)
                if (isDateFrom) {
                    if (dateTo != null && selectedDate.isAfter(dateTo)) {
                        // If selected date is after dateTo, update dateTo
                        dateTo = selectedDate
                        binding.dateToInput.setText(selectedDate.format(dateFormatter))
                    }
                    dateFrom = selectedDate
                    binding.dateFromInput.setText(selectedDate.format(dateFormatter))
                } else {
                    if (dateFrom != null && selectedDate.isBefore(dateFrom)) {
                        // If selected date is before dateFrom, update dateFrom
                        dateFrom = selectedDate
                        binding.dateFromInput.setText(selectedDate.format(dateFormatter))
                    }
                    dateTo = selectedDate
                    binding.dateToInput.setText(selectedDate.format(dateFormatter))
                }
            },
            initialDate?.year ?: today.year,
            (initialDate?.monthValue ?: today.monthValue) - 1,
            initialDate?.dayOfMonth ?: today.dayOfMonth
        ).show()
    }

    private fun clearFilters() {
        binding.apply {
            diagnosisNameInput.text?.clear()
            doctorInput.text?.clear()
            dateFromInput.text?.clear()
            dateToInput.text?.clear()
        }
        dateFrom = null
        dateTo = null
        selectedDoctorId = null
        currentFilter = null
        
        filterListener?.onFiltersApplied(null)
        dismiss()
    }

    private fun applyFilters() {
        val request = DiagnosisFilterRequest(
            diagnosis_name = binding.diagnosisNameInput.text?.toString()?.takeIf { it.isNotEmpty() },
            date_from = dateFrom?.format(apiDateFormatter),
            date_to = dateTo?.format(apiDateFormatter),
            doctor_id = selectedDoctorId,
            patient_id = patientId
        )
        filterListener?.onFiltersApplied(request)
        currentFilter = request
        dismiss()
    }
    fun setDoctors(diagnosisList: List<Diagnosis>) {
        if (isAdded) {
            setupDoctors(diagnosisList)
            // Restore filter state after setting up doctors
            currentFilter?.let { filter ->
                restoreFilterState(filter)
            }
        } else {
            pendingDiagnoses = diagnosisList
        }
    }
    fun setPatientId(patientId: Int?){
        this@DiagnosisFilterBottomSheet.patientId = patientId
    }

    private fun setupDoctors(diagnosisList: List<Diagnosis>) {
        doctors.clear()
        doctors.addAll(diagnosisList.map { it.doctor }.distinctBy { it.id })
        setupDoctorDropdown()
    }

    private fun setupDoctorDropdown() {
        val items = doctors.map { "Dr. ${it.name}" }.toTypedArray()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            items
        )
        binding.doctorInput.setAdapter(adapter)
        
        binding.doctorInput.setOnItemClickListener { _, _, position, _ ->
            selectedDoctorId = doctors[position].id
        }
    }

    private fun restoreFilterState(filter: DiagnosisFilterRequest) {
        // Restore diagnosis name
        binding.diagnosisNameInput.setText(filter.diagnosis_name ?: "")

        // Restore doctor selection if doctors are loaded
        filter.doctor_id?.let { doctorId ->
            val doctor = doctors.find { it.id == doctorId }
            if (doctor != null) {
                binding.doctorInput.setText("Dr. ${doctor.name}", false)
                selectedDoctorId = doctorId
            }
        }

        // Restore dates
        filter.date_from?.let { dateStr ->
            try {
                val date = LocalDate.parse(dateStr, apiDateFormatter)
                dateFrom = date
                binding.dateFromInput.setText(date.format(dateFormatter))
            } catch (e: Exception) {
                // Handle invalid date format
            }
        }

        filter.date_to?.let { dateStr ->
            try {
                val date = LocalDate.parse(dateStr, apiDateFormatter)
                dateTo = date
                binding.dateToInput.setText(date.format(dateFormatter))
            } catch (e: Exception) {
                // Handle invalid date format
            }
        }
    }

    fun setFilterListener(listener: FilterListener) {
        filterListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DiagnosisFilterBottomSheet"
    }
} 