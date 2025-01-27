package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.Medication
import com.example.careplus.data.SessionManager
import android.util.Log
import retrofit2.HttpException
import com.example.careplus.data.model.MedicationRequest
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.model.MedicationUpdateRequest

class MedicationRepository(private val sessionManager: SessionManager) {
    init {
        Log.d("MedicationRepository", "Initializing repository")
        ApiClient.create(sessionManager)
    }

    suspend fun getMedications(patientId: Int, perPage: Int? = null, pageNumber: Int? = null): List<MedicationDetails> {
        try {
            Log.d("MedicationRepository", "Getting medications for patient $patientId")
            val request = MedicationRequest(patientId, perPage, pageNumber)
            val response = ApiClient.medicationApi.getMedications(request)
            
            if (!response.error) {
                val medications = response.data ?: emptyList()
                Log.d("MedicationRepository", "Medications received: ${medications.size}")
                return medications
            } else {
                Log.e("MedicationRepository", "Error fetching medications")
                throw Exception("Failed to fetch medications")
            }
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication service is currently unavailable")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load medications: ${e.message}")
        }
    }

    suspend fun getMedicationById(id: Long): MedicationDetailResponse {
        try {
            Log.d("MedicationRepository", "Getting medication details for id: $id")
            return ApiClient.medicationApi.getMedicationById(id)
        } catch (e: HttpException) {
            Log.e("MedicationRepository", "HTTP Error: ${e.code()}", e)
            when (e.code()) {
                404 -> throw Exception("Medication not found")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error fetching medication details", e)
            throw Exception("Failed to load medication details: ${e.message}")
        }
    }

    suspend fun updateMedication(id: Long, updateData: MedicationUpdateRequest) {
        try {
            ApiClient.medicationApi.updateMedication(id, updateData)
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication not found")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to update medication: ${e.message}")
        }
    }
} 