package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.MainActivity
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.Medication
import com.example.careplus.data.model.SimpleProfile
import com.example.careplus.databinding.FragmentMedicationsBinding
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.R
import com.example.careplus.data.model.MedicationDetails
import androidx.navigation.fragment.findNavController

class MedicationsFragment : Fragment() {
    private var _binding: FragmentMedicationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MedicationsViewModel(SessionManager(requireContext())) as T
            }
        }
    }
    private val medicationsAdapter = MedicationsAdapter { medication ->
        findNavController().navigate(
            MedicationsFragmentDirections.actionMedicationsToMedicationDetail(medication.id)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationsBinding.inflate(inflater, container, false)
        
        setupViews()
        setupRecyclerView()
        setupObservers()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setPageTitle("Medications")
    }

    private fun setupViews() {
//        binding.addMedicationButton.setOnClickListener {
//            // TODO: Navigate to add medication screen
//        }

        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        binding.medicationsList.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        binding.medicationsList.apply {
            adapter = medicationsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.medications.observe(viewLifecycleOwner) { result: Result<List<MedicationDetails>> ->
            binding.progressBar.visibility = View.GONE
            binding.medicationsList.visibility = View.VISIBLE
            
            result.onSuccess { medications ->
                Log.d("MedicationsFragment", "Medications received: ${medications.size}")
                medicationsAdapter.submitList(medications)
            }.onFailure { exception ->
                Log.e("MedicationsFragment", "Error loading medications", exception)
                SnackbarUtils.showSnackbar(
                    binding.root,
                    exception.message ?: "Failed to load medications"
                )
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 