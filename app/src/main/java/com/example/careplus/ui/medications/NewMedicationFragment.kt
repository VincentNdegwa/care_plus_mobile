package com.example.careplus.ui.medications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.databinding.FragmentNewMedicationBinding
import com.example.careplus.utils.SnackbarUtils

class NewMedicationFragment : Fragment() {
    private var _binding: FragmentNewMedicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setPageTitle("New Medication")
    }

    private fun setupListeners() {
        binding.saveMedicationButton.setOnClickListener {
            if (validateInputs()) {
                saveMedication()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (binding.medicationNameInput.text.isNullOrBlank()) {
            binding.medicationNameLayout.error = "Medication name is required"
            isValid = false
        }
        
        // Add other validations as needed
        
        return isValid
    }

    private fun saveMedication() {
        // TODO: Implement save medication logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 