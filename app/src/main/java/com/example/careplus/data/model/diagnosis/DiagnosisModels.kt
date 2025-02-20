package com.example.careplus.data.model.diagnosis

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiagnosisResponse(
    val error: Boolean,
    val data: List<Diagnosis>,
    val pagination: PaginationData
) : Parcelable

@Parcelize
data class Diagnosis(
    val id: Int,
    val diagnosis_name: String,
    val description: String?,
    val date_diagnosed: String,
    val patient: DiagnosisPatient,
    val doctor: DiagnosisDoctor,
    val medication_counts: Int
) : Parcelable

@Parcelize
data class DiagnosisPatient(
    val id: Int,
    val name: String,
    val email: String
) : Parcelable

@Parcelize
data class DiagnosisDoctor(
    val id: Int,
    val name: String,
    val email: String
) : Parcelable

@Parcelize
data class PaginationData(
    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int
) : Parcelable

data class DiagnosisFilterRequest(
    val date_from: String? = null,
    val date_to: String? = null,
    val diagnosis_name: String? = null,
    val patient_id: Int? = null,
    val doctor_id: Int? = null
) 