package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.*
import retrofit2.HttpException
import com.google.gson.Gson
import android.util.Log
import com.example.careplus.data.filter_model.FilterCareProviders

class CaregiverRepository {
    private fun FilterCareProviders?.toMap(): Map<String, String> {
        if (this == null) return emptyMap()
        return buildMap {
            agency_name?.let { if (it.isNotBlank()) put("agency_name", it) }
            gender?.let { if (it.isNotBlank()) put("gender", it) }
            role?.let { if (it.isNotBlank()) put("role", it) }
            search?.let { if (it.isNotBlank()) put("search", it) }
            specialization?.let { if (it.isNotBlank()) put("specialization", it) }
            per_page?.let { put("per_page", it.toString()) }
            page_number?.let { put("page_number", it.toString()) }
        }
    }

    suspend fun fetchAllCaregivers(filter: FilterCareProviders? = null): CaregiverResponse {
        return ApiClient.caregiverApi.fetchAllCaregivers(filter.toMap())
    }

    suspend fun fetchMyDoctors(patientId: Int, filter: FilterCareProviders? = null): CaregiverResponse {
        return ApiClient.caregiverApi.fetchMyDoctors(patientId, filter.toMap())
    }

    suspend fun fetchMyCaregivers(patientId: Int, filter: FilterCareProviders? = null): CaregiverResponse {
        return ApiClient.caregiverApi.fetchMyCaregivers(patientId, filter.toMap())
    }

    suspend fun setDoctor(doctorId: Int, patientId: Int, isMain: Boolean = false): Result<DoctorRelationResponse> {
        return try {
            val request = SetDoctorRequest(doctorId, patientId, isMain)
            val response = ApiClient.caregiverApi.setDoctor(request)
            Log.d("CaregiverRepository", "Response: $response")
            if (response.error) {
                Log.d("CaregiverRepository", "Error from response: ${response.message}")
                Result.failure(Exception(response.message))
            } else {
                Result.success(response)
            }
        } catch (e: HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CaregiverRepository", "HTTP Exception error body: $errorBody")
                val errorResponse = Gson().fromJson(errorBody, ValidationErrorResponse::class.java)
                Log.e("CaregiverRepository", "Parsed error response: $errorResponse")
                Result.failure(Exception(errorResponse.message))
            } catch (e2: Exception) {
                Log.e("CaregiverRepository", "Error parsing error response", e2)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("CaregiverRepository", "General exception", e)
            Result.failure(e)
        }
    }

    suspend fun setCaregiver(caregiverId: Int, patientId: Int, relation: String): Result<CaregiverRelationResponse> {
        return try {
            val request = SetCaregiverRequest(caregiverId, patientId, relation)
            val response = ApiClient.caregiverApi.setCaregiver(request)
            if (response.error) {
                Result.failure(Exception(response.message))
            } else {
                Result.success(response)
            }
        } catch (e: HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ValidationErrorResponse::class.java)
                Result.failure(Exception(errorResponse.message))
            } catch (e2: Exception) {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCaregiver(caregiverId: Int, patientId: Int): Result<RemoveRelationResponse> {
        return try {
            val request = RemoveCaregiverRequest(caregiverId, patientId)
            Result.success(ApiClient.caregiverApi.removeCaregiver(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeDoctor(doctorId: Int, patientId: Int): Result<RemoveRelationResponse> {
        return try {
            val request = RemoveDoctorRequest(doctorId, patientId)
            Result.success(ApiClient.caregiverApi.removeDoctor(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 