package com.example.careplus.data.model.fcm

class FcmMessages {
}

data class FCMMedicationPayload(
    val type: String,
    val payload: MedicationPayload?
)

data class MedicationPayload(
    val id: Int,
    val medication_id: Int,
    val patient_id: Int,
    val dose_time: String,
    val processed_at: String?,
    val status: String,
    val taken_at: String?,
    val second_notification_sent: Int?,
    val created_at: String,
    val updated_at: String,
    val medication: MedicationDetail
)

data class MedicationDetail(
    val id: Int,
    val patient_id: Int,
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form_id: Int,
    val route_id: Int,
    val frequency: String,
    val duration: String,
    val prescribed_date: String,
    val doctor_id: Int?,
    val caregiver_id: Int?,
    val stock: Int,
    val active: Int
)