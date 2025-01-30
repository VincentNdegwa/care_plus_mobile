package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CaregiverResponse(
    val current_page: Int,
    val `data`: List<CaregiverData>,
    val last_page: Int,
    val per_page: Int,
    val total: Int
)
@Parcelize
data class CaregiverData(
    val email: String,
    val id: Int,
    val name: String,
    val profile: ProfileX,
    val role: String,
    val user_role: UserRole
): Parcelable

@Parcelize
data class ProfileX(
    val address: String?,
    val avatar: String?,
    val date_of_birth: String?,
    val gender: String?,
    val id: Int,
    val phone_number: String?,
    val user_id: Int
): Parcelable

@Parcelize
data class UserRole(
    val active: String,
    val agency_contact: String?,
    val agency_name: String?,
    val clinic_address: String?,
    val clinic_name: String?,
    val id: Int,
    val last_activity: String?,
    val license_issuing_body: String?,
    val license_number: String?,
    val specialization: String?,
    val user_id: Int
): Parcelable

