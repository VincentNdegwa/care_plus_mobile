package com.example.careplus.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.R
import com.example.careplus.databinding.FragmentForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.resetButton.isEnabled = !isLoading
            binding.resetButton.text = if (isLoading) "Sending..." else "Send Reset Link"
        }

        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    showSuccess(response.message)
                    // Navigate back to login after short delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigateUp()
                    }, 2000)
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

        binding.resetButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (validateInput(email)) {
                viewModel.forgotPassword(email)
            }
        }
    }

    private fun validateInput(email: String): Boolean {
        return if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email"
            false
        } else {
            binding.emailLayout.error = null
            true
        }
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