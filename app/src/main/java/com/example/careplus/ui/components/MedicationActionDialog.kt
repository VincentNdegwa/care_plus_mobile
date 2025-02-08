package com.example.careplus.ui.components

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import com.example.careplus.R
import com.example.careplus.data.model.Schedule
import com.example.careplus.databinding.DialogMedicationActionBinding
import com.example.careplus.ui.home.HomeViewModel
import com.example.careplus.ui.medications.MedicationDetailViewModel
import com.example.careplus.utils.SnackbarUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MedicationActionDialog(
    context: Context,
    private val schedule: Schedule,
    private val viewModel: MedicationDetailViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val application:Application
) : Dialog(context) {

    private lateinit var binding: DialogMedicationActionBinding
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        binding = DialogMedicationActionBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Set dialog width to 90% of screen width
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Prevent dialog from being canceled by clicking outside
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        setupUI()
        setupClickListeners()
        observeResults()
    }

    private fun setupUI() {
        binding.apply {
            // Medication name and details
            medicationNameText.text = schedule.medication.medication_name
            dosageText.text = "${schedule.medication.dosage_quantity} ${schedule.medication.dosage_strength}"
            
            // Schedule time
            val utcDateTime = LocalDateTime.parse(schedule.dose_time.replace(" ", "T"))
            val localDateTime = utcDateTime
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
            
            scheduleTimeText.text = localDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

            // Status chip
            statusChip.text = schedule.status
            val chipColor = when(schedule.status.lowercase()) {
                "pending" -> R.color.primary
                "taken" -> R.color.success
                else -> R.color.error
            }
            statusChip.setChipBackgroundColorResource(chipColor)

            // Additional details
            frequencyText.text = schedule.medication.frequency
            stockText.text = "Stock: ${schedule.medication.stock} units"

            // Show/hide buttons based on status
            val isPending = schedule.status.lowercase() == "pending"
            takeButton.isEnabled = isPending
            snoozeButton.isEnabled = isPending
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            takeButton.setOnClickListener {
                if (!isLoading) {
                    setLoading(true)
                    viewModel.takeMedication(schedule.id)

                }
            }

            snoozeButton.setOnClickListener {
                if (!isLoading) {
                    showSnoozeOptionsDialog()
                }
            }

            cancelButton.setOnClickListener {
                if (!isLoading) {
                    dismiss()
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
            cancelButton.isEnabled = !loading
        }
    }

    private fun showSnoozeOptionsDialog() {
        val options = arrayOf("5 minutes", "10 minutes", "15 minutes", "30 minutes")
        val minutes = arrayOf(5, 10, 15, 30)

        MaterialAlertDialogBuilder(context)
            .setTitle("Snooze for")
            .setItems(options) { _, which ->
                setLoading(true)
                viewModel.snoozeMedication(schedule.id, minutes[which])
            }
            .show()
    }

    private fun observeResults() {
        viewModel.takeMedicationResult.observe(lifecycleOwner) { result ->
            setLoading(false)
            result?.onSuccess { result->
                if (result.error){
                    showMessage(result.message)
                }else{
                    showMessage(result.message, false)
                    dismiss()
                    val homeViewModel = HomeViewModel(application)
                    homeViewModel.updateSceduleFromTakenMed(result.data)
                }
            }?.onFailure { exception ->
                showMessage(exception.message.toString())
            }
        }

        viewModel.snoozeMedicationResult.observe(lifecycleOwner) { result ->
            setLoading(false)
            result?.onSuccess { result->
                if (result.error){
                    showMessage(result.message)
                }else{
                    showMessage(result.message, false)
                    dismiss()
                }
            }?.onFailure { exception ->
                showMessage(exception.message.toString())
            }
        }
    }
    fun showMessage(message:String, isError:Boolean= true){
       SnackbarUtils.showSnackbar(binding.root,message,isError)
    }
} 