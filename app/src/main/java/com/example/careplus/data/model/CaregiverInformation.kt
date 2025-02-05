package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Caregiverinfo(
    val id: Long,
    val name: String
) : Parcelable