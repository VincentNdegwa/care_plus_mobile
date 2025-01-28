package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class MedicationListResponse(
    val error: Boolean,
    val data: List<MedicationDetails>?,
    val pagination: PaginationData?
)

@Parcelize
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
) : Parcelable

@Parcelize
data class PatientInfo(
    val patient_id: Long,
    val name: String,
    val email: String,
    val avatar: String?
) : Parcelable

@Parcelize
data class MedicationForm(
    val id: Long,
    val name: String,
    val patient_id: Long?
) : Parcelable

@Parcelize
data class MedicationRoute(
    val id: Long,
    val name: String,
    val description: String
) : Parcelable

data class PaginationData(
    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int
)

@Parcelize
data class DoctorInfo(
    val id: Long,
    val name: String
) : Parcelable

@Parcelize
data class CaregiverInfo(
    val id: Long,
    val name: String
) : Parcelable 