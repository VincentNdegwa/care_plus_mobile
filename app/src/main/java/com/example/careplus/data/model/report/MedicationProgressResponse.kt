package com.example.careplus.data.model.report

data class MedicationProgressResponse(
    val progress: Int = 0,
    val total_schedules: Int = 0,
    val completed_schedules: Int = 0,
    val taken_schedules: Int = 0
)
