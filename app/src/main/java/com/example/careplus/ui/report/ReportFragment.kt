package com.example.careplus.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentReportBinding
import com.example.careplus.data.model.report.MedicationVsSideEffectCountsRequest
import com.example.careplus.utils.SnackbarUtils
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import android.graphics.Color
import android.text.BoringLayout
import com.example.careplus.data.model.report.MedicationSideEffectCount
import java.lang.Error
import android.util.Log
import com.example.careplus.data.model.report.MedicalAdherenceReportRequest
import com.example.careplus.data.model.report.TopSideEffectsResponse
import com.example.careplus.data.model.report.MostMissedMedicationsResponse
import com.example.careplus.data.model.report.MedicalAdherenceReportResponse
import com.example.careplus.data.model.report.MedicationAdherence
import com.example.careplus.data.model.report.MedicationAdherenceByMedicationRequest
import com.example.careplus.data.model.report.MedicationAdherenceByMedicationResponse
import com.example.careplus.data.model.report.MissedMedication
import com.example.careplus.data.model.report.MostMissedMedicationsRequest
import com.example.careplus.data.model.report.TopSideEffect
import com.example.careplus.data.model.report.TopSideEffectsRequest

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportViewModel
    private lateinit var lineChart: LineChartView
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeComponents()
        setupUI()
        setupObservers()
        loadData()
    }

    private fun initializeComponents() {
        viewModel = ReportViewModel(requireActivity().application)
        sessionManager = SessionManager(requireContext())
        lineChart = binding.sideEffectsChart

        lineChart.lineChartData = LineChartData()
    }

    private fun setupUI() {
        setupChartConfiguration()
    }

    private fun setupChartConfiguration() {
        with(lineChart) {
            isViewportCalculationEnabled = true
            isValueSelectionEnabled = true
            isClickable = true
            isZoomEnabled = true
            isInteractive = true

            lecho.lib.hellocharts.model.Axis().apply {
                setName("Medication ID")
                textColor = Color.BLACK
            }
            lecho.lib.hellocharts.model.Axis().apply {
                setName("Side Effect Count")
                textColor = Color.BLACK
            }
        }
    }

    private fun setupObservers() {
        viewModel.medicationVsSideEffectCounts.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    Log.d("ReportFragment", "Data received: ${response.data}")
                    if (response.data.isEmpty()) {
                        showError("No data available", false)
                        return@onSuccess
                    }
                    updateMedicationSideEffectChart(response.data)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }

        viewModel.topSideEffects.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    updateTopSideEffectsChart(response.data)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }

        viewModel.mostMissedMedications.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    updateMostMissedMedicationsChart(response.data)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }

        viewModel.medicalAdherenceReport.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    updateAdherenceChart(response)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }

        viewModel.medicationAdherenceByMedication.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    updateAdherenceByMedicationChart(response.data)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }
    }

    private fun loadData() {
        sessionManager.getUser()?.patient?.id?.let { patientId ->
            fetchReports(patientId)
        } ?: run {
            showError("Patient ID not found")
        }
    }

    private fun fetchReports(patientId: Int) {
        val request = MedicationVsSideEffectCountsRequest(patient_id = patientId)
        viewModel.fetchMedicationVsSideEffectCounts(request)
        viewModel.fetchTopSideEffects(TopSideEffectsRequest(patient_id = patientId))
        viewModel.fetchMostMissedMedications(MostMissedMedicationsRequest(patient_id = patientId))
        viewModel.fetchMedicalAdherenceReport(MedicalAdherenceReportRequest(patient_id = patientId))
        viewModel.fetchMedicationAdherenceByMedication(MedicationAdherenceByMedicationRequest(patient_id = patientId))
    }

    private fun updateMedicationSideEffectChart(data: List<MedicationSideEffectCount>) {
        try {
            Log.d("ReportFragment", "Updating chart with ${data.size} data points")
            
            val values = data.mapIndexed { index, item -> 
                PointValue(index.toFloat(), item.side_effect_count.toFloat()).apply {
                    setLabel("${item.medication_name ?: "Med ${item.medication_id}"}: ${item.side_effect_count}")
                }
            }
            
            val line = Line(values).apply {
                setColor(Color.BLUE)
                setCubic(false) // Set to false for clearer data points
                setHasLabels(true)
                setHasPoints(true)
                setPointRadius(8) // Make points more visible
                setStrokeWidth(2) // Make line more visible
            }

            val chartData = LineChartData(listOf(line)).apply {
                // Configure axes
                axisXBottom = lecho.lib.hellocharts.model.Axis().apply {
                    setName("Medications")
                    textColor = Color.BLACK
                }
                axisYLeft = lecho.lib.hellocharts.model.Axis().apply {
                    setName("Side Effects")
                    textColor = Color.BLACK
                }
            }

            lineChart.apply {
                lineChartData = chartData
                // Adjust viewport to show all data points
                val maxY = data.maxOf { it.side_effect_count } + 1
                setViewportCalculationEnabled(false)
                maximumViewport = lecho.lib.hellocharts.model.Viewport(
                    -0.5f, maxY.toFloat(),
                    data.size - 0.5f, 0f
                )
                currentViewport = maximumViewport
                invalidate()
            }

            Log.d("ReportFragment", "Chart updated successfully")
        } catch (e: Exception) {
            Log.e("ReportFragment", "Error updating chart", e)
            showError("Error displaying chart: ${e.message}")
        }
    }

    private fun updateTopSideEffectsChart(data: List<TopSideEffect>) {
        // Implement logic to update the top side effects chart
        // Similar to the updateMedicationSideEffectChart method
    }

    private fun updateMostMissedMedicationsChart(data: List<MissedMedication>) {
        // Implement logic to update the most missed medications chart
        // Similar to the updateMedicationSideEffectChart method
    }

    private fun updateAdherenceChart(data: MedicalAdherenceReportResponse) {
        // Implement logic to update the adherence chart
        // Use PieChartView for this
    }

    private fun updateAdherenceByMedicationChart(data: List<MedicationAdherence>) {
        // Implement logic to update the adherence by medication chart
        // Similar to the updateMedicationSideEffectChart method
    }

    private fun showError(message: String, isError: Boolean = true) {
        Log.d("ReportFragment", "Showing message: $message (isError: $isError)")
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