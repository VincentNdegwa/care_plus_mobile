package com.example.careplus.ui.medications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.databinding.FragmentEditMedicationBinding
import com.example.careplus.utils.SnackbarUtils

class EditMedicationFragment : Fragment() {
    private var _binding: FragmentEditMedicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditMedicationViewModel by viewModels()
    private val args: EditMedicationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInitialData()
        setupUpdateButton()
        observeUpdateResult()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setPageTitle("Edit Medication")
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupInitialData() {
        viewModel.setMedicationDetails(args.medicationDetails)
        viewModel.medicationDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { medication ->
                binding.apply {
                    medicationNameInput.setText(medication.medication_name)
                    dosageQuantityInput.setText(medication.dosage_quantity)
                    dosageStrengthInput.setText(medication.dosage_strength)
                    frequencyInput.setText(medication.frequency)
                    durationInput.setText(medication.duration)
                    stockInput.setText(medication.stock.toString())
                }
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error loading medication details")
            }
        }
    }

    private fun setupUpdateButton() {
        binding.updateButton.setOnClickListener {
            val updateData = collectFormData()
            viewModel.updateMedication(args.medicationId, updateData)
        }
    }

    private fun collectFormData(): MedicationUpdateRequest {
        return MedicationUpdateRequest(
            medication_name = binding.medicationNameInput.text.toString(),
            dosage_quantity = binding.dosageQuantityInput.text.toString(),
            dosage_strength = binding.dosageStrengthInput.text.toString(),
            frequency = binding.frequencyInput.text.toString(),
            duration = binding.durationInput.text.toString(),
            stock = binding.stockInput.text.toString().toIntOrNull() ?: 0
        )
    }

    private fun observeUpdateResult() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                SnackbarUtils.showSnackbar(binding.root, "Medication updated successfully", false)
                findNavController().navigateUp()
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error updating medication")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 