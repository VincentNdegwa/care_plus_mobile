package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.SimpleProfile
import com.example.careplus.data.SessionManager

class ProfileRepository(private val sessionManager: SessionManager) {
    init {
        // Ensure ApiClient is initialized
        ApiClient.create(sessionManager)
    }

    suspend fun getProfile(): SimpleProfile {
        val response = ApiClient.profileApi.getProfile()
        if (!response.error) {
            return response.data ?: throw Exception("Profile not found")
        } else {
            throw Exception(response.message ?: "Failed to fetch profile")
        }
    }
} 