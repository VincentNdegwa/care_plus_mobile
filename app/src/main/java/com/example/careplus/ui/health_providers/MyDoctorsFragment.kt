package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentMyDoctorsBinding

class MyDoctorsFragment : Fragment() {
    private var _binding: FragmentMyDoctorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var doctorsAdapter: HealthProvidersAdapter // Create an adapter for displaying doctors

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
        // Fetch and observe doctors data here
    }

    private fun setupRecyclerView() {
        doctorsAdapter = HealthProvidersAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = doctorsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 