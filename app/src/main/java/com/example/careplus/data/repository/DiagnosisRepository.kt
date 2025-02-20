package com.example.careplus.data.repository

import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.ErrorResponse
import com.example.careplus.data.model.diagnosis.DiagnosisFilterRequest
import com.example.careplus.data.model.diagnosis.DiagnosisResponse
import com.google.gson.Gson

class DiagnosisRepository(private val sessionManager: SessionManager) {
    private val gson = Gson()

    init {
        ApiClient.create(sessionManager)
    }

    suspend fun getPatientDiagnoses(
        patientId: Int,
        perPage: Int? = null,
        pageNumber: Int? = null
    ): Result<DiagnosisResponse> {
        return try {
            val response = ApiClient.diagnosisApi.getPatientDiagnoses(patientId, perPage, pageNumber)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to get diagnoses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchDiagnoses(
        query: String,
        perPage: Int? = null,
        pageNumber: Int? = null
    ): Result<DiagnosisResponse> {
        return try {
            val response = ApiClient.diagnosisApi.searchDiagnoses(query, perPage, pageNumber)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to get diagnoses"))            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun filterDiagnoses(request: DiagnosisFilterRequest): Result<DiagnosisResponse> {
        return try {
            val response = ApiClient.diagnosisApi.filterDiagnoses(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Unauthorized"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to get diagnoses"))            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 