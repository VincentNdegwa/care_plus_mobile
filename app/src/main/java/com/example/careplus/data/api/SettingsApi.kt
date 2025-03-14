package com.example.careplus.data.api

import com.example.careplus.data.model.settings.Settings
import com.example.careplus.data.model.settings.TimezoneResponse
import com.example.careplus.data.model.settings.UpdateSettingsRequest
import com.example.careplus.data.model.settings.UpdateSettingsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SettingsApi {
    @GET("settings")
    suspend fun getSettings(): Response<Settings>

    @POST("settings")
    suspend fun updateSettings(@Body settings: UpdateSettingsRequest): Response<UpdateSettingsResponse>

    @GET("timezone")
    suspend fun searchTimezones(@Query("search") query: String): Response<List<TimezoneResponse>>
}