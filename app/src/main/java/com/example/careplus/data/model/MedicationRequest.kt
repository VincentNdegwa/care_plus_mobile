package com.example.careplus.data.model

data class MedicationRequest(
    val patient_id: Int,
    val per_page: Int? = null,
    val page_number: Int? = null,
)
data class CreateMedicationRequest(
    val diagnosis_id: Int?,
    val dosage_quantity: String,
    val dosage_strength: String,
    val duration: String?,
    val form_id: Int?,
    val frequency: String,
    val medication_name: String,
    val patient_id: Int,
    val prescribed_date: String? = null,
    val route_id: Int?,
    val stock: Int?
)