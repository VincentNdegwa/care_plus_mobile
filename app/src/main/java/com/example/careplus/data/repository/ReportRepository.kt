package com.example.careplus.data.repository

import android.util.Log
import com.example.careplus.data.SessionManager
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.ErrorResponse
import com.example.careplus.data.model.report.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

class ReportRepository(private val sessionManager: SessionManager) {
    
    init {
        ApiClient.create(sessionManager)
    }

    suspend fun getMedicationProgress(medicationId: Int): Result<MedicationProgressResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.reportApi.getMedicationProgress(medicationId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Result.failure(Exception(errorResponse?.message ?: "Failed to fetch medication progress"))
                }
            } catch (e: HttpException) {
                handleHttpException(e, "medication progress")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    suspend fun getMedicationVsSideEffectCounts(request: MedicationVsSideEffectCountsRequest): Result<MedicationVsSideEffectCountsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.reportApi.getMedicationVsSideEffectCounts(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Result.failure(Exception(errorResponse?.message ?: "Failed to fetch medication vs side effect counts"))
                }
            } catch (e: HttpException) {
                Log.e("ReportRepository", "HTTP Error", e)
                Result.failure(Exception(when (e.code()) {
                    404 -> "Report not found"
                    401 -> "Please login again"
                    else -> "Network error: ${e.message()}"
                }))
            } catch (e: Exception) {
                Log.e("ReportRepository", "Error fetching report", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTopSideEffects(request: TopSideEffectsRequest): Result<TopSideEffectsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.reportApi.getTopSideEffects(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Result.failure(Exception(errorResponse?.message ?: "Failed to fetch top side effects"))
                }
            } catch (e: HttpException) {
                handleHttpException(e, "top side effects")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    suspend fun getMostMissedMedications(request: MostMissedMedicationsRequest): Result<MostMissedMedicationsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.reportApi.getMostMissedMedications(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Result.failure(Exception(errorResponse?.message ?: "Failed to fetch most missed medications"))
                }
            } catch (e: HttpException) {
                handleHttpException(e, "most missed medications")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    suspend fun getMedicalAdherenceReport(request: MedicalAdherenceReportRequest): Result<MedicalAdherenceReportResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.reportApi.getMedicalAdherenceReport(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Result.failure(Exception(errorResponse?.message ?: "Failed to fetch medical adherence report"))
                }
            } catch (e: HttpException) {
                handleHttpException(e, "medical adherence report")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    suspend fun getMedicationAdherenceByMedication(request: MedicationAdherenceByMedicationRequest): Result<MedicationAdherenceByMedicationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.reportApi.getMedicationAdherenceByMedication(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Result.failure(Exception(errorResponse?.message ?: "Failed to fetch medication adherence by medication"))
                }
            } catch (e: HttpException) {
                handleHttpException(e, "medication adherence by medication")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleHttpException(e: HttpException, reportType: String): Result<Nothing> {
        Log.e("ReportRepository", "HTTP Error fetching $reportType", e)
        return Result.failure(Exception(when (e.code()) {
            404 -> "Report not found"
            401 -> "Please login again"
            else -> "Network error: ${e.message()}"
        }))
    }

    private fun handleException(e: Exception): Result<Nothing> {
        Log.e("ReportRepository", "Error fetching report", e)
        return Result.failure(e)
    }
} 