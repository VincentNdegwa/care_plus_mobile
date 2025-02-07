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
import com.example.careplus.R
import com.example.careplus.databinding.FragmentMedicationDetailBinding
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.careplus.data.model.MedicationDetails
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        

        val medicationDetails = args.medicationDetails
        updatedMedicationDetails = medicationDetails
        displayMedicationDetails(updatedMedicationDetails)
        viewModel.setMedicationDetails(medicationDetails)

        setupToolbar()
        updateMenuItems()
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

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.medication_detail_menu)
//        binding.toolbar.setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_take_medication -> {
                    showTakeMedicationDialog()
                    true
                }
                R.id.action_snooze_medication -> {
                    showSnoozeMedicationDialog()
                    true
                }
                R.id.action_stop_medication -> {
                    showStopMedicationDialog()
                    true
                }
                R.id.action_resume_medication -> {
                    showResumeMedicationDialog()
                    true
                }
                R.id.action_edit_medication -> {
                    navigateToEdit()
                    true
                }
                R.id.action_delete_medication -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateMenuItems() {
        val menu = binding.toolbar.menu
        val isMedicationActive = updatedMedicationDetails.active == 1
        
        // Show/hide menu items based on conditions
        menu.findItem(R.id.action_take_medication)?.isVisible = isMedicationActive
        menu.findItem(R.id.action_snooze_medication)?.isVisible = isMedicationActive
        menu.findItem(R.id.action_stop_medication)?.isVisible = isMedicationActive
        menu.findItem(R.id.action_resume_medication)?.isVisible = !isMedicationActive
    }

    private fun showTakeMedicationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Take Medication")
            .setMessage("Mark this medication as taken?")
            .setPositiveButton("Take") { _, _ ->
                viewModel.takeMedication(updatedMedicationDetails.id.toInt())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSnoozeMedicationDialog() {
        // Show dialog with time picker or predefined intervals
        // Then call viewModel.snoozeMedication()
    }

    private fun showStopMedicationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Stop Medication")
            .setMessage("Are you sure you want to stop this medication schedule?")
            .setPositiveButton("Stop") { _, _ ->
                viewModel.stopMedication(updatedMedicationDetails.id.toInt())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResumeMedicationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resume Medication")
            .setMessage("Do you want to extend the schedule duration?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.resumeMedication(updatedMedicationDetails.id.toInt(), true)
            }
            .setNegativeButton("No") { _, _ ->
                viewModel.resumeMedication(updatedMedicationDetails.id.toInt(), false)
            }
            .show()
    }

    private fun navigateToEdit() {
        findNavController().navigate(
            MedicationDetailFragmentDirections.actionMedicationDetailToEdit(
                args.medicationDetails.id.toInt(),
                updatedMedicationDetails
            )
        )
    }

    private fun showDeleteConfirmation() {
        // Implement the logic to show a confirmation dialog before deleting
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

        viewModel.takeMedicationResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess { response ->
                SnackbarUtils.showSnackbar(binding.root, response.message, false)
                viewModel.fetchMedicationDetails(updatedMedicationDetails.id)
            }?.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Failed to take medication")
            }
        }

        // Add similar observers for stop, snooze, and resume results
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 