package com.example.careplus.ui.health_providers

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.careplus.databinding.FragmentFilterBottomSheetBinding
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var filterListener: FilterListener? = null

    interface FilterListener {
        fun onFiltersApplied(
            specialization: String?,
            clinicName: String?,
            agencyName: String?
        )
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
        setupFilterButton()
        setupClearButton()
    }

    private fun setupSpecializationDropdown() {
        val specializations = listOf(
            "All Specializations",
            "General Practice",
            "Pediatrics",
            "Cardiology",
            "Neurology",
            "Orthopedics",
            "Dermatology"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            specializations
        )
        
        binding.specializationDropdown.setAdapter(adapter)
    }

    private fun setupFilterButton() {
        binding.applyFilterButton.setOnClickListener {
            val specialization = binding.specializationDropdown.text.toString()
                .takeIf { it != "All Specializations" }
            val clinicName = binding.clinicInput.text.toString().takeIf { it.isNotBlank() }
            val agencyName = binding.agencyInput.text.toString().takeIf { it.isNotBlank() }

            filterListener?.onFiltersApplied(specialization, clinicName, agencyName)
            dismiss()
        }
    }

    private fun setupClearButton() {
        binding.clearFilterButton.setOnClickListener {
            // Clear all inputs
            binding.specializationDropdown.setText("", false)
            binding.clinicInput.text?.clear()
            binding.agencyInput.text?.clear()
            
            // Apply empty filters (effectively clearing them)
            filterListener?.onFiltersApplied(null, null, null)
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