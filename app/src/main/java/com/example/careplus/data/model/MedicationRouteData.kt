package com.example.careplus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Medicationroute(
    val id: Long,
    val name: String,
    val description: String
) : Parcelable