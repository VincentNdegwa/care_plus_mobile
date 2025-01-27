package com.example.careplus.data.model

data class ProfileResponse(
    val error: Boolean,
    val message: String?,
    val data: SimpleProfile?
) 