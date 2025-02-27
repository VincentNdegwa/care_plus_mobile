package com.example.careplus.data.api

import com.example.careplus.data.model.settings.Settings
import retrofit2.Response
import retrofit2.http.GET

interface SettingsApi {
    @GET("settings")
    suspend fun getSettings(): Response<Settings>
}