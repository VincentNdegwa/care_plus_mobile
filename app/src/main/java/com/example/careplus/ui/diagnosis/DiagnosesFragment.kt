package com.example.careplus.ui.diagnosis

import android.os.Bundle
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
                        delay(500)
                        text?.toString()?.let { query ->
                            if (query.isNotEmpty()) {
                                viewModel.searchDiagnoses("patient", query)
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
                emptyStateImage.setImageResource(R.drawable.ic_empty_diagnoses)
                emptyStateTitle.text = "No Diagnoses Found"
                emptyStateDescription.text = "There are no diagnoses to display at the moment"
            }
        }
    }

    private fun setupObservers() {
        viewModel.diagnoses.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                diagnosisAdapter.submitList(response.data)
                updateEmptyState(response.data.isEmpty())
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "An error occurred")
                updateEmptyState(true)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
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
        sessionManager.getUser()?.patient?.id?.let { patientId ->
            viewModel.loadDiagnoses(patientId, refresh)
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
        if (filter == null){
            loadData()
        }else{
            viewModel.filterDiagnoses(filter)
        }
        currentFilter = filter
        updateFilterIndicator()
    }
} 