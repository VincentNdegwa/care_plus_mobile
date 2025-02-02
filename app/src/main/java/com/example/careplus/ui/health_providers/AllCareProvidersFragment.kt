package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentAllCaregiversBinding
import com.example.careplus.utils.SnackbarUtils
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import com.example.careplus.data.filter_model.FilterCareProviders
import androidx.recyclerview.widget.RecyclerView

class AllCareProvidersFragment : Fragment(), FilterBottomSheetFragment.FilterListener {
    private var _binding: FragmentAllCaregiversBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthProvidersViewModel by viewModels()
    private lateinit var healthProvidersAdapter: HealthProvidersAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private var currentFilter: FilterCareProviders? = null

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
        setupObservers()
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter { caregiver ->
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver)
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadNextPageAllCaregivers()
                    }
                }
            })
        }
        viewModel.fetchAllCaregivers()
        showLoadingState()
    }

    private fun setupSearch() {
        binding.searchFilterLayout.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.fetchAllCaregivers(FilterCareProviders(null,null,null,text.toString(),null,null,null))
        }
    }
    private fun showLoadingState(){
        loadingIndicator.visibility = VISIBLE
        binding.recyclerView.visibility = GONE
        emptyStateText.visibility = GONE
    }
    private fun setupFilterButton() {
        binding.searchFilterLayout.filterButton.setOnClickListener {
            val filterBottomSheet = FilterBottomSheetFragment.newInstance(currentFilter)
            filterBottomSheet.setFilterListener(this)
            filterBottomSheet.show(parentFragmentManager, filterBottomSheet.tag)
        }
        updateFilterIndicator()
    }

    private fun setupObservers() {
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

//        viewModel.paginationLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.paginationProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
    }
    private fun updateFilterIndicator() {
        binding.searchFilterLayout.filterIndicator.visibility =
            if (currentFilter != null) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFiltersApplied(filter: FilterCareProviders?) {
        currentFilter = filter
        viewModel.fetchAllCaregivers(filter)
        showLoadingState()
        updateFilterIndicator()
    }
} 