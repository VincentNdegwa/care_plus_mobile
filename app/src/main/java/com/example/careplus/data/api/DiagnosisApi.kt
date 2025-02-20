package com.example.careplus.data.api

import com.example.careplus.data.model.diagnosis.DiagnosisFilterRequest
import com.example.careplus.data.model.diagnosis.DiagnosisResponse
import retrofit2.Response
import retrofit2.http.*

interface DiagnosisApi {
    @GET("diagnosis/patient/{patientId}")
    suspend fun getPatientDiagnoses(
        @Path("patientId") patientId: Int,
        @Query("per_page") perPage: Int? = null,
        @Query("page_number") pageNumber: Int? = null
    ): Response<DiagnosisResponse>

    @GET("diagnosis/search")
    suspend fun searchDiagnoses(
        @Query("search") query: String,
        @Query("per_page") perPage: Int? = null,
        @Query("page_number") pageNumber: Int? = null
    ): Response<DiagnosisResponse>

    @POST("diagnosis/filter")
    suspend fun filterDiagnoses(
        @Body request: DiagnosisFilterRequest
    ): Response<DiagnosisResponse>
} 