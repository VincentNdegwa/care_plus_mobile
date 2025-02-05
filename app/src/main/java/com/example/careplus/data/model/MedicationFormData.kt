package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicationFormData(
    val id: Long,
    val name: String,
    val patient_id: Long?
) : Parcelable