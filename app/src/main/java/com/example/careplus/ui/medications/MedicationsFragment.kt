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
import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.model.CaregiverInfo
import com.example.careplus.data.model.DoctorInfo
import com.example.careplus.data.model.MedicationForm
import com.example.careplus.data.model.MedicationRoute


class MedicationsFragment : Fragment(), MedicationFilterBottomSheet.FilterListener {
    private var _binding: FragmentMedicationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MedicationsViewModel(SessionManager(requireContext())) as T
            }
        }
    }
    private var forms: List<MedicationForm> = emptyList()
    private var routes: List<MedicationRoute> = emptyList()
    private var caregivers: List<CaregiverInfo> = emptyList()
    private var doctors: List<DoctorInfo> = emptyList()

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
        setupSearchAndFilter()
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
                setupDateForFilters(medications)
            }.onFailure { exception ->
                Log.e("MedicationsFragment", "Error loading medications", exception)
                SnackbarUtils.showSnackbar(
                    binding.root,
                    exception.message ?: "Failed to load medications"
                )
            }
        }

    }

    private fun setupSearchAndFilter() {
        binding.searchFilterLayout.filterButton.setOnClickListener {
            val filterBottomSheet = MedicationFilterBottomSheet.newInstance(forms, routes, caregivers, doctors)
            filterBottomSheet.setFilterListener(this)
            filterBottomSheet.show(parentFragmentManager, filterBottomSheet.tag)
        }
    }
    private fun setupDateForFilters(medications: List<MedicationDetails>) {
         forms = medications.mapNotNull { it.form }.distinct()
         routes = medications.mapNotNull { it.route }.distinct()
         caregivers = medications.mapNotNull { it.caregiver }.distinct()
         doctors = medications.mapNotNull { it.doctor }.distinct()
    }

    override fun onFiltersApplied(filter: FilterMedications,) {
        // TODO: Apply the filters to your ViewModel
//        viewModel.applyFilters(filter)
    }
}
   