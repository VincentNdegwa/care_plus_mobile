package com.example.careplus.data.model

data class MedicationScheduleResponse(
    val error: Boolean,
    val schedules: ScheduleData?
)

data class ScheduleData(
    val now: String,
    val start_of_day: String,
    val end_of_day: String,
    val count: Int,
    val medications: List<Schedule>
)

data class Schedule(
    val id: Int,
    val medication_id: Int,
    val patient_id: Int,
    val dose_time: String,
    val processed_at: String?,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val medication: ScheduledMedication,
    val taken_at: String?,
    val second_notification_sent: Int?
)

data class ScheduledMedication(
    val id: Int,
    val patient_id: Int,
    val diagnosis_id: Int?,
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form_id: Int,
    val route_id: Int,
    val frequency: String,
    val duration: String,
    val prescribed_date: String,
    val doctor_id: Int?,
    val caregiver_id: Int?,
    val stock: Int,
    val active: Int
) 