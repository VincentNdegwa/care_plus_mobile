package com.example.careplus.data.api

import com.example.careplus.data.model.DashboardResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DashboardApi {
    @GET("dashboard/patient-data/{patientId}")
    suspend fun getPatientData(@Path("patientId") patientId: Int): DashboardResponse
} 