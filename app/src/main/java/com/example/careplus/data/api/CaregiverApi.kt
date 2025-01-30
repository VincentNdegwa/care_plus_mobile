package com.example.careplus.data.api

import com.example.careplus.data.model.CaregiverResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CaregiverApi {
    @GET("care-providers/fetch-all")
    suspend fun fetchAllCaregivers(): CaregiverResponse

    @GET("care-providers/fetch-patient-doctors/{patientId}")
    suspend fun fetchMyDoctors(@Path("patientId") patientId: Int): CaregiverResponse

    @GET("care-providers/fetch-patient-caregivers/{patientId}")
    suspend fun fetchMyCaregivers(@Path("patientId") patientId: Int): CaregiverResponse
} 