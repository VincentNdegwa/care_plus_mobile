package com.example.careplus.data.model

data class MedicationUpdateRequest(
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val frequency: String,
    val duration: String,
    val stock: Int
) 