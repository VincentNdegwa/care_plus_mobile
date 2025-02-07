package com.example.careplus.data.model

import com.google.gson.annotations.SerializedName

data class MedicationNotificationData(
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

data class NotificationMedication(
    val id: Int,
    @SerializedName("medication_name")
    val medication_name: String,
    @SerializedName("dosage_quantity")
    val dosage_quantity: String?,
    @SerializedName("dosage_strength")
    val dosage_strength: String?
) 