package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentMyDoctorsBinding
import com.example.careplus.utils.SnackbarUtils

class MyDoctorsFragment : Fragment() {
    private var _binding: FragmentMyDoctorsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthProvidersViewModel by viewModels()
    private lateinit var healthProvidersAdapter: HealthProvidersAdapter // Create an adapter for displaying doctors

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDoctorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupRecyclerView()
        setupObervers()
        viewModel.fetchMyDoctors()
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
        }
    }
    private fun setupObervers() {
        viewModel.myDoctors.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                healthProvidersAdapter.submitList(response.data) // Update the adapter with the fetched data
                binding.recyclerView.visibility = View.VISIBLE // Show the RecyclerView when data is available
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error fetching caregivers")
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 