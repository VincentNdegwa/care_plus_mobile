package com.example.careplus.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.AuthResponse
import com.example.careplus.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = AuthRepository(sessionManager)

    private val _authResult = MutableLiveData<Result<AuthResponse>>()
    val authResult: LiveData<Result<AuthResponse>> = _authResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(email, password)
            result.onSuccess { response ->
                if (!response.error && response.token != null && response.user != null) {
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUser(response.user)
                }
            }
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(name, email, password, passwordConfirmation)
            result.onSuccess { response ->
                if (!response.error && response.token != null && response.user != null) {
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUser(response.user)
                }
            }
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = repository.forgotPassword(email)
            _isLoading.value = false
        }
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun logout() {
        sessionManager.clearSession()
    }
} 