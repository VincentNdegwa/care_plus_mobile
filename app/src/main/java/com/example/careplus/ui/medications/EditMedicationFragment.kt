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
import androidx.navigation.fragment.navArgs
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationFrequencyResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.model.MedicationUnitResource
import com.example.careplus.databinding.FragmentEditMedicationBinding
import com.example.careplus.utils.SnackbarUtils

class EditMedicationFragment : Fragment() {
    private var _binding: FragmentEditMedicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditMedicationViewModel by viewModels()
    private val args: EditMedicationFragmentArgs by navArgs()

    private var selectedForm: MedicationFormResource? = null
    private var selectedRoute: MedicationRouteResource? = null
    private var selectedFrequency: MedicationFrequencyResource? = null
    private var selectedUnit: MedicationUnitResource? = null

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
        setupObservers()
        setupUpdateButton()

        // Log the received medication details
        Log.d("EditMedicationFragment", "Received medication details: ${args.medicationDetails}")

        // Check if medication details are valid
        if (args.medicationDetails != null) {
            viewModel.setMedicationDetails(args.medicationDetails)
        } else {
            Log.e("EditMedicationFragment", "Received medication details are null")
            // Handle the error case, e.g., show a message to the user
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setPageTitle("Edit Medication")
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupObservers() {
        // Observe medication details
        viewModel.medicationDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { details ->
                binding.apply {
                    medicationNameInput.setText(details.medication_name)
                    dosageQuantityInput.setText(details.dosage_quantity)
                    
                    // Split dosage strength into value and unit
                    val strengthPattern = "(\\d+(?:\\.\\d+)?)(\\s*\\w+)".toRegex()
                    val matchResult = details?.dosage_strength?.let { strengthPattern.find(it) }
                    
                    if (matchResult != null) {
                        val (value, unit) = matchResult.destructured
                        dosageStrengthValueInput.setText(value)
                        dosageStrengthUnitInput.setText(unit.trim())
                    }

                    durationInput.setText(details.duration)
                    stockInput.setText(details.stock.toString())
                    
                    formInput.setText(details.form?.name)
                    routeInput.setText(details.route?.name)
                    frequencyInput.setText(details.frequency)
                }
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error loading medication details")
            }
        }

        // Observe forms
        viewModel.forms.observe(viewLifecycleOwner) { result: Result<List<MedicationFormResource>> ->
            result.onSuccess { forms ->
                setupFormDropdown(forms)
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, "Error loading medication forms")
            }
        }

        // Observe routes
        viewModel.routes.observe(viewLifecycleOwner) { result: Result<List<MedicationRouteResource>> ->
            result.onSuccess { routes ->
                setupRouteDropdown(routes)
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, "Error loading medication routes")
            }
        }

        // Observe frequencies
        viewModel.frequencies.observe(viewLifecycleOwner) { result: Result<List<MedicationFrequencyResource>> ->
            result.onSuccess { frequencies ->
                setupFrequencyDropdown(frequencies)
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, "Error loading medication frequencies")
            }
        }

        //Observe units
        viewModel.units.observe(viewLifecycleOwner){ result: Result<List<MedicationUnitResource>>->
            result.onSuccess { units->
                setupUnitDropdown(units)
            }.onFailure {
                SnackbarUtils.showSnackbar(binding.root, "Error loading medication units")
            }

        }

        // Observe update result
        viewModel.updateResult.observe(viewLifecycleOwner) { result: Result<Unit> ->
            result.onSuccess {
                SnackbarUtils.showSnackbar(binding.root, "Medication updated successfully", false)
                findNavController().navigateUp()
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error updating medication")
            }
        }
    }

    private fun setupFormDropdown(forms: List<MedicationFormResource>) {
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            forms.map { it.name }
        )
        
        (binding.formInput as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                selectedForm = forms[position]
            }
        }
    }

    private fun setupRouteDropdown(routes: List<MedicationRouteResource>) {
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            routes.map { it.name }
        )
        
        (binding.routeInput as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                selectedRoute = routes[position]
            }
        }
    }

    private fun setupFrequencyDropdown(frequencies: List<MedicationFrequencyResource>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            frequencies.map { it.frequency }
        )
        
        (binding.frequencyInput as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                selectedFrequency = frequencies[position]
            }
        }
    }

    private fun setupUnitDropdown(units: List<MedicationUnitResource>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            units.map { it.name }
        )
        binding.dosageStrengthUnitInput.setAdapter(adapter)
    }

    private fun setupUpdateButton() {
        binding.updateButton.setOnClickListener {
            if (validateAndUpdateMedication()) {
                // No need to collect data again, as the viewModel handles the update
            }
        }
    }

    private fun validateAndUpdateMedication(): Boolean {
        var isValid = true

        if (binding.medicationNameInput.text.isNullOrBlank()) {
            binding.medicationNameLayout.error = "Please enter medication name"
            isValid = false
        } else {
            binding.medicationNameLayout.error = null
        }

        if (binding.dosageQuantityInput.text.isNullOrBlank()) {
            binding.dosageQuantityLayout.error = "Please enter dosage quantity"
            isValid = false
        } else {
            binding.dosageQuantityLayout.error = null
        }

        if (binding.dosageStrengthValueInput.text.isNullOrBlank()) {
            binding.dosageStrengthValueLayout.error = "Please enter strength value"
            isValid = false
        } else {
            binding.dosageStrengthValueLayout.error = null
        }

        if (binding.dosageStrengthUnitInput.text.isNullOrBlank()) {
            binding.dosageStrengthUnitLayout.error = "Please select unit"
            isValid = false
        } else {
            binding.dosageStrengthUnitLayout.error = null
        }

        if (isValid) {
            val strengthValue = binding.dosageStrengthValueInput.text.toString()
            val strengthUnit = binding.dosageStrengthUnitInput.text.toString()
            val combinedStrength = "$strengthValue $strengthUnit"

            viewModel.updateMedication(
                args.medicationId,
                MedicationUpdateRequest(
                    medication_name = binding.medicationNameInput.text.toString(),
                    dosage_quantity = binding.dosageQuantityInput.text.toString(),
                    dosage_strength = combinedStrength,
                    form_id = selectedForm?.id ?: 0,
                    route_id = selectedRoute?.id ?: 0,
                    frequency = binding.frequencyInput.text.toString(),
                    duration = binding.durationInput.text.toString(),
                    stock = binding.stockInput.text.toString().toIntOrNull() ?: 0
                )
            )

            return true
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 