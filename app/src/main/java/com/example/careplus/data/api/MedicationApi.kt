package com.example.careplus.data.api

import com.example.careplus.data.model.MedicationListResponse
import com.example.careplus.data.model.MedicationRequest
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.model.MedicationUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH

interface MedicationApi {
    @POST("medications/fetch/by-patient")
    suspend fun getMedications(@Body request: MedicationRequest): MedicationListResponse

    @GET("medications/{id}")
    suspend fun getMedicationById(@Path("id") id: Long): MedicationDetailResponse

    @PATCH("medications/update/{id}")
    suspend fun updateMedication(
        @Path("id") id: Long,
        @Body updateRequest: MedicationUpdateRequest
    )
} 