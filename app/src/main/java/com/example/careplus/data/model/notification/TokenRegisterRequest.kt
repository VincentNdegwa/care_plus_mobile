package com.example.careplus.data.model.notification

data class TokenRegisterRequest(
    val token: String,
    val device_type: String = "android"
)

data class DeviceTokenResponse(
    val error: Boolean,
    val message: String,
    val data: DeviceTokenData
)

data class DeviceTokenData(
    val token: String,
    val user_id: Int,
    val device_type: String,
    val is_active: Boolean,
    val updated_at: String,
    val created_at: String,
    val id: Int
)

data class DeactivateTokenRequest(
    val token: String
)

data class DeactivateTokenResponse(
    val error: Boolean,
    val message: String
)
