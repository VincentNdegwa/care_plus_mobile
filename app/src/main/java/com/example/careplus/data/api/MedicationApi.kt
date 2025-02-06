package com.example.careplus.data.api

import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.model.CreateMedicationRequest
import com.example.careplus.data.model.CreateMedicationResponse
import com.example.careplus.data.model.MedicationListResponse
import com.example.careplus.data.model.MedicationRequest
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUnitResource
import com.example.careplus.data.model.MedicationFrequencyResource
import com.example.careplus.data.model.MedicationUpdateResponse
import com.example.careplus.ui.medications.CreateScheduleRequest
import com.example.careplus.ui.medications.GenerateScheduleTimesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH

interface MedicationApi {
    @POST("medications/fetch/by-patient")
    suspend fun getMedications(@Body request: FilterMedications): MedicationListResponse

    @GET("medications/{id}")
    suspend fun getMedicationById(@Path("id") id: Int): MedicationDetails

    @PATCH("medications/update/{id}")
    suspend fun updateMedication(
        @Path("id") id: Long,
        @Body updateRequest: MedicationUpdateRequest
    ): Response<MedicationUpdateResponse>

    @POST("medications/create")
    suspend fun createMedication(
        @Body createMedicationRequest: CreateMedicationRequest
    ): Response<CreateMedicationResponse>

    @GET("medications/medication-resources/forms")
    suspend fun getMedicationForms(): List<MedicationFormResource>

    @GET("medications/medication-resources/routes")
    suspend fun getMedicationRoutes(): List<MedicationRouteResource>

    @GET("medications/medication-resources/units")
    suspend fun getMedicationUnits(): List<MedicationUnitResource>

    @GET("medications/medication-resources/frequencies")
    suspend fun getMedicationFrequencies(): List<MedicationFrequencyResource>

    @POST("medications/schedule/generate-time")
    suspend fun generateScheduleTimes(@Body request: GenerateScheduleTimesRequest): Response<List<String>>

    @POST("/medications/schedule/custom")
    suspend fun createSchedule(@Body request: CreateScheduleRequest): Response<Unit>
} 