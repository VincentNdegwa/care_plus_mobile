package com.example.careplus.data.model

data class MedicationUpdateResponse(
    val error: Boolean,
    val medication: MedicationUpdated,
    val message: String
)

data class MedicationUpdated(
    val active: Int,
    val caregiver_id: Any,
    val diagnosis_id: Int,
    val doctor_id: Int,
    val dosage_quantity: String,
    val dosage_strength: String,
    val duration: String,
    val form_id: Any,
    val frequency: String,
    val id: Int,
    val medication_name: String,
    val patient_id: Int,
    val prescribed_date: String,
    val route_id: Any,
    val stock: Int
)