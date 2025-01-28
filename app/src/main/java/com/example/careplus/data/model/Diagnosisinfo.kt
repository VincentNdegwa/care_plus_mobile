package com.example.careplus.data.model

data class Diagnosisinfo(
    val date_diagnosed: String,
    val description: Any,
    val diagnosis_name: String,
    val doctor_id: Int,
    val id: Int,
    val patient_id: Int,
    val symptoms: Any
)