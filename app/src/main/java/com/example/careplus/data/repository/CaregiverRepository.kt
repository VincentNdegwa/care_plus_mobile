package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.CaregiverResponse

class CaregiverRepository {
    suspend fun fetchAllCaregivers(): CaregiverResponse {
        return ApiClient.caregiverApi.fetchAllCaregivers()
    }
    suspend fun fetchMyDoctors(patientId:Int): CaregiverResponse {
        return ApiClient.caregiverApi.fetchMyDoctors(patientId)
    }
    suspend fun fetchMyCaregivers(patientId:Int): CaregiverResponse {
        return ApiClient.caregiverApi.fetchMyCaregivers(patientId)
    }
} 