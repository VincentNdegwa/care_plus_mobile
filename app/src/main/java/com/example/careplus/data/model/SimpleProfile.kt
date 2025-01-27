package com.example.careplus.data.model

data class SimpleProfileResponse(
    val error: Boolean,
    val message: String?,
    val profile: SimpleProfile?
)

data class SimpleProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val created_at: String,
    val updated_at: String
) 