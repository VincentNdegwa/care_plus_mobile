package com.example.careplus.data.model

import com.google.gson.annotations.SerializedName

data class CreateMedicationScheduleResponse(
    val error: Boolean,
    val message: String,
    val data: ScheduleData
)

data class CreatedScheduleData(
    val id: Int,
    @SerializedName("medication_id")
    val medicationId: Int,
    @SerializedName("patient_id")
    val patientId: Int,
    @SerializedName("dose_time")
    val doseTime: String,
    @SerializedName("processed_at")
    val processedAt: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val medication: ScheduleMedication
)

data class ScheduleMedication(
    val id: Int,
    @SerializedName("patient_id")
    val patientId: Int,
    @SerializedName("diagnosis_id")
    val diagnosisId: Int?,
    @SerializedName("medication_name")
    val medicationName: String,
    @SerializedName("dosage_quantity")
    val dosageQuantity: String,
    @SerializedName("dosage_strength")
    val dosageStrength: String,
    @SerializedName("form_id")
    val formId: Int,
    @SerializedName("route_id")
    val routeId: Int,
    val frequency: String,
    val duration: String,
    @SerializedName("prescribed_date")
    val prescribedDate: String,
    @SerializedName("doctor_id")
    val doctorId: Int?,
    @SerializedName("caregiver_id")
    val caregiverId: Int?,
    val stock: Int,
    val active: Int,
    val tracker: MedicationTracker
)

data class MedicationTracker(
    val id: Int,
    @SerializedName("medication_id")
    val medicationId: Int,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("next_start_month")
    val nextStartMonth: String,
    @SerializedName("stop_date")
    val stopDate: String,
    val duration: String,
    val frequency: String,
    val schedules: String, // This is a JSON string array
    val timezone: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) 