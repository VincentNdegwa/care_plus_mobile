package com.example.careplus.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.R
import com.example.careplus.databinding.FragmentChangePasswordBinding
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.resetPasswordButton.isEnabled = !isLoading
            binding.resetPasswordButton.text = if (isLoading) "Resetting..." else "Reset Password"
        }

        viewModel.passwordChangeResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    showSuccess(response.message.toString())
                    binding.root.postDelayed({
                        findNavController().navigate(R.id.action_changePassword_to_login)
                    }, 1500)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }
    }

    private fun setupClickListeners() {
        binding.backToLoginText.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.resetPasswordButton.setOnClickListener {
            val currentPassword = binding.currentPasswordInput.text.toString()
            val newPassword = binding.newPasswordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()
            
            if (validateInput(currentPassword, newPassword, confirmPassword)) {
                viewModel.changePassword(currentPassword, newPassword, confirmPassword)
            }
        }
    }

    private fun validateInput(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        var isValid = true

        if (currentPassword.isEmpty()) {
            binding.currentPasswordLayout.error = "Current password is required"
            isValid = false
        } else {
            binding.currentPasswordLayout.error = null
        }

        if (newPassword.isEmpty() || newPassword.length < 4) {
            binding.newPasswordLayout.error = "New password must be at least 4 characters"
            isValid = false
        } else {
            binding.newPasswordLayout.error = null
        }

        if (confirmPassword != newPassword) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.surface_light))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.success))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.surface_light))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
