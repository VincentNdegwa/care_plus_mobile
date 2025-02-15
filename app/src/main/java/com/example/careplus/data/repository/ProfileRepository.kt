package com.example.careplus.data.repository


import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.SimpleProfile
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.profile.ProfileData
import com.example.careplus.data.model.profile.ProfileErrorResponse
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.UserProfile
import com.google.gson.Gson

class ProfileRepository(private val sessionManager: SessionManager) {
    private val gson = Gson()

    init {
        // Ensure ApiClient is initialized
        ApiClient.create(sessionManager)
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            errorBody?.let {
                val errorResponse = gson.fromJson(it, ProfileErrorResponse::class.java)
                errorResponse.message
            } ?: "Unknown error occurred"
        } catch (e: Exception) {
            e.message ?: "Unknown error occurred"
        }
    }

    suspend fun getProfile(): Result<ProfileData> {
        return try {
            val response = ApiClient.profileApi.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: ProfileUpdateRequest): Result<UserProfile> {
        return try {
            val response = ApiClient.profileApi.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.profile)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 