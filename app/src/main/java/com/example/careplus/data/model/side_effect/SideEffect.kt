package com.example.careplus.data.model.side_effect

import com.example.careplus.data.model.PaginationData

data class SideEffect(
    val id: Int,
    val medication_id: Int,
    val patient_id: Int,
    val datetime: String,
    val side_effect: String,
    val severity: String,
    val duration: Int?,
    val notes: String?,
    val created_at: String,
    val updated_at: String,
    val medication: SideEffectMedication?
)
data class SideEffectMedication(
    val id: Int,
    val patient_id: Int,
    val diagnosis_id: Int?,
    val medication_name: String,
    val dosage_quantity: String?,
    val dosage_strength: String?,
    val form_id: Int?,
    val route_id: Int?,
    val frequency: String?,
    val duration: String?,
    val prescribed_date: String,
    val doctor_id: Int?,
    val caregiver_id: Int?,
    val stock: Int?,
    val active: Int
)
data class CreateSideEffectRequest(
    val medication_id: Int,
    val datetime: String,
    val side_effect: String,
    val severity: String,
    val duration: Int? = null,
    val notes: String? = null
)

data class CreateSideEffectResponse(
    val error: Boolean,
    val message: String,
    val side_effect: SideEffect
)

data class UpdateSideEffectRequest(
    val datetime: String,
    val side_effect: String,
    val severity: String,
    val duration: Int? = null,
    val notes: String? = null
)

data class UpdateSideEffectResponse(
    val error: Boolean,
    val message: String,
    val side_effect: SideEffect
)

data class FetchSideEffectsRequest(
    val patient_id: Int,
    val medication_id: Int? = null,
    val severity: String? = null,
    val from_datetime: String? = null,
    val to_datetime: String? = null,
    val per_page: Int = 10,
    val page_number: Int = 1
)

data class FetchSideEffectsResponse(
    val error: Boolean,
    val data: List<SideEffect>,
    val pagination: PaginationData
)

data class  ErrorSideExceptionMessage(
    val error: Boolean,
    val message: String,
)
data class DeleteSideEffectResponse(
    val error: Boolean,
    val message: String
)

// Enum for severity levels
enum class SideEffectSeverity(val value: String) {
    MILD("mild"),
    MODERATE("moderate"),
    SEVERE("severe")
}
