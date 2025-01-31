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

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var filterListener: FilterListener? = null

    interface FilterListener {
        fun onFiltersApplied(filter: FilterCareProviders)
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
        setupSpecializationDropdown()
        setupButtons()
    }

    private fun setupSpecializationDropdown() {
        val specializations = arrayOf(
            "General Practice",
            "Cardiology",
            "Neurology",
            "Pediatrics",
            "Oncology",
            "Other"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            specializations
        )
        binding.specializationDropdown.setAdapter(adapter)
    }

    private fun setupButtons() {
        binding.applyFilterButton.setOnClickListener {
            val filter = FilterCareProviders(
                specialization = binding.specializationDropdown.text.toString().takeIf { it.isNotEmpty() },
                agency_name = binding.agencyInput.text.toString().takeIf { it.isNotEmpty() },
                role = null, // You can add a role dropdown if needed
                search = null // This will be handled by the search box in the main UI
            )
            
            filterListener?.onFiltersApplied(filter)
            dismiss()
        }

        binding.clearFilterButton.setOnClickListener {
            binding.specializationDropdown.setText("", false)
            binding.clinicInput.text?.clear()
            binding.agencyInput.text?.clear()
            
            // Apply empty filters (effectively clearing them)
            filterListener?.onFiltersApplied(FilterCareProviders())
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
        fun newInstance() = FilterBottomSheetFragment()
    }
} 