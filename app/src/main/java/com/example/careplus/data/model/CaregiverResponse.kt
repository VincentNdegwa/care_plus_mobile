package com.example.careplus.data.model

data class CaregiverResponse(
    val current_page: Int,
    val `data`: List<Data>,
    val last_page: Int,
    val per_page: Int,
    val total: Int
)

data class Data(
    val email: String,
    val id: Int,
    val name: String,
    val profile: ProfileX,
    val role: String,
    val user_role: UserRole
)

data class ProfileX(
    val address: Any,
    val avatar: Any,
    val date_of_birth: Any,
    val gender: Any,
    val id: Int,
    val phone_number: Any,
    val user_id: Int
)

data class UserRole(
    val active: String,
    val agency_contact: Any,
    val agency_name: Any,
    val clinic_address: Any,
    val clinic_name: Any,
    val id: Int,
    val last_activity: Any,
    val license_issuing_body: Any,
    val license_number: Any,
    val specialization: Any,
    val user_id: Int
)

