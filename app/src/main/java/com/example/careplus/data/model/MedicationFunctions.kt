package com.example.careplus.data.model

import com.google.gson.annotations.SerializedName

// Request Models
data class TakeMedicationRequest(
    @SerializedName("medication_schedule_id")
    val medication_schedule_id: Int,
    @SerializedName("taken_at")
    val taken_at: String
)

data class StopMedicationRequest(
    @SerializedName("medication_id")
    val medication_id: Int
)

data class SnoozeMedicationRequest(
    @SerializedName("medication_schedule_id")
    val medication_schedule_id: Int,
    @SerializedName("snooze_minutes")
    val snooze_minutes: Int
)

data class ResumeMedicationRequest(
    @SerializedName("medication_id")
    val medication_id: Int,
    @SerializedName("extend_days")
    val extend_days: Boolean
)

// Response Models
data class TakeMedicationResponse(
    val error: Boolean,
    val message: String,
    val data: TakenMedicationData
)

data class TakenMedicationData(
    val id: Int,
    @SerializedName("medication_id")
    val medication_id: Int,
    @SerializedName("patient_id")
    val patient_id: Int,
    @SerializedName("dose_time")
    val dose_time: String,
    @SerializedName("processed_at")
    val processed_at: String?,
    val status: String,
    @SerializedName("taken_at")
    val taken_at: String?,
    @SerializedName("second_notification_sent")
    val second_notification_sent: Int,
    @SerializedName("created_at")
    val created_at: String,
    @SerializedName("updated_at")
    val updated_at: String
)

data class StopMedicationResponse(
    val error: Boolean,
    val message: String
)

data class SnoozeMedicationResponse(
    val error: Boolean,
    val message: String,
    @SerializedName("snooze_time")
    val snooze_time: String
)

data class ResumeMedicationResponse(
    val error: Boolean,
    val message: String,
    val data: ResumeMedicationData
)

data class ResumeMedicationData(
    @SerializedName("original_end_date")
    val original_end_date: String,
    @SerializedName("new_end_date")
    val new_end_date: String,
    @SerializedName("minutes_added")
    val minutes_added: Double
) 