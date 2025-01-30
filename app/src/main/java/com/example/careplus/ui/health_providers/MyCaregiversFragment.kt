package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentMyCaregiversBinding

class MyCaregiversFragment : Fragment() {
    private var _binding: FragmentMyCaregiversBinding? = null
    private val binding get() = _binding!!
    private lateinit var healthProvidersAdapter: HealthProvidersAdapter // Create an adapter for displaying caregivers

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCaregiversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        // Fetch and observe caregivers data here
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = healthProvidersAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 