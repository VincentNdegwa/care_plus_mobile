package com.example.careplus.data.model

data class ValidationErrorResponse(
    val error: Boolean,
    val message: String,
    val errors: Map<String, List<String>>
) 