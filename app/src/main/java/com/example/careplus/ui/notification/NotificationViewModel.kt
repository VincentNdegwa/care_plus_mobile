package com.example.careplus.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.notification.TokenRegisterRequest
import com.example.careplus.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = NotificationRepository(sessionManager)

    private val _tokenRegistrationResult = MutableLiveData<Result<Boolean>>()
    val tokenRegistrationResult: LiveData<Result<Boolean>> = _tokenRegistrationResult

    fun registerToken(token: TokenRegisterRequest) {
        viewModelScope.launch {
            try {
                val result = repository.registerToken(token)
                result.onSuccess { response ->
                    if (!response.error) {
                        sessionManager.saveFcmToken(token.token)
                        _tokenRegistrationResult.value = Result.success(true)
                    } else {
                        _tokenRegistrationResult.value = Result.failure(Exception(response.message))
                    }
                }.onFailure {
                    _tokenRegistrationResult.value = Result.failure(it)
                }
            } catch (e: Exception) {
                _tokenRegistrationResult.value = Result.failure(e)
            }
        }
    }

    fun deactivateToken(token: String) {
        viewModelScope.launch {
            try {
                repository.deactivateToken(token)
                sessionManager.saveFcmToken(token)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
} 