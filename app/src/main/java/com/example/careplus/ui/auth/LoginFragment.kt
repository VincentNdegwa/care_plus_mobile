package com.example.careplus.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.R
import com.example.careplus.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loginButton.isEnabled = !isLoading
            binding.loginButton.text = if (isLoading) "Logging in..." else "Login"
        }

        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    // Login successful, navigate to home
                    findNavController().navigate(R.id.action_login_to_home)
                } else {
                    // Show error message
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }
    }

    private fun setupClickListeners() {
        binding.registerText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            
            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password cannot be empty"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.surface_light))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 