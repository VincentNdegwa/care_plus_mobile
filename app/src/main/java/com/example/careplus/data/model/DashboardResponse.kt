package com.example.careplus.data.model

data class DashboardResponse(
    val patient_stats: PatientStats,
    val health_vitals: List<HealthVital>
)

data class PatientStats(
    val medication: Stats,
    val caregiver: Stats,
    val side_effect: Stats,
    val diagnosis: Stats
)

data class Stats(
    val current: Int,
    val last: Int,
    val change: String,
    val label: String
)

data class HealthVital(
    val name: String,
    val value: String,
    val unit: String,
    val isNormal: Boolean
) 