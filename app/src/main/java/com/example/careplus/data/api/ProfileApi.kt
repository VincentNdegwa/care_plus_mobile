package com.example.careplus.data.api

import com.example.careplus.data.model.ProfileResponse
import retrofit2.http.GET

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): ProfileResponse
} 