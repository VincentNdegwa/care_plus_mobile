package com.example.careplus.data.repository


import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.SimpleProfile
import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.FileUploadResponse
import com.example.careplus.data.model.profile.ProfileData
import com.example.careplus.data.model.profile.ProfileErrorResponse
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.UserProfile
import com.google.gson.Gson
import okhttp3.MultipartBody
import android.util.Log

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

    suspend fun uploadFile(file: MultipartBody.Part, folder: String): Result<FileUploadResponse> {
        return try {
            Log.d("ProfileRepo", "Uploading file: ${file.body.contentLength()} bytes")
            val response = ApiClient.fileUploadApi.uploadFile(file, folder)
            Log.d("ProfileRepo", "Upload response: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("ProfileRepo", "Upload successful: ${body.data.file_path}")
                Result.success(body)
            } else {
                val error = response.errorBody()?.string()
                Log.e("ProfileRepo", "Upload failed: $error")
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Upload exception", e)
            Result.failure(e)
        }
    }
} 