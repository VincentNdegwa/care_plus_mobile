package com.example.careplus.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val role:String
)

data class ForgotPasswordRequest(
    val email: String
)

data class AuthResponse(
    val error: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val email_verified_at: String?,
    val patient: Patient? = null,
    val caregiver: Caregiver? = null,
    val doctor: Doctor? = null,
    var avatar: String? = null
) 