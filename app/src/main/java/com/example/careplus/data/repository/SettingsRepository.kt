package com.example.careplus.data.repository

import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.settings.Settings

class SettingsRepository(private val sessionManager: SessionManager) {
    init {
        ApiClient.create(sessionManager)
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
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}