package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentMyCaregiversBinding
import com.example.careplus.utils.SnackbarUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import com.example.careplus.data.filter_model.FilterMedications
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.careplus.data.filter_model.FilterCareProviders

class MyCaregiversFragment : Fragment(), CaregiverActionListener, FilterBottomSheetFragment.FilterListener {
    private var _binding: FragmentMyCaregiversBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthProvidersViewModel by viewModels()
    private lateinit var healthProvidersAdapter: HealthProvidersAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCaregiversBinding.inflate(inflater, container, false)
        loadingIndicator = binding.loadingIndicator // Bind the loading indicator
        emptyStateText = binding.emptyStateText // Assuming you have a TextView for empty state
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearch()
        setupFilterButton()
        setupRecyclerView()
        setupObservers()
        viewModel.fetchMyCaregivers()
        showLoadingState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyCaregivers()
        showLoadingState()
    }
    private fun showLoadingState(){
        loadingIndicator.visibility = VISIBLE
        binding.recyclerView.visibility = GONE
        emptyStateText.visibility = GONE
    }

    private fun setupSearch() {
        binding.searchFilterLayout.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.fetchAllCaregivers(FilterCareProviders(null,null,null,text.toString(),null,null,null))
        }
    }

    private fun setupFilterButton() {
        binding.searchFilterLayout.filterButton.setOnClickListener {
            val filterBottomSheet = FilterBottomSheetFragment.newInstance()
            filterBottomSheet.setFilterListener(this)
            filterBottomSheet.show(parentFragmentManager, filterBottomSheet.tag)
        }
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter { caregiver ->
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
        }
    }

    private fun setupObservers() {
        viewModel.myCaregivers.observe(viewLifecycleOwner) { result ->
            loadingIndicator.visibility = GONE
            result.onSuccess { response ->
                if (response.data.isNullOrEmpty()) {
                    emptyStateText.visibility = VISIBLE
                    healthProvidersAdapter.submitList(emptyList())
                    binding.recyclerView.visibility = GONE
                } else {
                    emptyStateText.visibility = GONE
                    healthProvidersAdapter.submitList(response.data)
                    binding.recyclerView.visibility = VISIBLE
                }
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error fetching caregivers")
            }
        }
    }

    override fun onCaregiverRemoved(roleId: Int) {
        // Get current list from adapter, remove item, and submit updated list
        val currentList = healthProvidersAdapter.currentList.toMutableList()
        currentList.removeAll { it.user_role.id == roleId }
        
        if (currentList.isEmpty()) {
            emptyStateText.visibility = VISIBLE
            binding.recyclerView.visibility = GONE
        }
        
        healthProvidersAdapter.submitList(currentList)
    }

    override fun onDoctorRemoved(roleId: Int) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFiltersApplied(filter: FilterCareProviders) {
        viewModel.fetchMyCaregivers(filter)
        showLoadingState()
    }
} 