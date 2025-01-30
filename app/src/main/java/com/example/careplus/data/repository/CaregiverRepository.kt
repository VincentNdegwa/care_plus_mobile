package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.CaregiverResponse

class CaregiverRepository {
    suspend fun fetchAllCaregivers(): CaregiverResponse {
        return ApiClient.caregiverApi.fetchAllCaregivers()
    }
} 