package com.example.careplus.data.model

data class SetDoctorRequest(
    val doctor_id: Int,
    val patient_id: Int,
    val isMain: Boolean = false
)

data class SetCaregiverRequest(
    val caregiver_id: Int,
    val patient_id: Int,
    val relation: String
)

data class RemoveCaregiverRequest(
    val caregiver_id: Int,
    val patient_id: Int
)

data class RemoveDoctorRequest(
    val doctor_id: Int,
    val patient_id: Int
)

data class DoctorRelationResponse(
    val error: Boolean,
    val message: String,
    val Doctor: DoctorRelation?,
    val errors: Map<String, List<String>>? = null
)

data class DoctorRelation(
    val id: Int,
    val patient_id: Int,
    val doctor_id: Int,
    val isMain: Boolean,
    val created_at: String,
    val updated_at: String
)

data class CaregiverRelationResponse(
    val error: Boolean,
    val message: String,
    val Caregiver: CaregiverRelation?
)

data class CaregiverRelation(
    val id: Int,
    val caregiver_id: Int,
    val patient_id: Int,
    val relation: String,
    val created_at: String,
    val updated_at: String
)

data class RemoveRelationResponse(
    val error: Boolean,
    val message: String
) 