package com.example.careplus.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentReportBinding
import com.example.careplus.utils.SnackbarUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.model.report.MedicalAdherenceReportRequest
import com.example.careplus.data.model.report.MedicalAdherenceReportResponse

import com.example.careplus.data.model.report.MissedMedication
import com.example.careplus.data.model.report.MostMissedMedicationsRequest
import com.example.careplus.data.model.report.TopSideEffect
import com.example.careplus.data.model.report.TopSideEffectsRequest
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.view.PieChartView
import lecho.lib.hellocharts.model.PieChartData
import android.util.TypedValue

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportViewModel
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
        setupObservers()
        loadData()
    }

    private fun initializeComponents() {
        binding.toolbar.setPageTitle("Reports")
        viewModel = ReportViewModel(requireActivity().application)
        sessionManager = SessionManager(requireContext())
    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.show("Loading reports...")
            } else {
                binding.loadingOverlay.hide()
            }        }

        viewModel.topSideEffects.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    displayTopSideEffects(response.data)
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
                    displayMostMissedMedications(response.data)
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
                    displayAdherenceData(response)
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
            viewModel.fetchReports(patientId)
        } ?: run {
            showError("Patient ID not found")
        }
    }

//    private fun fetchReports(patientId: Int) {
//        viewModel.fetchTopSideEffects(TopSideEffectsRequest(patient_id = patientId))
//        viewModel.fetchMostMissedMedications(MostMissedMedicationsRequest(patient_id = patientId))
//        viewModel.fetchMedicalAdherenceReport(MedicalAdherenceReportRequest(patient_id = patientId))
//    }

    private fun displayTopSideEffects(data: List<TopSideEffect>) {
        if (data.isEmpty()) {
            binding.noDataTopSideEffects.visibility = View.VISIBLE
            binding.topSideEffectsRecyclerView.visibility = View.GONE
        } else {
            binding.noDataTopSideEffects.visibility = View.GONE
            binding.topSideEffectsRecyclerView.visibility = View.VISIBLE
            val adapter = SideEffectAdapter(data)
            binding.topSideEffectsRecyclerView.adapter = adapter
            binding.topSideEffectsRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun displayMostMissedMedications(data: List<MissedMedication>) {
        if (data.isEmpty()) {
            binding.noDataMissedMedications.visibility = View.VISIBLE
            binding.missedMedicationsRecyclerView.visibility = View.GONE
        } else {
            binding.noDataMissedMedications.visibility = View.GONE
            binding.missedMedicationsRecyclerView.visibility = View.VISIBLE
            val adapter = MissedMedicationAdapter(data)
            binding.missedMedicationsRecyclerView.adapter = adapter
            binding.missedMedicationsRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun displayAdherenceData(data: MedicalAdherenceReportResponse) {
        val adherencePercentage = data.data.adherence_percentage
        val nonAdherencePercentage = 100 - adherencePercentage

        val values = mutableListOf<SliceValue>()
        values.add(SliceValue(adherencePercentage.toFloat(), resources.getColor(R.color.success)).setLabel("${adherencePercentage}%"))
        values.add(SliceValue(nonAdherencePercentage.toFloat(), resources.getColor(R.color.error)).setLabel("${nonAdherencePercentage}%"))

        val pieChartData = PieChartData(values)
        pieChartData.apply {
            setHasLabels(false)
            setHasCenterCircle(true)
            centerCircleScale = 0.8f

            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorBackgroundFloating, typedValue, true)
            centerCircleColor = typedValue.data
        }

        binding.adherenceChart.pieChartData = pieChartData
    }

    private fun showError(message: String, isError: Boolean = true) {
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