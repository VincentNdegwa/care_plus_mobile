package com.example.careplus.data.repository

import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.side_effect.*
import com.google.gson.Gson
import retrofit2.HttpException

class SideEffectRepository(private val sessionManager: SessionManager) {
    private val gson = Gson()

    init {
        ApiClient.create(sessionManager)
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            errorBody?.let {
                val errorResponse = gson.fromJson(it, ErrorSideExceptionMessage::class.java)
                errorResponse.message
            } ?: errorBody.toString()
        } catch (e: Exception) {
            e.message.toString()
        }
    }

    suspend fun createSideEffect(request: CreateSideEffectRequest): Result<SideEffect> {
        return try {
            val response = ApiClient.sideEffectApi.createSideEffect(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.side_effect)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseErrorMessage(e.response()?.errorBody()?.string())
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchSideEffects(request: FetchSideEffectsRequest): Result<FetchSideEffectsResponse> {
        return try {
            val response = ApiClient.sideEffectApi.fetchSideEffects(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSideEffect(sideEffectId: Int): Result<SideEffect> {
        return try {
            val response = ApiClient.sideEffectApi.getSideEffect(sideEffectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSideEffect(
        sideEffectId: Int,
        request: UpdateSideEffectRequest
    ): Result<SideEffect> {
        return try {
            val response = ApiClient.sideEffectApi.updateSideEffect(sideEffectId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.side_effect)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSideEffect(sideEffectId: Int): Result<Boolean> {
        return try {
            val response = ApiClient.sideEffectApi.deleteSideEffect(sideEffectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(!response.body()!!.error)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 