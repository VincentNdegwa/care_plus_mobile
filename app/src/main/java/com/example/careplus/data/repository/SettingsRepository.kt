package com.example.careplus.data.repository

import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.ServerResponseError
import com.example.careplus.data.model.profile.ProfileErrorResponse
import com.example.careplus.data.model.settings.Settings
import com.example.careplus.data.model.settings.UpdateSettingsRequest
import com.example.careplus.data.model.settings.UpdateSettingsResponse
import com.google.gson.Gson

class SettingsRepository(private val sessionManager: SessionManager) {
    init {
        ApiClient.create(sessionManager)
    }
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            errorBody?.let {
                val errorResponse = Gson().fromJson(it, ServerResponseError::class.java)
                errorResponse.message
            } ?: "Unknown error occurred"
        } catch (e: Exception) {
            e.message ?: "Unknown error occurred"
        }
    }

    suspend fun getSettings():Result<Settings>{
        return try {
            val response = ApiClient.settingsApi.getSettings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSettings(settings: Settings): Result<UpdateSettingsResponse>{
        return try {
            val updateSettingsRequest = UpdateSettingsRequest(
                settings=settings
            )
            val response = ApiClient.settingsApi.updateSettings(updateSettingsRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))            }
        }catch (e:Exception){
            Result.failure(e)
        }
    }
}