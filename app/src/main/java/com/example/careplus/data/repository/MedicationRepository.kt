package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.SessionManager
import android.util.Log
import retrofit2.HttpException
import com.example.careplus.data.model.MedicationRequest
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUnitResource
import com.example.careplus.data.model.MedicationFrequencyResource
import com.example.careplus.data.model.PatientInfo
import com.example.careplus.data.model.DoctorInfo
import com.example.careplus.data.model.CaregiverInfo
import com.example.careplus.data.model.MedicationForm
import com.example.careplus.data.model.MedicationRoute

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

    suspend fun getMedicationById(id: Int): MedicationDetails {
        try {
            Log.d("MedicationRepository", "Getting medication details for id: $id")
            val response = ApiClient.medicationApi.getMedicationById(id)
            // Convert MedicationDetailResponse to MedicationDetails
            return MedicationDetails(
                id = response.id,
                patient = PatientInfo(
                    patient_id = response.patient.patient_id,
                    name = response.patient.name,
                    email = response.patient.email,
                    avatar = response.patient.avatar
                ),
                medication_name = response.medication_name,
                dosage_quantity = response.dosage_quantity,
                dosage_strength = response.dosage_strength,
                form = MedicationForm(
                    id = response.form?.id,
                    name = response.form?.name,
                    patient_id = response.form?.patient_id
                ),
                route = MedicationRoute(
                    id = response.route?.id,
                    name = response.route?.name,
                    description = response.route?.description
                ),
                frequency = response.frequency,
                duration = response.duration,
                prescribed_date = response.prescribed_date,
                doctor = response.doctor?.let { DoctorInfo(it.id, it.name) },
                caregiver = response.caregiver?.let { CaregiverInfo(it.id, it.name) },
                stock = response.stock ?: 0,
                active = response.active,
                diagnosis = response.diagnosis
            )
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

    suspend fun getMedicationForms(): List<MedicationFormResource> {
        try {
            return ApiClient.medicationApi.getMedicationForms()
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication forms not available")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load medication forms: ${e.message}")
        }
    }

    suspend fun getMedicationRoutes(): List<MedicationRouteResource> {
        try {
            return ApiClient.medicationApi.getMedicationRoutes()
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication routes not available")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load medication routes: ${e.message}")
        }
    }

    suspend fun getMedicationUnits(): List<MedicationUnitResource> {
        try {
            return ApiClient.medicationApi.getMedicationUnits()
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication units not available")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load medication units: ${e.message}")
        }
    }

    suspend fun getMedicationFrequencies(): List<MedicationFrequencyResource> {
        try {
            return ApiClient.medicationApi.getMedicationFrequencies()
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication frequencies not available")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load medication frequencies: ${e.message}")
        }
    }
} 