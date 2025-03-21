package com.example.careplus.ui.side_effect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.careplus.R
import com.example.careplus.databinding.FragmentSideEffectDetailsBinding
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SideEffectDetailsFragment : Fragment() {
    private var _binding: FragmentSideEffectDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SideEffectViewModel by viewModels()
    private val args: SideEffectDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSideEffectDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        updateUi()
    }

    private fun updateUi() {
        val sideEffect = args.sideEffect
        binding.apply {
            sideEffectText.text = sideEffect.side_effect

            val severityColor = when(sideEffect.severity.lowercase()) {
                "mild" -> R.color.success
                "moderate" -> R.color.warning
                "severe" -> R.color.error
                else -> R.color.primary
            }

            severityIndicator.setBackgroundColor(
                ContextCompat.getColor(requireContext(), severityColor)
            )

            severityChip.apply {
                text = sideEffect.severity
                setChipBackgroundColorResource(severityColor)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            sideEffect.medication?.let { medication ->
                medicationNameText.text = medication.medication_name
                medicationDosageText.text = buildString {
                    append(medication.dosage_quantity ?: "")
                    if (!medication.dosage_strength.isNullOrEmpty()) {
                        append(" ")
                        append(medication.dosage_strength)
                    }
                }
            }

            dateTimeText.text = formatDateTime(sideEffect.datetime)
            durationText.text = "${sideEffect.duration} hours"
            notesText.text = sideEffect.notes ?: "No notes added"
        }
    }

    private fun setupViews() {
        binding.toolbar.setPageTitle("Side Effect Details")
        binding.editButton.setOnClickListener {
            findNavController().navigate(
                SideEffectDetailsFragmentDirections.actionSideEffectDetailsToEdit(args.sideEffect)
            )
        }
    }


    private fun formatDateTime(dateTime: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            
            val localDateTime = LocalDateTime.parse(dateTime, inputFormatter)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
            
            localDateTime.format(outputFormatter)
        } catch (e: Exception) {
            dateTime
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