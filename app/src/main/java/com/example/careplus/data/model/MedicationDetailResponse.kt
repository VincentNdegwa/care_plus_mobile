package com.example.careplus.data.model

data class MedicationDetailResponse(
    val id: Long,
    val patient: PatientInfo,
    val medication_name: String,
    val dosage_quantity: String,
    val dosage_strength: String,
    val form: MedicationForm,
    val route: MedicationRoute,
    val frequency: String,
    val duration: String,
    val prescribed_date: String,
    val doctor: DoctorInfo?,
    val caregiver: CaregiverInfo?,
    val stock: Int,
    val active: Int,
    val diagnosis: String?
) 