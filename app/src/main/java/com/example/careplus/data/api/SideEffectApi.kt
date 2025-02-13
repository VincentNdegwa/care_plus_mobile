package com.example.careplus.data.api

import com.example.careplus.data.model.side_effect.*
import retrofit2.Response
import retrofit2.http.*

interface SideEffectApi {
    @POST("side-effects/create")
    suspend fun createSideEffect(
        @Body request: CreateSideEffectRequest
    ): Response<CreateSideEffectResponse>

    @POST("side-effects/fetch")
    suspend fun fetchSideEffects(
        @Body request: FetchSideEffectsRequest
    ): Response<FetchSideEffectsResponse>

    @GET("side-effects/{sideEffectId}")
    suspend fun getSideEffect(
        @Path("sideEffectId") sideEffectId: Int
    ): Response<SideEffect>

    @PATCH("side-effects/update/{sideEffectId}")
    suspend fun updateSideEffect(
        @Path("sideEffectId") sideEffectId: Int,
        @Body request: UpdateSideEffectRequest
    ): Response<UpdateSideEffectResponse>

    @DELETE("side-effects/delete/{sideEffectId}")
    suspend fun deleteSideEffect(
        @Path("sideEffectId") sideEffectId: Int
    ): Response<DeleteSideEffectResponse>
} 