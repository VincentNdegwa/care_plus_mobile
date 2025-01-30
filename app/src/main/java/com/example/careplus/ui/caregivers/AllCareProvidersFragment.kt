package com.example.careplus.ui.caregivers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.databinding.FragmentAllCaregiversBinding
import com.example.careplus.utils.SnackbarUtils

class AllCaregiversFragment : Fragment() {
    private var _binding: FragmentAllCaregiversBinding? = null
    private val binding get() = _binding!!
    private lateinit var caregiversAdapter: CaregiversAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCaregiversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        // Fetch and observe caregivers data here
    }

    private fun setupRecyclerView() {
        caregiversAdapter = CaregiversAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = caregiversAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 