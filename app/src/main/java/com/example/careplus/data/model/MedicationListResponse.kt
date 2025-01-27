package com.example.careplus.data.model

data class MedicationListResponse(
    val error: Boolean,
    val data: List<MedicationDetails>?,
    val pagination: PaginationData?
)

data class MedicationDetails(
    val id: Long,
    val patient: PatientInfo,
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form: MedicationForm,
    val route: MedicationRoute,
    val frequency: String,
    val duration: String,
    val prescribed_date: String,
    val doctor: DoctorInfo?,
    val caregiver: CaregiverInfo?,
    val stock: Int,
    val active: Int,
    val diagnosis: String?
)

data class PatientInfo(
    val patient_id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)

data class MedicationForm(
    val id: Int,
    val name: String,
    val patient_id: Int?
)

data class MedicationRoute(
    val id: Int,
    val name: String,
    val description: String
)

data class PaginationData(
    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int
)

data class DoctorInfo(
    val id: Int,
    val name: String
)

data class CaregiverInfo(
    val id: Int,
    val name: String
) 