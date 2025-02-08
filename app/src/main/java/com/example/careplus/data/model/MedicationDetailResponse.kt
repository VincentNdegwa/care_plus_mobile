package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicationDetailResponse(
    val id: Long,
    val patient: PatientInformation,
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form: MedicationFormData,
    val route: MedicationRouteData,
    val frequency: String,
    val duration: String,
    val prescribed_date: String,
    val doctor: DoctorInformation?,
    val caregiver: CaregiverInformation?,
    val stock: Int?,
    val active: Int,
    val diagnosis: DiagnosisInformation?
) : Parcelable

@Parcelize
data class CreateMedicationResponse(
    val error: Boolean,
    val message: String,
    val data: MedicationDetailResponse
): Parcelable


@Parcelize
data class DeleteResponse(
    val error: Boolean,
    val message: String
):Parcelable