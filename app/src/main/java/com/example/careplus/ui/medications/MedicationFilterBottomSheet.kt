package com.example.careplus.ui.medications

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.model.CaregiverInfo
import com.example.careplus.data.model.DoctorInfo
import com.example.careplus.data.model.MedicationForm
import com.example.careplus.data.model.MedicationRoute
import com.example.careplus.databinding.FragmentMedicationFilterBottomSheetBinding
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MedicationFilterBottomSheet(
    private val forms: List<MedicationForm>,
    private val routes: List<MedicationRoute>,
    private val caregivers: List<CaregiverInfo>,
    private val doctors: List<DoctorInfo>,
    private val currentFilter: FilterMedications? = null
) : BottomSheetDialogFragment() {
    private var _binding: FragmentMedicationFilterBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var filterListener: FilterListener? = null
    
    interface FilterListener {
        fun onFiltersApplied(filter: FilterMedications?)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        // Restore state if we have a current filter
        currentFilter?.let { restoreFilterState(it) }
    }

    private fun setupViews() {
        // Setup form dropdown
        val formAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            forms.map { it.name ?: "Unknown Form" }
        )
        binding.formDropdown.setAdapter(formAdapter)

        // Setup route dropdown
        val routeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            routes.map { it.name ?: "Unknown Route" }
        )
        binding.routeDropdown.setAdapter(routeAdapter)

        // Setup doctor dropdown
        val doctorAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            doctors.map { it.name }
        )
        binding.doctorDropdown.setAdapter(doctorAdapter)

        // Setup caregiver dropdown
        val caregiverAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            caregivers.map { it.name }
        )
        binding.caregiverDropdown.setAdapter(caregiverAdapter)

        setupDatePickers()
        setupButtons()
    }

    private fun restoreFilterState(filter: FilterMedications) {
        // Restore active switch
        if (filter.active != null){
            binding.activeSwitch.isChecked = filter.active
        }

        // Restore form selection
        filter.form_id?.let { formId ->
            val formIndex = forms.indexOfFirst { it.id == formId }
            if (formIndex != -1) {
                binding.formDropdown.setText(forms[formIndex].name, false)
            }
        }

        // Restore route selection
        filter.route_id?.let { routeId ->
            val routeIndex = routes.indexOfFirst { it.id == routeId }
            if (routeIndex != -1) {
                binding.routeDropdown.setText(routes[routeIndex].name, false)
            }
        }

        // Restore doctor selection
        filter.doctor_id?.let { doctorId ->
            val doctorIndex = doctors.indexOfFirst { it.id == doctorId }
            if (doctorIndex != -1) {
                binding.doctorDropdown.setText(doctors[doctorIndex].name, false)
            }
        }

        // Restore caregiver selection
        filter.caregiver_id?.let { caregiverId ->
            val caregiverIndex = caregivers.indexOfFirst { it.id == caregiverId }
            if (caregiverIndex != -1) {
                binding.caregiverDropdown.setText(caregivers[caregiverIndex].name, false)
            }
        }

        // Restore dates
        binding.startDateInput.setText(filter.start_date ?: "")
        binding.endDateInput.setText(filter.end_date ?: "")
    }

    private fun setupDatePickers() {
        val dateFormatter = DateTimeFormatter.ISO_DATE
        
        binding.startDateInput.setOnClickListener {
            showDatePicker { date ->
                binding.startDateInput.setText(date.format(dateFormatter))
            }
        }

        binding.endDateInput.setOnClickListener {
            showDatePicker { date ->
                binding.endDateInput.setText(date.format(dateFormatter))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val now = LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        ).show()
    }

    private fun setupButtons() {
        binding.applyFilterButton.setOnClickListener {
            val filter = FilterMedications(
                active = binding.activeSwitch.isChecked,
                doctor_id = doctors.find { it.name == binding.doctorDropdown.text.toString() }?.id,
                caregiver_id = caregivers.find { it.name == binding.caregiverDropdown.text.toString() }?.id,
                form_id = forms.find { it.name == binding.formDropdown.text.toString() }?.id,
                route_id = routes.find { it.name == binding.routeDropdown.text.toString() }?.id,
                start_date = binding.startDateInput.text?.toString()?.takeIf { it.isNotEmpty() },
                end_date = binding.endDateInput.text?.toString()?.takeIf { it.isNotEmpty() }
            )
            filterListener?.onFiltersApplied(filter)
            dismiss()
        }

        binding.clearFilterButton.setOnClickListener {
            binding.activeSwitch.isChecked = false
            binding.doctorDropdown.setText("", false)
            binding.caregiverDropdown.setText("", false)
            binding.formDropdown.setText("", false)
            binding.routeDropdown.setText("", false)
            binding.startDateInput.text?.clear()
            binding.endDateInput.text?.clear()
            
            filterListener?.onFiltersApplied(null)
            dismiss()
        }
    }

    fun setFilterListener(listener: FilterListener) {
        filterListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        BottomSheetUtils.setupBottomSheetDialog(this, dialog)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            forms: List<MedicationForm>,
            routes: List<MedicationRoute>,
            caregivers: List<CaregiverInfo>,
            doctors: List<DoctorInfo>,
            currentFilter: FilterMedications? = null
        ) = MedicationFilterBottomSheet(forms, routes, caregivers, doctors, currentFilter)
    }
} 