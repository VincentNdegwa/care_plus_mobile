package com.example.careplus.data.api

import com.example.careplus.data.model.*
import com.example.careplus.data.filter_model.FilterCareProviders
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CaregiverApi {
    @GET("care-providers/fetch-all")
    suspend fun fetchAllCaregivers(
        @QueryMap(encoded = true) filter: Map<String, String> = emptyMap()
    ): CaregiverResponse

    @GET("care-providers/fetch-patient-doctors/{patientId}")
    suspend fun fetchMyDoctors(
        @Path("patientId") patientId: Int,
        @QueryMap(encoded = true) filter: Map<String, String> = emptyMap()
    ): CaregiverResponse

    @GET("care-providers/fetch-patient-caregivers/{patientId}")
    suspend fun fetchMyCaregivers(
        @Path("patientId") patientId: Int,
        @QueryMap(encoded = true) filter: Map<String, String> = emptyMap()
    ): CaregiverResponse

    @POST("care-providers/set-doctor")
    suspend fun setDoctor(@Body request: SetDoctorRequest): DoctorRelationResponse

    @POST("care-providers/set-caregiver")
    suspend fun setCaregiver(@Body request: SetCaregiverRequest): CaregiverRelationResponse

    @POST("care-providers/remove-caregiver")
    suspend fun removeCaregiver(@Body request: RemoveCaregiverRequest): RemoveRelationResponse

    @POST("care-providers/remove-doctor")
    suspend fun removeDoctor(@Body request: RemoveDoctorRequest): RemoveRelationResponse
} 