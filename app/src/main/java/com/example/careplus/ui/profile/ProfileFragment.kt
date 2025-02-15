package com.example.careplus.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.careplus.R
import com.example.careplus.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.example.careplus.data.model.profile.ProfileData

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupObservers()
        setupListeners()
        
        viewModel.fetchProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setPageTitle("Profile")
        }
    }

    private fun setupListeners() {
        binding.editProfileButton.setOnClickListener {
            viewModel.profile.value?.getOrNull()?.let { profileData ->
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToEditProfileFragment(profileData)
                findNavController().navigate(action)
            }
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.show()
            } else {
                binding.loadingOverlay.hide()
            }
        }

        viewModel.profile.observe(viewLifecycleOwner) { result ->
            result.onSuccess { profileData ->
                updateUI(profileData)
            }.onFailure { exception ->
                showError(exception.message ?: "Failed to load profile")
            }
        }
    }

    private fun updateUI(profileData: ProfileData) {
        binding.apply {
            nameText.text = profileData.name
            emailText.text = profileData.email
            roleChip.text = profileData.role

            // Load profile image
            profileData.profile.avatar?.let { avatarUrl ->
                Glide.with(this@ProfileFragment)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(profileImage)
            }

            // Update profile details
            genderText.text = profileData.profile.gender ?: "Not set"
            dobText.text = profileData.profile.date_of_birth ?: "Not set"
            phoneText.text = profileData.profile.phone_number ?: "Not set"
            addressText.text = profileData.profile.address ?: "Not set"
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 