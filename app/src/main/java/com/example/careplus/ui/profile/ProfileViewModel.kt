package com.example.careplus.ui.profile

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.careplus.R
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.profile.ProfileData
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.UserProfile
import com.example.careplus.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(SessionManager(application))

    private val _profile = MutableLiveData<Result<ProfileData>>()
    val profile: LiveData<Result<ProfileData>> = _profile

    private val _updateResult = MutableLiveData<Result<UserProfile>>()
    val updateResult: LiveData<Result<UserProfile>> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var avatarUrl: String? = null

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

    fun setAvatarUrl(url: String) {
        avatarUrl = url
    }

    fun uploadProfileImage(file: MultipartBody.Part) = flow {
        emit(repository.uploadFile(file, "profile"))
    }.flowOn(Dispatchers.IO)

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedRequest = request.copy(
                    avatar = avatarUrl ?: request.avatar
                )
                _updateResult.value = repository.updateProfile(updatedRequest)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDisplayImageUrl(filePath: String?): String? {
        return if (!filePath.isNullOrBlank() && filePath != "null") {
            if (filePath.startsWith("http")) {
                filePath
            } else {
                // Remove leading slash if present
                val cleanPath = if (filePath.startsWith("/")) filePath.substring(1) else filePath
                "https://care.tech360.systems/$cleanPath"
            }
        } else {
            null
        }
    }

} 