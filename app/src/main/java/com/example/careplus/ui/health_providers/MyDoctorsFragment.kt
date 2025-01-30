package com.example.careplus.ui.health_providers

import android.os.Bundle
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

class MyDoctorsFragment : Fragment() {
    private var _binding: FragmentMyDoctorsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthProvidersViewModel by viewModels()
    private lateinit var healthProvidersAdapter: HealthProvidersAdapter // Create an adapter for displaying doctors
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
        setupRecyclerView()
        setupObservers()
        viewModel.fetchMyDoctors()
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter{  caregiver ->
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver)
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
        }
    }

    private fun setupObservers() {
        viewModel.myDoctors.observe(viewLifecycleOwner) { result ->
            loadingIndicator.visibility = GONE // Hide loading indicator
            result.onSuccess { response ->
                if (response.data.isNullOrEmpty()) {
                    emptyStateText.visibility = VISIBLE // Show empty state
                    healthProvidersAdapter.submitList(emptyList()) // Clear the adapter
                    binding.recyclerView.visibility = GONE // Hide the RecyclerView
                } else {
                    emptyStateText.visibility = GONE // Hide empty state
                    healthProvidersAdapter.submitList(response.data) // Update the adapter with the fetched data
                    binding.recyclerView.visibility = VISIBLE // Show the RecyclerView when data is available
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
} 