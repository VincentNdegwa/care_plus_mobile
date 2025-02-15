package com.example.careplus.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.careplus.R
import com.example.careplus.databinding.FragmentEditProfileBinding
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.data.model.profile.ProfileData
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.UserProfile
import java.util.Calendar

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val args: EditProfileFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupGenderDropdown()
        setupDatePicker()
        setupObservers()
        setupListeners()
        
        populateFields(args.profileData)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setPageTitle("Edit Profile")
        }
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("male", "female", "other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.genderInput.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.dobInput.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupListeners() {
        binding.changeImageButton.setOnClickListener {
            // Implement image picker
        }

        binding.saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.show()
            } else {
                binding.loadingOverlay.hide()
            }
            binding.saveButton.isEnabled = !isLoading
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar("Profile updated successfully", isError = false)
                findNavController().popBackStack()
            }.onFailure { exception ->
                showSnackbar(exception.message ?: "Failed to update profile")
            }
        }
    }

    private fun populateFields(profileData: ProfileData) {
        binding.apply {
            genderInput.setText(profileData.profile.gender)
            dobInput.setText(profileData.profile.date_of_birth)
            phoneInput.setText(profileData.profile.phone_number)
            addressInput.setText(profileData.profile.address)

            // Load profile image
            profileData.profile.avatar?.let { avatarUrl ->
                Glide.with(this@EditProfileFragment)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }

    private fun saveProfile() {
        val request = ProfileUpdateRequest(
            gender = binding.genderInput.text?.toString(),
            date_of_birth = binding.dobInput.text?.toString(),
            phone_number = binding.phoneInput.text?.toString(),
            address = binding.addressInput.text?.toString(),
            avatar = null // Handle image upload separately
        )
        viewModel.updateProfile(request)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = String.format("%d-%02d-%02d", year, month + 1, day)
                binding.dobInput.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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