package com.example.careplus.ui.medications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentMedicationsBinding
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.data.model.MedicationDetails
import androidx.navigation.fragment.findNavController
import com.example.careplus.R
import com.example.careplus.data.filter_model.FilterCareProviders
import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.model.CaregiverInfo
import com.example.careplus.data.model.DoctorInfo
import com.example.careplus.data.model.MedicationForm
import com.example.careplus.data.model.MedicationRoute


class MedicationsFragment : Fragment(), MedicationFilterBottomSheet.FilterListener {
    private var _binding: FragmentMedicationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicationsViewModel by viewModels()
    private var forms: List<MedicationForm> = emptyList()
    private var routes: List<MedicationRoute> = emptyList()
    private var caregivers: List<CaregiverInfo> = emptyList()
    private var doctors: List<DoctorInfo> = emptyList()
    private var currentFilter: FilterMedications? = null

    private val medicationsAdapter = MedicationsAdapter { medication ->
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
        showLoadingState()
        viewModel.fetchMedications(currentFilter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setPageTitle("Medications")
        setupSearchAndFilter()
        binding.addMedicationFab.setOnClickListener {
            findNavController().navigate(R.id.action_medications_to_newMedication)
        }
    }

    private fun setupViews() {
//        binding.addMedicationButton.setOnClickListener {
//            // TODO: Navigate to add medication screen
//        }

    }

    private fun setupRecyclerView() {
        binding.medicationsList.apply {
            adapter = medicationsAdapter
            layoutManager = LinearLayoutManager(context)
            
            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Load more when user scrolls near the end
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.medications.observe(viewLifecycleOwner) { result: Result<List<MedicationDetails>> ->
            binding.progressBar.visibility = View.GONE

            result.onSuccess { medications ->
                Log.d("MedicationsFragment", "Medications received: ${medications.size}")
                if (medications.isEmpty()){
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.medicationsList.visibility = View.GONE
                }else{
                    binding.emptyStateText.visibility = View.GONE
                    binding.medicationsList.visibility = View.VISIBLE
                    medicationsAdapter.submitList(medications)
                }
                setupDataForFilters(medications)
            }.onFailure { exception ->
                Log.e("MedicationsFragment", "Error loading medications", exception)
                SnackbarUtils.showSnackbar(
                    binding.root,
                    exception.message ?: "Failed to load medications"
                )
            }
        }

        // Observe pagination loading state
//        viewModel.paginationLoading.observe(viewLifecycleOwner) { isLoading ->
//            // Show/hide loading indicator at the bottom of the list
//            binding.paginationProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
    }

    private fun setupSearchAndFilter() {
        binding.searchFilterLayout.filterButton.setOnClickListener {
            val filterBottomSheet = MedicationFilterBottomSheet.newInstance(
                forms, 
                routes, 
                caregivers, 
                doctors,
                currentFilter
            )
            filterBottomSheet.setFilterListener(this)
            filterBottomSheet.show(parentFragmentManager, filterBottomSheet.tag)
        }
        binding.searchFilterLayout.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.fetchMedications(FilterMedications(
                null,null,null,null,null,null,null,null,text.toString(),null,null,null))
            showLoadingState()
        }
        
        updateFilterIndicator()
    }

    private fun updateFilterIndicator() {
        binding.searchFilterLayout.filterIndicator.visibility = 
            if (currentFilter != null) View.VISIBLE else View.GONE
    }

    private fun setupDataForFilters(medications: List<MedicationDetails>) {
        val newForms = medications.mapNotNull { it.form }.distinct()
        val newRoutes = medications.mapNotNull { it.route }.distinct()
        val newCaregivers = medications.mapNotNull { it.caregiver }.distinct()
        val newDoctors = medications.mapNotNull { it.doctor }.distinct()

        // Add new items to existing lists without duplicates
        forms = (forms + newForms).distinct()
        routes = (routes + newRoutes).distinct()
        caregivers = (caregivers + newCaregivers).distinct()
        doctors = (doctors + newDoctors).distinct()
    }

    override fun onFiltersApplied(filter: FilterMedications?) {
        currentFilter = filter
        viewModel.fetchMedications(filter)
        showLoadingState()
        updateFilterIndicator() // Update indicator when filter is applied
    }

    private fun showLoadingState(){
        binding.progressBar.visibility = View.VISIBLE
        binding.medicationsList.visibility = View.GONE
        binding.emptyStateText.visibility = View.GONE
    }
}
   