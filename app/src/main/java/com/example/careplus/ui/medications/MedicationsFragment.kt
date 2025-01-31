package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentMedicationsBinding
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.data.model.MedicationDetails
import androidx.navigation.fragment.findNavController
import com.example.careplus.ui.health_providers.FilterBottomSheetFragment

class MedicationsFragment : Fragment(), FilterBottomSheetFragment.FilterListener {
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
        // Log the medication details before navigating
        Log.d("MedicationsFragment", "Navigating to EditMedicationFragment with: $medication")

        // Ensure medication is not null
        if (medication != null) {
            findNavController().navigate(
                MedicationsFragmentDirections.actionMedicationsToMedicationDetail(medication)
            )
        } else {
            Log.e("MedicationsFragment", "Medication is null, cannot navigate")
        }
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
//        setupSearch()
//        setupFilterButton()
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

    override fun onFiltersApplied(
        specialization: String?,
        clinicName: String?,
        agencyName: String?
    ) {
        TODO("Not yet implemented")
    }
}
   