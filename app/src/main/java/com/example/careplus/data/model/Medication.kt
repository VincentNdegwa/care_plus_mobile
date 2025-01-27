package com.example.careplus.data.model

data class Medication(
    val id: Long,
    val name: String,
    val dosage: String,
    val frequency: String,
    val form: String,
    val route: String,
    val duration: String,
    val prescribed_date: String,
    val doctor_id: Int?,
    val caregiver_id: Int?,
    val stock: Int,
    val active: Int
) 