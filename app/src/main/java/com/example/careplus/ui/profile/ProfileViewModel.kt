package com.example.careplus.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.profile.ProfileData
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.UserProfile
import com.example.careplus.data.repository.ProfileRepository
import kotlinx.coroutines.launch


class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(SessionManager(application))

    private val _profile = MutableLiveData<Result<ProfileData>>()
    val profile: LiveData<Result<ProfileData>> = _profile

    private val _updateResult = MutableLiveData<Result<UserProfile>>()
    val updateResult: LiveData<Result<UserProfile>> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _profile.value = repository.getProfile()
            } catch (e: Exception) {
                _profile.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _updateResult.value = repository.updateProfile(request)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
} 