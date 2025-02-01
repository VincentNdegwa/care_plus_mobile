package com.example.careplus.ui.health_providers

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.careplus.data.filter_model.FilterCareProviders
import com.example.careplus.databinding.FragmentFilterBottomSheetBinding
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment(
    private val currentFilter: FilterCareProviders? = null
) : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var filterListener: FilterListener? = null

    interface FilterListener {
        fun onFiltersApplied(filter: FilterCareProviders?)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        currentFilter?.let { restoreFilterState(it) }
    }

    private fun setupViews() {
        setupSpecializationDropdown()
        setupButtons()
    }

    private fun restoreFilterState(filter: FilterCareProviders) {
        // Restore all filter values
        binding.specializationDropdown.setText(filter.specialization)
        binding.agencyInput.setText(filter.agency_name)
        binding.roleDropdown.setText(filter.role,false)
        binding.genderDropDown.setText(filter.gender,false)
    }

    private fun setupSpecializationDropdown() {
        val specializations = arrayOf(
            "General Practice",
            "Cardiology",
            "Neurology",
            "Pediatrics",
            "Oncology"
        )
        val specializationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            specializations
        )

        val role = arrayOf("Doctor","Caregiver")
        val roleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            role
        )
        val genders = arrayOf("male", "female", "other")
        val genderAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genders
        )
        binding.genderDropDown.setAdapter(genderAdapter)
        binding.specializationDropdown.setAdapter(specializationAdapter)
        binding.roleDropdown.setAdapter(roleAdapter)
    }

    private fun setupButtons() {
        binding.applyFilterButton.setOnClickListener {
            val filter = FilterCareProviders(
                specialization = binding.specializationDropdown.text?.toString()?.takeIf { it.isNotEmpty() },
                agency_name = binding.agencyInput.text?.toString()?.takeIf { it.isNotEmpty() },
                role = binding.roleDropdown.text?.toString()?.takeIf { it.isNotEmpty() },
                gender = binding.genderDropDown.text?.toString()?.takeIf { it.isNotEmpty() }
            )
            filterListener?.onFiltersApplied(filter)
            dismiss()
        }

        binding.clearFilterButton?.setOnClickListener {
            binding.specializationDropdown.text?.clear()
            binding.genderDropDown.text?.clear()
            binding.agencyInput.text?.clear()
            binding.genderDropDown.text?.clear()
            
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
        fun newInstance(currentFilter: FilterCareProviders? = null) = 
            FilterBottomSheetFragment(currentFilter)
    }
} 