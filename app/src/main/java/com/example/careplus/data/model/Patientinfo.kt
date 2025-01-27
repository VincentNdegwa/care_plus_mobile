package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patientinfo(
    val patient_id: Long,
    val name: String,
    val email: String,
    val avatar: String?
) : Parcelable