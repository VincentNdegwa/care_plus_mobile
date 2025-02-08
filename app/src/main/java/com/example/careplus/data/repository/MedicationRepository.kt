package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.SessionManager
import android.util.Log
import com.example.careplus.data.filter_model.FilterMedications
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
import com.example.careplus.data.model.CreateMedicationRequest
import com.example.careplus.data.model.CreateMedicationResponse
import com.example.careplus.data.model.CreateMedicationScheduleResponse
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.model.MedicationForm
import com.example.careplus.data.model.MedicationListResponse
import com.example.careplus.data.model.MedicationRoute
import com.example.careplus.data.model.MedicationUpdateResponse
import com.example.careplus.ui.medications.CreateScheduleRequest
import com.example.careplus.ui.medications.GenerateScheduleTimesRequest
import com.example.careplus.data.model.ErrorResponse
import com.example.careplus.data.model.*
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicationRepository(private val sessionManager: SessionManager) {
    init {
        Log.d("MedicationRepository", "Initializing repository")
        ApiClient.create(sessionManager)
    }

    suspend fun getMedications(patientId: Int, filter:FilterMedications? ): MedicationListResponse {
        try {
            Log.d("MedicationRepository", "Getting medications for patient $patientId")
            val request = filter?.copy(patient_id = patientId.toLong())
                ?: FilterMedications(patient_id = patientId.toLong())
            val response = ApiClient.medicationApi.getMedications(request)
            
            if (!response.error) {
                return response
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

    suspend fun updateMedication(id: Long, updateData: MedicationUpdateRequest): Result<MedicationUpdateResponse> {
        try {
            val response = ApiClient.medicationApi.updateMedication(id, updateData)
            if (response.isSuccessful && response.body() != null){
                return Result.success(response.body()!!)
            }
            return  Result.failure(Exception("Unable to update medication"))
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

    suspend fun createMedication(request: CreateMedicationRequest):Result<CreateMedicationResponse>{
        try {
            val response =  ApiClient.medicationApi.createMedication(request)
            return if (response.isSuccessful && response.body() != null){
                Result.success(response.body()!!)
            }else{
                Result.failure(Exception("Unable to create medication"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("Medication not available")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to create medication: ${e.message}")
        }
    }

    suspend fun generateScheduleTimes(request: GenerateScheduleTimesRequest): Result<List<String>> {

        try {
            val response = ApiClient.medicationApi.generateScheduleTimes(request)
            return if (response.isSuccessful && response.body() != null){
                Result.success(response.body()!!)
            }else{
                Result.failure(Exception("Unable to generate schedule"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("404 not available")
                401 -> throw Exception("Please login again")
                else -> throw Exception("Network error: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to generate schedule time: ${e.message}")
        }
    }

    suspend fun createSchedule(request: CreateScheduleRequest): Result<CreateMedicationScheduleResponse> {
        try {
            val response = ApiClient.medicationApi.createSchedule(request)
            Log.d("MedicationRepository", "Response: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                return Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("MedicationRepository", "Error body: $errorBody")
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                return Result.failure(Exception(errorResponse?.message ?: "Unable to create schedule"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            return try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to create schedule: ${e.message}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun takeMedication(scheduleId: Int, takenAt: LocalDateTime? = null): Result<TakeMedicationResponse> {
        try {
            val request = TakeMedicationRequest(
                medication_schedule_id = scheduleId,
                taken_at = (takenAt ?: LocalDateTime.now())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
            
            val response = ApiClient.medicationApi.takeMedication(request)
            return if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to mark medication as taken"))
            }
        } catch (e: HttpException) {
            Log.e("MedicationRepository", "HTTP Error taking medication", e)
            return Result.failure(Exception(when (e.code()) {
                404 -> "Schedule not found"
                401 -> "Please login again"
                else -> "Network error: ${e.message()}"
            }))
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error taking medication", e)
            return Result.failure(e)
        }
    }

    suspend fun stopMedication(medicationId: Int): Result<StopMedicationResponse> {
        try {
            val request = StopMedicationRequest(medication_id = medicationId)
            val response = ApiClient.medicationApi.stopMedication(request)
            
            return if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to stop medication"))
            }
        } catch (e: HttpException) {
            Log.e("MedicationRepository", "HTTP Error stopping medication", e)
            return Result.failure(Exception(when (e.code()) {
                404 -> "Medication not found"
                401 -> "Please login again"
                else -> "Network error: ${e.message()}"
            }))
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error stopping medication", e)
            return Result.failure(e)
        }
    }

    suspend fun snoozeMedication(scheduleId: Int, minutes: Int): Result<SnoozeMedicationResponse> {
        try {
            val request = SnoozeMedicationRequest(
                medication_schedule_id = scheduleId,
                snooze_minutes = minutes
            )
            
            val response = ApiClient.medicationApi.snoozeMedication(request)
            return if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to snooze medication"))
            }
        } catch (e: HttpException) {
            Log.e("MedicationRepository", "HTTP Error snoozing medication", e)
            return Result.failure(Exception(when (e.code()) {
                404 -> "Schedule not found"
                401 -> "Please login again"
                else -> "Network error: ${e.message()}"
            }))
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error snoozing medication", e)
            return Result.failure(e)
        }
    }

    suspend fun resumeMedication(medicationId: Int, extendDays: Boolean = false): Result<ResumeMedicationResponse> {
        try {
            val request = ResumeMedicationRequest(
                medication_id = medicationId,
                extend_days = extendDays
            )
            
            val response = ApiClient.medicationApi.resumeMedication(request)
            return if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.message ?: "Failed to resume medication"))
            }
        } catch (e: HttpException) {
            Log.e("MedicationRepository", "HTTP Error resuming medication", e)
            return Result.failure(Exception(when (e.code()) {
                404 -> "Medication not found"
                401 -> "Please login again"
                else -> "Network error: ${e.message()}"
            }))
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error resuming medication", e)
            return Result.failure(e)
        }
    }

    suspend fun deleteMedication(medicationId: Int): Result<DeleteResponse> {
        return try {
            val response = ApiClient.medicationApi.deleteMedication(medicationId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 