package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.R
import com.example.careplus.databinding.FragmentMedicationDetailBinding
import com.example.careplus.databinding.ItemSideEffectBinding
import com.example.careplus.utils.SnackbarUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.report.MedicationProgressResponse
import com.example.careplus.data.model.side_effect.FetchSideEffectsResponse
import com.example.careplus.data.model.side_effect.SideEffect
import com.example.careplus.ui.side_effect.SideEffectAdapter
import com.example.careplus.ui.side_effect.SideEffectsFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue

class MedicationDetailFragment : Fragment() {
    private var _binding: FragmentMedicationDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationDetailViewModel by viewModels()
    private val args: MedicationDetailFragmentArgs by navArgs()
    private lateinit var updatedMedicationDetails: MedicationDetails
    private var isFabExpanded = false
    private lateinit var sideEffectsAdapter: SideEffectAdapter

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
        
        // Display default charts initially
        displayDefaultCharts()
        
        viewModel.setMedicationDetails(medicationDetails)

        setupToolbar()
        updateMenuItems()
        setupObservers()
        binding.sideEffectsRecyclerView.adapter = sideEffectsAdapter
    }

    override fun onResume() {
        super.onResume()
        val medicationId = args.medicationDetails.id
        viewModel.fetchMedicationDetails(medicationId)
    }

    private fun displayDefaultCharts() {
        // Using empty state for charts initially
        val defaultData = MedicationProgressResponse(
            progress = 0,
            total_schedules = 1,
            completed_schedules = 0,
            taken_schedules = 0
        )
        displayProgressCharts(defaultData)
        
        // Show loading indicators
        binding.apply {
            progressLoadingIndicator.isVisible = true
            adherenceLoadingIndicator.isVisible = true
            adherenceChart.isVisible = false
            stockUsageChart.isVisible = false
        }
    }

    private fun displayProgressCharts(response: MedicationProgressResponse) {

        binding.apply {
            // Hide loading indicators and show charts
            progressLoadingIndicator.isVisible = false
            adherenceLoadingIndicator.isVisible = false
            adherenceChart.isVisible = true
            stockUsageChart.isVisible = true

            // Update progress labels
            progressLabel.text = "Overall Progress"
            adherenceLabel.text = "Medication Adherence"
            
            // Update schedule counts
            progressDescription.text = "${response.completed_schedules} of ${response.total_schedules} schedules completed"
            adherenceDescription.text = "${response.taken_schedules} of ${response.completed_schedules} medications taken"
        }

        // Progress Chart - shows overall progress
        displayPieChart(
            chartView = binding.adherenceChart,
            percentage = response.progress.toFloat(),
            percentageTextView = binding.adherencePercentageText,
            primaryColor = R.color.success,
            secondaryColor = R.color.error,
            label = "Progress"
        )

        // Adherence Chart - shows taken vs scheduled ratio
        val adherencePercentage = if (response.completed_schedules > 0) {
            (response.taken_schedules.toFloat() / response.completed_schedules.toFloat()) * 100
        } else {
            0f
        }

        displayPieChart(
            chartView = binding.stockUsageChart,
            percentage = adherencePercentage,
            percentageTextView = binding.stockUsagePercentageText,
            primaryColor = R.color.primary,
            secondaryColor = R.color.warning,
            label = "Adherence"
        )

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

            // Set status chip color and text based on status
            statusChip.apply {
                text = medication.status
                setChipBackgroundColorResource(when(medication.status?.lowercase()) {
                    "running" -> R.color.success
                    "stopped" -> R.color.warning
                    "expired" -> R.color.error
                    else -> R.color.primary
                })
            }

            // Update menu visibility based on status
            binding.toolbar.menu?.apply {
//                findItem(R.id.action_take_medication)?.isVisible = medication.status.equals("running", ignoreCase = true)
                findItem(R.id.action_stop_medication)?.isVisible = medication.status.equals("running", ignoreCase = true)
                findItem(R.id.action_resume_medication)?.isVisible = medication.status.equals("stopped", ignoreCase = true)
                findItem(R.id.action_restart_expired_schedule)?.isVisible = medication.status.equals("expired", ignoreCase = true)
            }
        }
    }

    private fun displayPieChart(
        chartView: lecho.lib.hellocharts.view.PieChartView,
        percentage: Float,
        percentageTextView: android.widget.TextView,
        primaryColor: Int,
        secondaryColor: Int,
        label: String
    ) {
        val safePercentage = percentage.coerceIn(0f, 100f)
        val remainingPercentage = 100f - safePercentage

        // Update percentage text with label
        percentageTextView.text = "${safePercentage.toInt()}%"

        // Get theme-aware colors
        val primaryThemeColor = resources.getColor(primaryColor)
        val secondaryThemeColor = resources.getColor(secondaryColor)

        val values = mutableListOf<SliceValue>().apply {
            // Primary slice (progress)
            add(SliceValue(safePercentage).apply {
                color = primaryThemeColor
                setLabel("") // No label needed
            })
            // Secondary slice (remaining)
            add(SliceValue(remainingPercentage).apply {
                color = secondaryThemeColor
                setLabel("") // No label needed
            })
        }

        val pieChartData = PieChartData(values).apply {
            setHasLabels(false)
            setHasCenterCircle(true)
            centerCircleScale = 0.85f // Slightly larger center circle

            // Use theme background color for center circle
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorBackgroundFloating, typedValue, true)
            centerCircleColor = typedValue.data
        }

        // Configure chart
        chartView.apply {
            this.pieChartData = pieChartData
            isClickable = false
            isValueSelectionEnabled = false
            circleFillRatio = 0.9f // Slightly thicker chart
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
                R.id.action_register_side_effect ->{
                    navigateToSideEffectForm()
                    true
                }
                R.id.action_restart_expired_schedule->{
                    showRestartDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showRestartDialog() {
        MedicationScheduleDialog(
            context = requireContext(),
            medicationDetails = updatedMedicationDetails,
            viewModel = MedicationScheduleViewModel(requireActivity().application),
            lifecycleOwner = viewLifecycleOwner,
            onError = { message ->
                showSnackbar(message)
            },
            onScheduleCreated = { shouldTakeNow ->
                if (shouldTakeNow) {
                    viewModel.takeNow(updatedMedicationDetails.id.toInt())
                } else {
                    showSnackbar("Schedule created successfully", false)
                }
                true
            }
        ).show()
    }


    private fun showSnackbar(message: String, isError: Boolean = true) {
        SnackbarUtils.showSnackbar(
            view = binding.root,
            message = message,
            isError = isError
        )
    }

    private fun updateMenuItems() {
        val menu = binding.toolbar.menu
        val isMedicationActive = updatedMedicationDetails.active == 1
        val medicationStatus = updatedMedicationDetails.status
        
        // Show/hide menu items based on conditions
//        menu.findItem(R.id.action_take_medication)?.isVisible = isMedicationActive
//        menu.findItem(R.id.action_snooze_medication)?.isVisible = isMedicationActive
        menu.findItem(R.id.action_stop_medication)?.isVisible = isMedicationActive
        medicationStatus.let { status->
            if (status == "Expired"){
                menu.findItem(R.id.action_restart_expired_schedule).isVisible = true
            }else if(status == "Stopped"){
                menu.findItem(R.id.action_resume_medication)?.isVisible = true
            }
        }
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
    private fun navigateToSideEffectForm(){
        findNavController().navigate(
            MedicationDetailFragmentDirections.actionMedicationDetailToCreateSideEffect(
                medicationId = args.medicationDetails.id.toInt()
            )
        )
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Medication")
            .setMessage("Are you sure want to delete this medication?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteMedication(updatedMedicationDetails.id.toInt())
            }
            .setNegativeButton("No") { _, _ ->
            }
            .show()
    }

    private fun setupObservers() {
        viewModel.medication.observe(viewLifecycleOwner) { result ->
            result.onSuccess { medication ->
                updatedMedicationDetails = medication
                displayMedicationDetails(medication)
            }.onFailure { exception ->
                showSnackbar(exception.message ?: "Failed to fetch medication details")
            }
        }

        viewModel.medicationProgress.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                displayProgressCharts(response)
            }.onFailure { exception ->
                showSnackbar(exception.message ?: "Failed to fetch progress data")
                displayProgressCharts(MedicationProgressResponse(
                    progress = 0,
                    total_schedules = 1,
                    completed_schedules = 0,
                    taken_schedules = 0
                ))
            }
        }

        viewModel.takeMedicationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.error) {
                    showSnackbar(response.message)
                } else {
                    showSnackbar("Medication taken successfully")
                    viewModel.fetchMedicationDetails(args.medicationDetails.id)
                }
            }.onFailure { exception ->
                showSnackbar(exception.message ?: "Failed to take medication")
            }
        }
        viewModel.deleteMedicationResult.observe(viewLifecycleOwner){ result->
            result?.onSuccess { res->
                if (res.error){
                    SnackbarUtils.showSnackbar(binding.root, res.message)
                }else{
                    SnackbarUtils.showSnackbar(binding.root, res.message, false)
                    findNavController().navigateUp()
                }

            }?.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message.toString())
            }

        }

        viewModel.stopMedicationResult.observe(viewLifecycleOwner){ result->
            result?.onSuccess { res->
                if (res.error){
                    SnackbarUtils.showSnackbar(binding.root, res.message)
                }else{
                    var updated = updatedMedicationDetails
                    updated.status = "Stopped"
                    displayMedicationDetails(updated)
                    SnackbarUtils.showSnackbar(binding.root, res.message, false)
                }

            }?.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message.toString())
            }
        }
        viewModel.resumeMedicationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { res ->
                if (res.error) {
                    SnackbarUtils.showSnackbar(binding.root, res.message)
                } else {
                    var updated = updatedMedicationDetails
                    updated.status = "Running"
                    displayMedicationDetails(updated)
                    SnackbarUtils.showSnackbar(binding.root, res.message, false)
                }
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message.toString())
                // Use regex to match "No medication tracker found" with case insensitivity
                if (exception.message?.matches(Regex("(?i).*no\\s+medication\\s+tracker\\s+found.*")) == true) {
                    showSchedule()
                }
            }
        }

        viewModel.takeNowResult.observe(viewLifecycleOwner){result->
            result?.onSuccess { res->
                if (res.error){
                    SnackbarUtils.showSnackbar(binding.root, res.message)
                }else{
                    SnackbarUtils.showSnackbar(binding.root, res.message, false)
                }

            }?.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message.toString())
            }
        }

        viewModel.sideEffects.observe(viewLifecycleOwner){
            results->
            results.onSuccess { res->
                showSideEffectsInUi(res)
            }
        }

    }

    private fun showSideEffectsInUi(res: FetchSideEffectsResponse) {
        val data:List<SideEffect> = res.data
        binding.sideEffectsCard.visibility = if (data.isEmpty()) View.GONE else View.VISIBLE

        sideEffectsAdapter = SideEffectAdapter(
            onItemClick = { sideEffect ->
                findNavController().navigate(
                    SideEffectsFragmentDirections.actionSideEffectsToDetails(sideEffect)
                )
            },
            onEditClick = { sideEffect ->
                findNavController().navigate(
                    SideEffectsFragmentDirections.actionSideEffectsToEdit(sideEffect)
                )
            },
            onDeleteClick = { sideEffect ->
//                showDeleteConfirmationDialog(sideEffect)
            }
        )
        sideEffectsAdapter.submitList(data)

        binding.sideEffectsRecyclerView.apply {
            adapter = sideEffectsAdapter
            layoutManager = LinearLayoutManager(context)

        }
    }

    private fun showSchedule(){
        MedicationScheduleDialog(
            context = requireContext(),
            medicationDetails = updatedMedicationDetails,
            viewModel = MedicationScheduleViewModel(requireActivity().application),
            lifecycleOwner = viewLifecycleOwner,
            onError = { errorMessage ->
                SnackbarUtils.showSnackbar(binding.root, errorMessage)
            },
            onScheduleCreated = { shouldTakeNow ->
                if (shouldTakeNow) {
                    viewModel.takeNow(updatedMedicationDetails.id.toInt())
                } else {
                    showSnackbar("Schedule created successfully", false)
                }
                true
            }
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.medication_detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_restart_expired_schedule -> {
                showRestartDialog()
                true
            }
            // ... other menu items ...
            else -> super.onOptionsItemSelected(item)
        }
    }

}