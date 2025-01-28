package com.example.careplus.data.model

data class MedicationUpdateRequest(
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form_id: Number,
    val route_id: Number,
    val frequency: String,
    val duration: String,
    val stock: Int
) 