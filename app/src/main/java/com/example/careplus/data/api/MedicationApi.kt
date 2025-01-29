package com.example.careplus.data.api

import com.example.careplus.data.model.MedicationListResponse
import com.example.careplus.data.model.MedicationRequest
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUnitResource
import com.example.careplus.data.model.MedicationFrequencyResource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH

interface MedicationApi {
    @POST("medications/fetch/by-patient")
    suspend fun getMedications(@Body request: MedicationRequest): MedicationListResponse

    @GET("medications/{id}")
    suspend fun getMedicationById(@Path("id") id: Int): MedicationDetails

    @PATCH("medications/update/{id}")
    suspend fun updateMedication(
        @Path("id") id: Long,
        @Body updateRequest: MedicationUpdateRequest
    )

    @GET("medications/medication-resources/forms")
    suspend fun getMedicationForms(): List<MedicationFormResource>

    @GET("medications/medication-resources/routes")
    suspend fun getMedicationRoutes(): List<MedicationRouteResource>

    @GET("medications/medication-resources/units")
    suspend fun getMedicationUnits(): List<MedicationUnitResource>

    @GET("medications/medication-resources/frequencies")
    suspend fun getMedicationFrequencies(): List<MedicationFrequencyResource>
} 