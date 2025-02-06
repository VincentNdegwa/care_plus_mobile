package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
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
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationDetailResponse

class MedicationDetailFragment : Fragment() {
    private var _binding: FragmentMedicationDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationDetailViewModel by viewModels()
    private val args: MedicationDetailFragmentArgs by navArgs()
    private lateinit var updatedMedicationDetails: MedicationDetails
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
        setupFabs()
        
        val medicationDetails = args.medicationDetails
        updatedMedicationDetails =medicationDetails
        displayMedicationDetails(updatedMedicationDetails)
        viewModel.setMedicationDetails(medicationDetails)

        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // Fetch the updated medication details when the fragment is resumed
        val medicationId = args.medicationDetails.id // Assuming you have the ID from the args
        viewModel.fetchMedicationDetails(medicationId)
        setupObservers()

    }
    private fun displayMedicationDetails(medication: MedicationDetails) {
        binding.apply {
            toolbar.setPageTitle(medication.medication_name ?: "Unknown Medication")
            dosageText.text = "${medication.dosage_quantity ?: "- - -"} ${medication.dosage_strength ?: "- - -"}"
            formText.text = medication.form?.name ?: "- - -"
            routeText.text = medication.route?.name ?: "- - -"
            frequencyText.text = medication.frequency ?: "- - -"
            durationText.text = medication.duration ?: "- - -"
            stockText.text = "${medication.stock ?: 0} units remaining"

            // Format prescribed date
            val prescribedDate = LocalDateTime.parse(
                medication.prescribed_date?.replace(" ", "T")
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
                diagnosisText.text = diagnosis.diagnosis_name
                diagnosisContainer.visibility = View.VISIBLE
            } ?: run {
                diagnosisContainer.visibility = View.GONE
            }
        }
    }

    private fun setupFabs() {
        binding.editFab.setOnClickListener {
            if (isFabExpanded) collapseFabs() else expandFabs()
        }

        binding.editDetailsFab.setOnClickListener {
            findNavController().navigate(
                MedicationDetailFragmentDirections.actionMedicationDetailToEdit(
                    args.medicationDetails.id.toInt(),
                    updatedMedicationDetails
                )
            )
        }

        binding.deleteFab.setOnClickListener {
            // Handle delete action
            collapseFabs()
        }

        binding.startMedicationFab.setOnClickListener {
            showScheduleDialog()
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

        binding.startMedicationFab.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setStartDelay(100)
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

        binding.startMedicationFab.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(200)
            .withEndAction {
                if (_binding != null) {
                    binding.startMedicationFab.visibility = View.GONE
                }
            }
            .start()
        
        binding.editFab.animate()
            .rotation(0f)
            .setDuration(200)
            .start()
    }

    private fun setupObservers() {
        viewModel.medication.observe(viewLifecycleOwner) { result ->
            result.onSuccess { medicationDetails ->
                displayMedicationDetails(updatedMedicationDetails)
                updatedMedicationDetails = medicationDetails
            }.onFailure { exception ->
                Log.e("MedicationDetailFragment", "Error fetching medication details", exception)
                SnackbarUtils.showSnackbar(binding.root, "Failed to load medication details")
            }
        }
    }

    private fun showScheduleDialog() {
        val scheduleViewModel: MedicationScheduleViewModel by viewModels()
        MedicationScheduleDialog(
            requireContext(),
            updatedMedicationDetails,
            scheduleViewModel,
            viewLifecycleOwner
        ) { shouldTakeMedication ->
            if (shouldTakeMedication) {
                // Navigate to take medication screen or show take medication dialog
//                showTakeMedicationDialog()
            }
        }.show()
    }

    override fun onDestroyView() {
        binding.editDetailsFab.animate().cancel()
        binding.deleteFab.animate().cancel()
        binding.editFab.animate().cancel()
        super.onDestroyView()
        _binding = null
    }
} 