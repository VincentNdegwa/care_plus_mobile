package com.example.careplus.data.api

import com.example.careplus.data.model.report.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReportApi {
    @GET("reports/medication-progress")
    suspend fun getMedicationProgress(
        @Query("medication_id") medicationId: Int
    ): Response<MedicationProgressResponse>

    @POST("reports/medication-vs-side-effect-counts")
    suspend fun getMedicationVsSideEffectCounts(
        @Body request: MedicationVsSideEffectCountsRequest
    ): Response<MedicationVsSideEffectCountsResponse>

    @POST("reports/top-side-effects")
    suspend fun getTopSideEffects(
        @Body request: TopSideEffectsRequest
    ): Response<TopSideEffectsResponse>

    @POST("reports/most-missed-medications")
    suspend fun getMostMissedMedications(
        @Body request: MostMissedMedicationsRequest
    ): Response<MostMissedMedicationsResponse>

    @POST("reports/medical-adherence-report")
    suspend fun getMedicalAdherenceReport(
        @Body request: MedicalAdherenceReportRequest
    ): Response<MedicalAdherenceReportResponse>

    @POST("reports/medication-adherence-by-medication")
    suspend fun getMedicationAdherenceByMedication(
        @Body request: MedicationAdherenceByMedicationRequest
    ): Response<MedicationAdherenceByMedicationResponse>
}