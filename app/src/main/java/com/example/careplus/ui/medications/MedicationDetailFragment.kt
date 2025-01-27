package com.example.careplus.ui.medications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.careplus.databinding.FragmentMedicationDetailBinding
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicationDetailFragment : Fragment() {
    private var _binding: FragmentMedicationDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationDetailViewModel by viewModels()
    private val args: MedicationDetailFragmentArgs by navArgs()
    private var isFabExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        viewModel.fetchMedicationDetails(args.medicationId)
        setupFabs()
    }

    private fun setupObservers() {
        viewModel.medication.observe(viewLifecycleOwner) { result ->
            result.onSuccess { medication ->
                binding.apply {
                    toolbar.setPageTitle(medication.medication_name)
                    
                    dosageText.text = "${medication.dosage_quantity} ${medication.dosage_strength}"
                    formText.text = medication.form.name
                    routeText.text = medication.route.name
                    frequencyText.text = medication.frequency
                    durationText.text = medication.duration
                    stockText.text = "${medication.stock} units remaining"
                    
                    // Format prescribed date
                    val prescribedDate = LocalDateTime.parse(
                        medication.prescribed_date.replace(" ", "T")
                    ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    prescribedDateText.text = prescribedDate

                    // Show doctor info if available
                    medication.doctor?.let { doctor ->
                        doctorNameText.text = doctor.name
                        doctorContainer.visibility = View.VISIBLE
                    } ?: run {
                        doctorContainer.visibility = View.GONE
                    }

                    // Show diagnosis if available
                    medication.diagnosis?.let { diagnosis ->
                        diagnosisText.text = diagnosis
                        diagnosisContainer.visibility = View.VISIBLE
                    } ?: run {
                        diagnosisContainer.visibility = View.GONE
                    }

                    // Show route description
                    routeDescriptionText.text = medication.route.description
                }
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(
                    binding.root, 
                    exception.message ?: "Error loading medication details"
                )
            }
        }
    }

    private fun setupFabs() {
        binding.editFab.setOnClickListener {
            if (isFabExpanded) collapseFabs() else expandFabs()
        }
        
        binding.editDetailsFab.setOnClickListener {
            viewModel.medication.value?.getOrNull()?.let { medicationDetails ->
                val action = MedicationDetailFragmentDirections
                    .actionMedicationDetailToEdit(
                        medicationId = args.medicationId,
                        medicationDetails = medicationDetails
                    )
                findNavController().navigate(action)
                collapseFabs()
            }
        }
        
        binding.deleteFab.setOnClickListener {
            // Handle delete action
            collapseFabs()
        }
    }

    private fun expandFabs() {
        isFabExpanded = true
        
        binding.editDetailsFab.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .start()
        }
        
        binding.deleteFab.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setStartDelay(50)
                .start()
        }
        
        binding.editFab.animate()
            .rotation(45f)
            .setDuration(200)
            .start()
    }

    private fun collapseFabs() {
        if (_binding == null) return
        
        isFabExpanded = false
        
        binding.editDetailsFab.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(200)
            .withEndAction {
                if (_binding != null) {
                    binding.editDetailsFab.visibility = View.GONE
                }
            }
            .start()
        
        binding.deleteFab.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(200)
            .withEndAction {
                if (_binding != null) {
                    binding.deleteFab.visibility = View.GONE
                }
            }
            .start()
        
        binding.editFab.animate()
            .rotation(0f)
            .setDuration(200)
            .start()
    }

    override fun onDestroyView() {
        binding.editDetailsFab.animate().cancel()
        binding.deleteFab.animate().cancel()
        binding.editFab.animate().cancel()
        super.onDestroyView()
        _binding = null
    }
} 