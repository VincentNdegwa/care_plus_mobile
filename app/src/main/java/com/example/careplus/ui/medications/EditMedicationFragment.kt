package com.example.careplus.ui.medications

import android.os.Bundle
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
        
        // Set the initial medication details from navigation args
        viewModel.setMedicationDetails(args.medicationDetails)
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
        viewModel.medicationDetails.observe(viewLifecycleOwner) { result: Result<MedicationDetails> ->
            result.onSuccess { medication ->
                populateFields(medication)
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
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            frequencies.map { it.frequency }
        )
        
        (binding.frequencyInput as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                selectedFrequency = frequencies[position]
                setText(frequencies[position].frequency)
            }
        }
    }

    private fun populateFields(medication: MedicationDetails) {
        binding.apply {
            medicationNameInput.setText(medication.medication_name)
            dosageQuantityInput.setText(medication.dosage_quantity)
            dosageStrengthInput.setText(medication.dosage_strength)
            formInput.setText(medication.form.name)
            routeInput.setText(medication.route.name)
            frequencyInput.setText(medication.frequency)
            durationInput.setText(medication.duration)
            stockInput.setText(medication.stock.toString())
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
            form_id = selectedForm?.id ?: args.medicationDetails.form.id,
            route_id = selectedRoute?.id ?: args.medicationDetails.route.id,
            frequency = selectedFrequency?.frequency ?: binding.frequencyInput.text.toString(),
            duration = binding.durationInput.text.toString(),
            stock = binding.stockInput.text.toString().toIntOrNull() ?: 0
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 