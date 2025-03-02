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
    val dosage_quantity: String?,
    val dosage_strength: String?,
    val form: MedicationForm?,
    val route: MedicationRoute?,
    val frequency: String?,
    val duration: String?,
    val prescribed_date: String?,
    val doctor: DoctorInfo?,
    val caregiver: CaregiverInfo?,
    val stock: Int?,
    val active: Int,
    val diagnosis: DiagnosisInformation?,
    var status: String?
) : Parcelable
//MedicationDetails(id=2, patient=PatientInfo(patient_id=1, name=Test User, email=test@example.com, avatar=null), medication_name=Sertraline, dosage_quantity=1, dosage_strength=500mg, form=null, route=null, frequency=2 times per day, duration=7 days, prescribed_date=2025-01-11 00:00:00, doctor=DoctorInfo(id=0, name=Ellis.Marquardt90), caregiver=null, stock=20, active=1, diagnosis=Diagnosisinfo(date_diagnosed=2024-11-21, description=null, diagnosis_name=Malaria, doctor_id=3, id=1, patient_id=1, symptoms=null))


@Parcelize
data class PatientInfo(
    val patient_id: Long,
    val name: String,
    val email: String,
    val avatar: String?
) : Parcelable

@Parcelize
data class MedicationForm(
    val id: Long?,
    val name: String?,
    val patient_id: Long?
) : Parcelable

@Parcelize
data class MedicationRoute(
    val id: Long?,
    val name: String?,
    val description: String?
) : Parcelable

data class PaginationData(
    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int,
    val last_page:Int
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