package com.example.careplus.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentReportBinding
import com.example.careplus.utils.SnackbarUtils
import android.util.Log
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.careplus.data.model.report.MedicationVsSideEffectCountsRequest
import com.example.careplus.data.model.report.MedicationSideEffectCount
import com.example.careplus.data.model.report.MedicalAdherenceReportRequest
import com.example.careplus.data.model.report.MedicalAdherenceReportResponse
import com.example.careplus.data.model.report.MedicationAdherence
import com.example.careplus.data.model.report.MedicationAdherenceByMedicationRequest
import com.example.careplus.data.model.report.MissedMedication
import com.example.careplus.data.model.report.MostMissedMedicationsRequest
import com.example.careplus.data.model.report.TopSideEffect
import com.example.careplus.data.model.report.TopSideEffectsRequest

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
            fetchReports(patientId)
        } ?: run {
            showError("Patient ID not found")
        }
    }

    private fun fetchReports(patientId: Int) {
        viewModel.fetchTopSideEffects(TopSideEffectsRequest(patient_id = patientId))
        viewModel.fetchMostMissedMedications(MostMissedMedicationsRequest(patient_id = patientId))
        viewModel.fetchMedicalAdherenceReport(MedicalAdherenceReportRequest(patient_id = patientId))
    }

    private fun displayTopSideEffects(data: List<TopSideEffect>) {
        val adapter = SideEffectAdapter(data)
        binding.topSideEffectsRecyclerView.adapter = adapter
    }

    private fun displayMostMissedMedications(data: List<MissedMedication>) {
        val adapter = MissedMedicationAdapter(data)
        binding.missedMedicationsRecyclerView.adapter = adapter
    }

    private fun displayAdherenceData(data: MedicalAdherenceReportResponse) {
        val adherencePercentage = data.data.adherence_percentage
        binding.adherenceTextView.text = "$adherencePercentage%"
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