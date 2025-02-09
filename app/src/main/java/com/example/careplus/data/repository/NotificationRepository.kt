package com.example.careplus.data.repository

import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.notification.*

class NotificationRepository(private val sessionManager: SessionManager) {

    init {
        ApiClient.create(sessionManager)
    }

    suspend fun registerToken(token: String): Result<DeviceTokenResponse> {
        return try {
            val request = TokenRegisterRequest(token)
            val response = ApiClient.notificationApi.registerToken(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateToken(token: String): Result<DeactivateTokenResponse> {
        return try {
            val request = DeactivateTokenRequest(token)
            val response = ApiClient.notificationApi.deactivateToken(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 