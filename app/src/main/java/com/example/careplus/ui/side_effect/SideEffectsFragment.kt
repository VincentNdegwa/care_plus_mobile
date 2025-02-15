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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.data.model.side_effect.FetchSideEffectsRequest
import com.example.careplus.data.model.side_effect.SideEffect
import com.example.careplus.databinding.FragmentSideEffectsBinding
import com.example.careplus.utils.SnackbarUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SideEffectsFragment : Fragment() {
    private var _binding: FragmentSideEffectsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SideEffectViewModel by viewModels()
    private lateinit var adapter: SideEffectAdapter
    private var currentSideEffects = mutableListOf<SideEffect>()
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
        adapter = SideEffectAdapter(
            onItemClick = { sideEffect ->
                findNavController().navigate(
                    SideEffectsFragmentDirections.actionSideEffectsToDetails(sideEffect)
                )
            },
            onEditClick = { sideEffect ->
                findNavController().navigate(
                    SideEffectsFragmentDirections.actionSideEffectsToEdit(sideEffect)
                )
            },
            onDeleteClick = { sideEffect ->
                showDeleteConfirmationDialog(sideEffect)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SideEffectsFragment.adapter
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadNextPage()
                    }
                }
            })
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
                currentSideEffects = response.data.toMutableList()
                updateUI()
            }.onFailure { exception ->
                binding.emptyStateText.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                showSnackbar(exception.message ?: "Failed to load side effects")
            }
        }


        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { success ->
                if (success) {
                    pendingDeleteSideEffect?.let { deletedSideEffect ->
                        // Remove from current list
                        currentSideEffects.removeAll { it.id == deletedSideEffect.id }
                        // Update UI
                        updateUI()
                        showSnackbar("Side effect deleted successfully", false)
                    }
                    pendingDeleteSideEffect = null
                }
            }.onFailure { exception ->
                showSnackbar(exception.message ?: "Failed to delete side effect")
                pendingDeleteSideEffect = null
            }
        }
    }

    private fun updateUI() {
        adapter.submitList(ArrayList(currentSideEffects))
        
        if (currentSideEffects.isEmpty()) {
            binding.emptyStateText.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateText.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private var pendingDeleteSideEffect: SideEffect? = null

    private fun fetchSideEffects(searchQuery: String? = null) {
        binding.loadingIndicator.visibility = View.VISIBLE
        val patientId = viewModel.getPatientId()
        if (patientId != null){

        viewModel.fetchSideEffects(
            FetchSideEffectsRequest(
                patient_id = patientId,
                page_number = 1,
                per_page = 20
            )
        )

        }
    }

    private fun showDeleteConfirmationDialog(sideEffect: SideEffect) {
        pendingDeleteSideEffect = sideEffect
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Side Effect")
            .setMessage("Are you sure you want to delete this side effect?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSideEffect(sideEffect.id)
            }
            .setNegativeButton("Cancel") { _, _ ->
                pendingDeleteSideEffect = null
            }
            .show()
    }

    private fun showSnackbar(message: String, isError: Boolean = true) {
        SnackbarUtils.showSnackbar(
            view = binding.root,
            message = message,
            isError = isError
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 