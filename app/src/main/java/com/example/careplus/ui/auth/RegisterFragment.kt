package com.example.careplus.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.careplus.R
import com.example.careplus.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.registerButton.isEnabled = !isLoading
            binding.registerButton.text = if (isLoading) "Creating Account..." else "Create Account"
        }

        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (!response.error) {
                    // Registration successful, navigate to home
                    findNavController().navigate(R.id.action_register_to_home)
                } else {
                    showError(response.message)
                }
            }.onFailure { exception ->
                showError(exception.message ?: "An error occurred")
            }
        }
    }

    private fun setupClickListeners() {
        binding.loginText.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            
            if (validateInput(name, email, password)) {
                viewModel.register(name, email, password, password, "Patient")
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameLayout.error = "Name cannot be empty"
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

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