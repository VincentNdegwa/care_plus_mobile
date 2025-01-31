package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentAllCaregiversBinding
import com.example.careplus.utils.SnackbarUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.doOnTextChanged
import com.example.careplus.R
import com.example.careplus.data.filter_model.FilterCareProviders

class AllCareProvidersFragment : Fragment(), FilterBottomSheetFragment.FilterListener {
    private var _binding: FragmentAllCaregiversBinding? = null
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
        _binding = FragmentAllCaregiversBinding.inflate(inflater, container, false)
        loadingIndicator = binding.loadingIndicator
        emptyStateText = binding.emptyStateText
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilterButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter { caregiver ->
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver)
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
        }
        viewModel.fetchAllCaregivers()
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

    private fun observeViewModel() {
        viewModel.caregivers.observe(viewLifecycleOwner) { result ->
            binding.loadingIndicator.visibility = View.GONE
            
            result.onSuccess { caregivers ->
                if (caregivers.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    healthProvidersAdapter.submitList(caregivers)
                }
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(
                    binding.root,
                    exception.message ?: "Error fetching caregivers"
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFiltersApplied(filter: FilterCareProviders) {
        viewModel.fetchAllCaregivers(filter)
    }
} 