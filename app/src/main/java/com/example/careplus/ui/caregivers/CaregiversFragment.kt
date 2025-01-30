package com.example.careplus.ui.caregivers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentCaregiversBinding
import com.example.careplus.utils.SnackbarUtils
import com.google.android.material.tabs.TabLayoutMediator

class CaregiversFragment : Fragment() {
    private var _binding: FragmentCaregiversBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverViewModel by viewModels()
    private lateinit var caregiversAdapter: CaregiversAdapter // Create an adapter for displaying caregivers

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        observeCaregivers()
        viewModel.fetchAllCaregivers() // Fetch caregivers when the view is created
    }

    private fun setupViewPager() {
        val pagerAdapter = CaregiverPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All Caregivers"
                1 -> "My Doctors"
                2 -> "My Caregivers"
                else -> null
            }
        }.attach()
    }

    private fun observeCaregivers() {
        viewModel.caregivers.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                caregiversAdapter.submitList(response.data) // Update the adapter with the fetched data
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