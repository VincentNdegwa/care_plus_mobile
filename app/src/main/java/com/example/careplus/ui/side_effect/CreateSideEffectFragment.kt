package com.example.careplus.ui.side_effect

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
import com.example.careplus.databinding.FragmentCreateSideEffectBinding
import com.example.careplus.data.model.side_effect.CreateSideEffectRequest
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreateSideEffectFragment : Fragment() {
    private var _binding: FragmentCreateSideEffectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SideEffectViewModel by viewModels()
    private val args: CreateSideEffectFragmentArgs by navArgs()
    private var isProcessing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateSideEffectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.toolbar.setPageTitle("Register Side Effect")
        
        // Setup severity dropdown
        val severities = arrayOf("Mild", "Moderate", "Severe")
        val severityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            severities
        )
        (binding.severityInput as? AutoCompleteTextView)?.setAdapter(severityAdapter)

        binding.createButton.setOnClickListener {
            if (!isProcessing && validateInput()) {
                createSideEffect()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.createResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { res ->
                updateButtonState(isLoading = true)
                showSnackbar("Side effect registered successfully", false)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    findNavController().navigateUp()
                }
            }.onFailure { exception ->
                isProcessing = false
                updateButtonState(isLoading = false)
                showSnackbar(exception.message ?: "Failed to register side effect")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            isProcessing = isLoading
//            updateButtonState(isLoading = isLoading)
        }
    }

    private fun createSideEffect() {
        isProcessing = true
        updateButtonState(isLoading = true)
        val request = CreateSideEffectRequest(
            medication_id = args.medicationId,
            datetime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME),
            side_effect = binding.sideEffectInput.text.toString(),
            severity = binding.severityInput.text.toString(),
            duration = binding.durationInput.text.toString().toIntOrNull(),
            notes = binding.notesInput.text.toString().takeIf { it.isNotEmpty() }
        )
        viewModel.createSideEffect(request)
    }

    private fun updateButtonState(isLoading: Boolean, text: String? = null) {
        binding.createButton.apply {
            isEnabled = !isLoading
            this.text = text ?: if (isLoading) "Creating..." else "Create"
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (binding.sideEffectInput.text.isNullOrBlank()) {
            binding.sideEffectLayout.error = "Please enter side effect"
            isValid = false
        }

        if (binding.severityInput.text.isNullOrBlank()) {
            binding.severityLayout.error = "Please select severity"
            isValid = false
        }

        return isValid
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