package com.example.careplus.data.model

data class AuthProfileResponse(
    val error: Boolean,
    val data: UserProfile
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val email_verified_at: String?,
    val caregiver: Caregiver?,
    val patient: Patient?,
    val doctor: Doctor?,
    val profile: CommonProfile
)

data class CommonProfile(
    val id: Int,
    val user_id: Int,
    val gender: String?,
    val date_of_birth: String?,
    val address: String?,
    val phone_number: String?,
    val avatar: String?
)

data class Caregiver(
    val id: Int,
    val user_id: Int,
    val specialization: String?,
    val last_activity: String?,
    val agency_name: String?,
    val agency_contact: String?
)

data class Patient(
    val id: Int,
    val user_id: Int
)

data class Doctor(
    val id: Int,
    val user_id: Int,
    val specialization: String?,
    val last_activity: String?,
    val license_number: String?,
    val license_issuing_body: String?,
    val clinic_name: String?,
    val clinic_address: String?,
    val active: String
) 