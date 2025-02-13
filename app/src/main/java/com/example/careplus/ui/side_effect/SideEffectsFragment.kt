package com.example.careplus.ui.side_effect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.data.model.side_effect.FetchSideEffectsRequest
import com.example.careplus.databinding.FragmentSideEffectsBinding
import com.example.careplus.utils.SnackbarUtils

class SideEffectsFragment : Fragment() {
    private var _binding: FragmentSideEffectsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SideEffectViewModel by viewModels()
    private lateinit var sideEffectAdapter: SideEffectAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSideEffectsBinding.inflate(inflater, container, false)
        loadingIndicator = binding.loadingIndicator
        emptyStateText = binding.emptyStateText
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
        fetchSideEffects()
    }

    private fun setupViews() {
        binding.toolbar.setPageTitle("Side Effects")
    }

    private fun setupRecyclerView() {
        sideEffectAdapter = SideEffectAdapter { sideEffect ->
            // Handle side effect click - show details or edit
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sideEffectAdapter
        }
    }

    private fun setupSearch() {
        binding.searchFilterLayout.searchEditText.doOnTextChanged { text, _, _, _ ->
            // Implement search functionality
            fetchSideEffects(searchQuery = text?.toString())
        }
    }

    private fun observeViewModel() {
        viewModel.sideEffects.observe(viewLifecycleOwner) { result ->
            binding.loadingIndicator.visibility = View.GONE
            
            result.onSuccess { response ->
                if (response.data.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    sideEffectAdapter.submitList(response.data)
                }
            }.onFailure { exception ->
                binding.emptyStateText.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error fetching side effects")
            }
        }
    }

    private fun fetchSideEffects(searchQuery: String? = null) {
        binding.loadingIndicator.visibility = View.VISIBLE
        val patientId = viewModel.getPatientId()
        if (patientId != null){

        viewModel.fetchSideEffects(
            FetchSideEffectsRequest(
                patient_id = patientId,
            )
        )

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 