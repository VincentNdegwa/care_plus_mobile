package com.example.careplus.data.model

data class MedicationFormResource(
    val id: Int,
    val name: String,
    val patient_id: Int?
)

data class MedicationRouteResource(
    val id: Int,
    val name: String,
    val description: String
)

data class MedicationUnitResource(
    val id: Int,
    val name: String
)

data class MedicationFrequencyResource(
    val id: Int,
    val frequency: String
) 