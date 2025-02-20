package com.example.careplus.ui.diagnosis

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.SessionManager
import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.model.diagnosis.DiagnosisFilterRequest
import com.example.careplus.databinding.FragmentDiagnosesBinding
import com.example.careplus.utils.SnackbarUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiagnosesFragment : Fragment(), DiagnosisFilterBottomSheet.FilterListener {
    private var _binding: FragmentDiagnosesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiagnosisViewModel by viewModels()
    private lateinit var diagnosisAdapter: DiagnosisAdapter
    private lateinit var sessionManager: SessionManager
    private var searchJob: Job? = null
    private var currentFilter: DiagnosisFilterRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiagnosesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupUI()
        setupObservers()
        loadData()
    }

    private fun setupUI() {
        binding.toolbar.setPageTitle("Diagnoses")

        binding.emptyText.text = "No diagnoses found"
        
        diagnosisAdapter = DiagnosisAdapter { diagnosis ->
//            findNavController().navigate(
//                DiagnosesFragmentDirections.actionToDiagnosisDetail(diagnosis)
//            )
        }

        binding.apply {
            diagnosisRecyclerView.apply {
                adapter = diagnosisAdapter
                layoutManager = LinearLayoutManager(context)
            }
            binding.searchFilterLayout.apply {
                searchEditText.doOnTextChanged { text, _, _, _ ->
                    searchJob?.cancel()
                    searchJob = MainScope().launch {
                        delay(100)
                        text?.toString()?.let { query ->
                            if (query.isNotEmpty()) {
                                viewModel.searchDiagnoses(query)
                            } else {
                                loadData(true)
                            }
                        }
                    }
                }

                filterButton.setOnClickListener {
                    showFilterDialog()
                }
            }
            
            emptyStateLayout.apply {
                emptyStateTitle.text = "No Diagnoses Found"
            }
        }
    }

    private fun setupObservers() {
        viewModel.diagnoses.observe(viewLifecycleOwner) { result ->
            Log.d("DiagnosesFragment", "Received diagnoses update")
            result.onSuccess { response ->
                Log.d("DiagnosesFragment", "Success: ${response.data.size} items")
                diagnosisAdapter.submitList(response.data)
                updateEmptyState(response.data.isEmpty())
            }.onFailure { exception ->
                Log.e("DiagnosesFragment", "Error: ${exception.message}")
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "An error occurred")
                updateEmptyState(true)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("DiagnosesFragment", "Loading state changed to: $isLoading")
            binding.loadingOverlay.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            emptyStateLayout.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
            diagnosisRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun loadData(refresh: Boolean = false) {
        Log.d("DiagnosesFragment", "loadData called with refresh: $refresh")
        sessionManager.getUser()?.patient?.id?.let { patientId ->
            Log.d("DiagnosesFragment", "Loading data for patient: $patientId")
            viewModel.loadDiagnoses(patientId, refresh)
        } ?: run {
            Log.e("DiagnosesFragment", "No patient ID found")
            SnackbarUtils.showSnackbar(binding.root, "Unable to load data: No patient ID")
        }
    }

    private fun showFilterDialog() {
        val filterBottomSheet = DiagnosisFilterBottomSheet().apply {
            setFilterListener(this@DiagnosesFragment)
            currentFilter = this@DiagnosesFragment.currentFilter
        }
        
        viewModel.diagnoses.value?.getOrNull()?.data?.let { diagnoses ->
            filterBottomSheet.setDoctors(diagnoses)
            filterBottomSheet.setPatientId(sessionManager.getUser()?.patient?.id)
        }
        
        filterBottomSheet.show(childFragmentManager, DiagnosisFilterBottomSheet.TAG)
    }

    private fun updateFilterIndicator() {
        binding.searchFilterLayout.filterIndicator.visibility =
            if (currentFilter != null) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFiltersApplied(filter: DiagnosisFilterRequest?) {
        currentFilter = filter
        updateFilterIndicator()
        if (filter == null) {
            sessionManager.getUser()?.patient?.id.let {
                if (it != null) {
                    viewModel.listMyDiagnoses(it)
                    Log.d("Diagnoses Fragment", "Loading.. list mine with ${it}")
                }
            }
        } else {
            viewModel.filterDiagnoses(filter)
        }
    }
} 