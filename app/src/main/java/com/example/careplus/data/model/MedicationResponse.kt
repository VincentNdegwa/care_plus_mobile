package com.example.careplus.data.model

data class MedicationResponse(
    val error: Boolean,
    val message: String?,
    val medications: List<Medication>?
) 