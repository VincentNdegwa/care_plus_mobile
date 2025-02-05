package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DoctorInformation(
    val id: Long,
    val name: String
) : Parcelable 