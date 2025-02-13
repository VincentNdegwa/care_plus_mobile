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
import com.example.careplus.data.model.side_effect.SideEffect
import com.example.careplus.databinding.FragmentEditSideEffectBinding
import com.example.careplus.data.model.side_effect.UpdateSideEffectRequest
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EditSideEffectFragment : Fragment() {
    private var _binding: FragmentEditSideEffectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SideEffectViewModel by viewModels()
    private val args: EditSideEffectFragmentArgs by navArgs()
    private lateinit var sideEffect: SideEffect;
    private var isProcessing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSideEffectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        loadSideEffect()
    }

    private fun setupViews() {
        binding.toolbar.setPageTitle("Edit Side Effect")
        
        // Setup severity dropdown
        val severities = arrayOf("Mild", "Moderate", "Severe")
        val severityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            severities
        )
        (binding.severityInput as? AutoCompleteTextView)?.setAdapter(severityAdapter)

        binding.updateButton.setOnClickListener {
            if (!isProcessing && validateInput()) {
                updateSideEffect()
            }
        }
    }

    private fun loadSideEffect() {
        sideEffect = args.sideEffect
        binding.apply {
            sideEffectInput.setText(sideEffect.side_effect)
            severityInput.setText(sideEffect.severity, false)
            durationInput.setText(sideEffect.duration?.toString() ?: "")
            notesInput.setText(sideEffect.notes ?: "")
        }
    }

    private fun observeViewModel() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar("Side effect updated successfully", false)
                findNavController().navigateUp()
            }.onFailure { exception ->
                isProcessing = false
                updateButtonState(isLoading = false)
                showSnackbar(exception.message ?: "Failed to update side effect")
            }
        }
    }

    private fun updateSideEffect() {
        isProcessing = true
        updateButtonState(isLoading = true)
        
        val request = UpdateSideEffectRequest(
            side_effect = binding.sideEffectInput.text.toString(),
            severity = binding.severityInput.text.toString(),
            duration = binding.durationInput.text.toString().toIntOrNull(),
            notes = binding.notesInput.text.toString().takeIf { it.isNotEmpty() }
        )
        
        viewModel.updateSideEffect(sideEffect.id, request)
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

    private fun updateButtonState(isLoading: Boolean) {
        binding.updateButton.apply {
            isEnabled = !isLoading
            text = if (isLoading) "Updating..." else "Update Side Effect"
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