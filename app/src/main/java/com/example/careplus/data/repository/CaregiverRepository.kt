package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.*
import retrofit2.HttpException
import com.google.gson.Gson
import android.util.Log

class CaregiverRepository {
    suspend fun fetchAllCaregivers(): CaregiverResponse {
        return ApiClient.caregiverApi.fetchAllCaregivers()
    }
    suspend fun fetchMyDoctors(patientId:Int): CaregiverResponse {
        return ApiClient.caregiverApi.fetchMyDoctors(patientId)
    }
    suspend fun fetchMyCaregivers(patientId:Int): CaregiverResponse {
        return ApiClient.caregiverApi.fetchMyCaregivers(patientId)
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