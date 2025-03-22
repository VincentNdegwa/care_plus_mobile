package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentMyDoctorsBinding
import com.example.careplus.utils.SnackbarUtils
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.filter_model.FilterCareProviders

class MyDoctorsFragment : Fragment(), CaregiverActionListener, FilterBottomSheetFragment.FilterListener {
    private var _binding: FragmentMyDoctorsBinding? = null
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
        _binding = FragmentMyDoctorsBinding.inflate(inflater, container, false)
        loadingIndicator = binding.loadingIndicator // Bind the loading indicator
        emptyStateText = binding.emptyStateText // Assuming you have a TextView for empty state
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchAndFilter()
        setupFilterButton()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyDoctors()
        showLoadingState()
    }

    private fun setupSearchAndFilter() {
        binding.searchFilterLayout.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.fetchMyDoctors(FilterCareProviders(null,null,null,text.toString(), null,null,null))
            showLoadingState()
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

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter { caregiver ->
            Log.d("MyDoctorsFragment", "Opening bottom sheet for caregiver: ${caregiver.id}")
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver, myHealthProvider = true)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
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
                        viewModel.loadNextPageMyDoctors()
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.myDoctors.observe(viewLifecycleOwner) { result ->
            binding.loadingIndicator.visibility = GONE
            
            result.onSuccess { doctors ->
                if (doctors.data.isEmpty()) {
                    binding.emptyStateText.visibility = VISIBLE
                    binding.recyclerView.visibility = GONE
                    healthProvidersAdapter.submitList(emptyList())
                } else {
                    binding.emptyStateText.visibility = GONE
                    binding.recyclerView.visibility = VISIBLE
                    healthProvidersAdapter.submitList(doctors.data)
                }
            }.onFailure { exception ->
                binding.emptyStateText.visibility = VISIBLE
                binding.recyclerView.visibility = GONE
                healthProvidersAdapter.submitList(emptyList())
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error fetching doctors")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCaregiverRemoved(roleId: Int) {
        TODO("Not yet implemented")
    }
    private fun updateFilterIndicator() {
        binding.searchFilterLayout.filterIndicator.visibility =
            if (currentFilter != null) VISIBLE else GONE
    }

    override fun onDoctorRemoved(roleId: Int) {
        val currentList = healthProvidersAdapter.currentList.toMutableList()
        currentList.removeAll { it.user_role.id == roleId }
        if (currentList.isEmpty()) {
            emptyStateText.visibility = VISIBLE
            binding.recyclerView.visibility = GONE
        }
        healthProvidersAdapter.submitList(currentList)
    }

    override fun onFiltersApplied(filter: FilterCareProviders?) {
        currentFilter = filter // Save the current filter
        viewModel.fetchMyDoctors(filter)
        showLoadingState()
        updateFilterIndicator()
    }
} 