package com.example.careplus.data.repository

import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.*
import com.example.careplus.data.SessionManager
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(private val sessionManager: SessionManager) {
    private val api = ApiClient.create(sessionManager)

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                sessionManager.clearSession()
                Result.failure(Exception("Invalid credentials"))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        role:String
    ): Result<AuthResponse> {
        return try {
            val response = api.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    password_confirmation = passwordConfirmation,
                    role=role
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<AuthResponse> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserProfile> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                if (!response.body()!!.error) {
                    val profile = response.body()!!.data
                    sessionManager.saveUser(User(
                        id = profile.id,
                        name = profile.name,
                        email = profile.email,
                        role = profile.role,
                        email_verified_at = profile.email_verified_at,
                        patient = profile.patient,
                        caregiver = profile.caregiver,
                        doctor = profile.doctor,
                        avatar = profile.profile.avatar
                    ))
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Failed to get profile"))
                }
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

    suspend fun getMedicationSchedules(patientId: Int, date: String): Result<List<Schedule>> {
        return try {
            val response = api.getMedicationSchedules(patientId, date)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.schedules?.medications?.let { medications ->
                    Result.success(medications)
                } ?: Result.failure(Exception("No medication schedules found"))
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

    suspend fun getDashboardStats(patientId: Int): Result<DashboardResponse> {
        return try {
            val response = ApiClient.dashboardApi.getPatientData(patientId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMedicationById(patientId: Int):MedicationDetails {
        val response  = ApiClient.medicationApi.getMedicationById(patientId)
        return response
    }

    suspend fun changePassword(currentPassword: String, newPassword: String, newPasswordConfirmation: String): Result<AuthResponse> {
        return try {
            val response = api.changePassword(
                ChangePasswordRequest(
                    current_password = currentPassword,
                    new_password = newPassword,
                    new_password_confirmation = newPasswordConfirmation
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            errorBody?.let {
                val errorResponse = Gson().fromJson(it, ServerResponseError::class.java)
                errorResponse.message
            } ?: "Unknown error occurred"
        } catch (e: Exception) {
            e.message ?: "Unknown error occurred"
        }
    }
}