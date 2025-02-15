package com.example.careplus.data.api



import com.example.careplus.data.model.profile.ProfileGetResponse
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.ProfileUpdateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): Response<ProfileGetResponse>

    @PATCH("profile")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): Response<ProfileUpdateResponse>
} 