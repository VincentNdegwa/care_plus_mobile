package com.example.careplus.data.api

import com.example.careplus.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>

    @GET("profile")
    suspend fun getProfile(): Response<AuthProfileResponse>

    @GET("medication-schedules/{patientId}")
    suspend fun getMedicationSchedules(
        @Path("patientId") patientId: Int,
        @Query("today_date") date: String
    ): Response<MedicationScheduleResponse>
    
    @GET("dashboard/patient-data/{patientId}")
    suspend fun getPatientData(@Path("patientId") patientId: Int): DashboardResponse
}