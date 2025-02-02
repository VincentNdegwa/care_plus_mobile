package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentNewMedicationBinding
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.careplus.data.model.*

class NewMedicationFragment : Fragment() {
    private var _binding: FragmentNewMedicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationsViewModel by viewModels()

    private var selectedForm: MedicationFormResource? = null
    private var selectedRoute: MedicationRouteResource? = null
    private var selectedUnit: MedicationUnitResource? = null

    private val durationUnits = listOf("hours", "days", "weeks", "months", "years")

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
        binding.toolbar.setPageTitle("New Medication")
        
        setupObservers()
        setupDurationUnitDropdown()
        setupSaveButton()
    }

    private fun setupObservers() {
        viewModel.forms.observe(viewLifecycleOwner) { result ->
            result.onSuccess { forms ->
                setupFormDropdown(forms)
            }.onFailure {
                SnackbarUtils.showSnackbar(binding.root, "Failed to load medication forms")
            }
        }

        viewModel.routes.observe(viewLifecycleOwner) { result ->
            result.onSuccess { routes ->
                setupRouteDropdown(routes)
            }.onFailure {
                SnackbarUtils.showSnackbar(binding.root, "Failed to load medication routes")
            }
        }

        viewModel.units.observe(viewLifecycleOwner) { result ->
            result.onSuccess { units ->
                setupUnitDropdown(units)
            }.onFailure {
                SnackbarUtils.showSnackbar(binding.root, "Failed to load medication units")
            }
        }

        viewModel.frequencies.observe(viewLifecycleOwner) { result ->
            result.onSuccess { frequencies ->
                setupFrequencyDropdown(frequencies)
            }.onFailure {
                SnackbarUtils.showSnackbar(binding.root, "Failed to load frequencies")
            }
        }

        viewModel.newMedicationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                SnackbarUtils.showSnackbar(binding.root, "Medication created successfully", false)
                findNavController().navigateUp()
            }.onFailure { error ->
                SnackbarUtils.showSnackbar(binding.root, error.message ?: "Failed to create medication")
            }
        }
    }

    private fun setupFormDropdown(forms: List<MedicationFormResource>) {
        val formAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            forms.map { it.name }
        )
        (binding.formInput as? AutoCompleteTextView)?.setAdapter(formAdapter)
        binding.formInput.setOnItemClickListener { _, _, position, _ ->
            selectedForm = forms[position]
        }
    }

    private fun setupRouteDropdown(routes: List<MedicationRouteResource>) {
        val routeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            routes.map { it.name }
        )
        (binding.routeInput as? AutoCompleteTextView)?.setAdapter(routeAdapter)
        binding.routeInput.setOnItemClickListener { _, _, position, _ ->
            selectedRoute = routes[position]
        }
    }

    private fun setupUnitDropdown(units: List<MedicationUnitResource>) {
        val unitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            units.map { it.name }
        )
        (binding.strengthUnitInput as? AutoCompleteTextView)?.setAdapter(unitAdapter)
        binding.strengthUnitInput.setOnItemClickListener { _, _, position, _ ->
            selectedUnit = units[position]
        }
    }

    private fun setupFrequencyDropdown(frequencies: List<MedicationFrequencyResource>) {
        val frequencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            frequencies.map { it.frequency }
        )
        (binding.frequencyInput as? AutoCompleteTextView)?.setAdapter(frequencyAdapter)
    }

    private fun setupDurationUnitDropdown() {
        val durationUnitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            durationUnits
        )
        (binding.durationUnitInput as? AutoCompleteTextView)?.setAdapter(durationUnitAdapter)
    }

    private fun setupSaveButton() {
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
        
        if (binding.dosageQuantityInput.text.isNullOrBlank()) {
            binding.dosageQuantityLayout.error = "Dosage quantity is required"
            isValid = false
        }
        
        if (binding.dosageStrengthInput.text.isNullOrBlank() || binding.strengthUnitInput.text.isNullOrBlank()) {
            binding.dosageStrengthLayout.error = "Dosage strength and unit are required"
            isValid = false
        }
        if (binding.strengthUnitInput.text.isNullOrBlank()){
            binding.strengthUnitLayout.error = "Dosage unit is required"
            isValid =false
        }

        if (binding.frequencyInput.text.isNullOrBlank()) {
            binding.frequencyLayout.error = "Frequency is required"
            isValid = false
        }
        
        if (
            (binding.durationInput.text.isNullOrBlank() && !binding.durationUnitInput.text.isNullOrBlank())
            ) {
            binding.durationLayout.error = "Duration is required when duration unit is set"
            isValid = false
        }
        if ((!binding.durationInput.text.isNullOrBlank() && binding.durationUnitInput.text.isNullOrBlank())){
            binding.durationUnitLayout.error = "Duration unit is required when duration is set"
            isValid = false
        }
        
        return isValid
    }

    private fun saveMedication() {
        val dosageStrength = "${binding.dosageStrengthInput.text} ${binding.strengthUnitInput.text}"
        val duration = "${binding.durationInput.text} ${binding.durationUnitInput.text}"
        val patientId = SessionManager(requireContext()).getUser()?.patient?.id

        if (patientId != null) {
            val newMed = CreateMedicationRequest(
                diagnosis_id = null,
                dosage_quantity = binding.dosageQuantityInput.text.toString(),
                dosage_strength = dosageStrength,
                duration = duration,
                form_id = selectedForm?.id,
                frequency = binding.frequencyInput.text.toString(),
                medication_name = binding.medicationNameInput.text.toString(),
                patient_id = patientId,
                stock = binding.stockInput.text.toString().toIntOrNull(),
                route_id = selectedUnit?.id
            )

            viewModel.createMedication(newMed).observe(viewLifecycleOwner) { result ->
                result.onSuccess { response ->
                    if (response.error){
                        showSnackbar(response.message)
                    }else{
                        showSnackbar(response.message, false)
                        findNavController().navigateUp()
                    }
                }.onFailure { exception ->
                    showSnackbar(exception.message ?: "Failed to create medication")
                }
            }
        } else {
            showSnackbar("Please login to continue")
        }
    }

    private fun showSnackbar(message: String, isError: Boolean = true) {
        SnackbarUtils.showSnackbar(
            view = binding.root,
            message = message,
            isError = isError
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 