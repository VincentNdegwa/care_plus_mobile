package com.example.careplus.data.api

import com.example.careplus.data.model.CaregiverResponse
import retrofit2.http.GET

interface CaregiverApi {
    @GET("care-providers/fetch-all")
    suspend fun fetchAllCaregivers(): CaregiverResponse
} 