package com.example.careplus.data.model

data class ErrorResponse(
    val error: Boolean,
    val message: String,
    val errors: String
) 