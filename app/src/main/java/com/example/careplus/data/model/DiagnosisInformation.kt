package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Diagnosisinfo(
    val date_diagnosed: String,
    val description: String?,
    val diagnosis_name: String,
    val doctor_id: Int,
    val id: Int,
    val patient_id: Int,
    val symptoms: String?
): Parcelable