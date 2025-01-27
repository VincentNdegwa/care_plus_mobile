package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicationDetailResponse(
    val id: Long,
    val patient: Patientinfo,
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form: Medicationform,
    val route: Medicationroute,
    val frequency: String,
    val duration: String,
    val prescribed_date: String,
    val doctor: Doctorinfo?,
    val caregiver: Caregiverinfo?,
    val stock: Int,
    val active: Int,
    val diagnosis: String?
) : Parcelable
