package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.R
import com.example.careplus.data.model.Schedule
import com.example.careplus.databinding.FragmentMedicationReminderBinding
import com.example.careplus.utils.SnackbarUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MedicationReminderFragment : Fragment() {
    private var _binding: FragmentMedicationReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationDetailViewModel by viewModels()
    private var schedule: Schedule? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString("notification_data")?.let { jsonData ->
            try {
                schedule = Gson().fromJson(jsonData, Schedule::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing notification data", e)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
        observeResults()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setPageTitle("Medication Reminder")
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

        private fun setupUI() {
        schedule?.let { schedule ->
            binding.apply {
                toolbar.setNavigationOnClickListener { 
                    findNavController().navigateUp() 
                }

                medicationNameText.text = schedule.medication.medication_name
                dosageText.text = "${schedule.medication.dosage_quantity} ${schedule.medication.dosage_strength}"
                
                val utcDateTime = LocalDateTime.parse(schedule.dose_time.replace(" ", "T"))
                val localDateTime = utcDateTime
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault())
                
                scheduleTimeText.text = localDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

                statusChip.text = schedule.status
                val chipColor = when(schedule.status.lowercase()) {
                    "pending" -> R.color.primary
                    "taken" -> R.color.success
                    else -> R.color.error
                }
                statusChip.setChipBackgroundColorResource(chipColor)

                frequencyText.text = schedule.medication.frequency
                stockText.text = "Stock: ${schedule.medication.stock} units"

                val isPending = schedule.status.lowercase() == "pending"
                takeButton.isEnabled = isPending
                snoozeButton.isEnabled = isPending
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            takeButton.setOnClickListener {
                if (!isLoading) {
                    setLoading(true)
                    schedule?.let { viewModel.takeMedication(it.id) }
                }
            }

            snoozeButton.setOnClickListener {
                if (!isLoading) {
                    showSnoozeOptionsDialog()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.apply {
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            takeButton.isEnabled = !loading
            snoozeButton.isEnabled = !loading
        }
    }

    private fun showSnoozeOptionsDialog() {
        val options = arrayOf("5 minutes", "10 minutes", "15 minutes", "30 minutes")
        val minutes = arrayOf(5, 10, 15, 30)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Snooze for")
            .setItems(options) { _, which ->
                setLoading(true)
                schedule?.let { viewModel.snoozeMedication(it.id, minutes[which]) }
            }
            .show()
    }

    private fun observeResults() {
        viewModel.takeMedicationResult.observe(viewLifecycleOwner) { result ->
            setLoading(false)
            result?.onSuccess { response ->
                if (response.error) {
                    showMessage(response.message)
                } else {
                    showMessage(response.message, false)
                    findNavController().navigateUp()
                }
            }?.onFailure { exception ->
                showMessage(exception.message.toString())
            }
        }

        viewModel.snoozeMedicationResult.observe(viewLifecycleOwner) { result ->
            setLoading(false)
            result?.onSuccess { response ->
                if (response.error) {
                    showMessage(response.message)
                } else {
                    showMessage(response.message, false)
                    findNavController().navigateUp()
                }
            }?.onFailure { exception ->
                showMessage(exception.message.toString())
            }
        }
    }

    private fun showMessage(message: String, isError: Boolean = true) {
        SnackbarUtils.showSnackbar(binding.root, message, isError)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MedicationReminderFrag"
    }
} 