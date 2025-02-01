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
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.careplus.data.filter_model.FilterCareProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MyDoctorsFragment : Fragment(), CaregiverActionListener, FilterBottomSheetFragment.FilterListener {
    private var _binding: FragmentMyDoctorsBinding? = null
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
        _binding = FragmentMyDoctorsBinding.inflate(inflater, container, false)
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
        viewModel.fetchMyDoctors()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyDoctors()
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
            Log.d("MyDoctorsFragment", "Opening bottom sheet for caregiver: ${caregiver.id}")
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
        }
    }

    private fun setupObservers() {
        viewModel.myDoctors.observe(viewLifecycleOwner) { result ->
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

    override fun onDoctorRemoved(roleId: Int) {
        val currentList = healthProvidersAdapter.currentList.toMutableList()
        currentList.removeAll { it.user_role.id == roleId }
        if (currentList.isEmpty()) {
            emptyStateText.visibility = VISIBLE
            binding.recyclerView.visibility = GONE
        }
        healthProvidersAdapter.submitList(currentList)
    }

    override fun onFiltersApplied(filter: FilterCareProviders) {
            viewModel.fetchMyDoctors(filter)
    }
} 