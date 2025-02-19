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

data class TakeNowRequest(
    val medication_id: Int,
    val date_time:String
)

data class TakeNowResponse(
    val error: Boolean,
    val message: String,
    val data: TakeNowData
)

data class TakeNowData(
    val id: Int,
    val medication_id: Int,
    val patient_id: Int,
    val dose_time: String,
    val processed_at: String?,
    val status: String,
    val taken_at: String?,
    val second_notification_sent: Int,
    val created_at: String,
    val updated_at: String
)