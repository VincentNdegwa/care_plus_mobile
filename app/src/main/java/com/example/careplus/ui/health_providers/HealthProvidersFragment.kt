package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.careplus.databinding.FragmentHealthProvidersBinding
import com.google.android.material.tabs.TabLayoutMediator
import androidx.lifecycle.ViewModelProvider

class HealthProvidersFragment : Fragment() {
    private var _binding: FragmentHealthProvidersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthProvidersViewModel by viewModels()
    private lateinit var healthProvidersAdapter: HealthProvidersAdapter // Create an adapter for displaying caregivers

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthProvidersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setPageTitle("Health Providers")
        setupViewPager()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupViewPager() {
        val pagerAdapter = CaregiverPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Care Providers"
                1 -> "My Doctors"
                2 -> "My Caregivers"
                else -> null
            }
        }.attach()
    }

    private fun setupRecyclerView() {
        healthProvidersAdapter = HealthProvidersAdapter { caregiver ->
            val bottomSheet = CaregiverBottomSheetFragment.newInstance(caregiver)
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        binding.recyclerView.adapter = healthProvidersAdapter
    }

    private fun observeViewModel() {
        viewModel.fetchAllCaregivers() // Fetch caregivers
        viewModel.caregivers.observe(viewLifecycleOwner) { result ->
            result.onSuccess { caregivers ->
                healthProvidersAdapter.submitList(caregivers) // Update the adapter with the list of caregivers
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 