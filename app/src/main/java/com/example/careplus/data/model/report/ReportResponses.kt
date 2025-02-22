package com.example.careplus.data.model.report

data class MedicationVsSideEffectCountsResponse(
    val error: Boolean,
    val data: List<MedicationSideEffectCount>,
    val message: String
)

data class MedicationSideEffectCount(
    val medication_id: Int,
    val medication_name: String,
    val side_effect_count: Int
)

data class TopSideEffectsResponse(
    val error: Boolean,
    val data: List<TopSideEffect>,
    val message: String
)

data class TopSideEffect(
    val patient_id: Int,
    val patient_name: String,
    val side_effect: String,
    val severity: String,
    val datetime: String,
    val duration: String?,
    val notes: String?
)

data class MostMissedMedicationsResponse(
    val error: Boolean,
    val data: List<MissedMedication>,
    val message: String
)

data class MissedMedication(
    val medication_id: Int,
    val medication_name: String,
    val missed_count: Int
)

data class MedicalAdherenceReportResponse(
    val error: Boolean,
    val data: MedicalAdherenceData,
    val message: String
)

data class MedicalAdherenceData(
    val total_scheduled: Int,
    val total_taken: Int,
    val adherence_percentage: Double,
    val from_date: String,
    val to_date: String
)

data class MedicationAdherenceByMedicationResponse(
    val error: Boolean,
    val data: List<MedicationAdherence>,
    val message: String
)

data class MedicationAdherence(
    val medication_id: Int,
    val medication_name: String,
    val total_scheduled: Int,
    val total_taken: Int,
    val adherence_percentage: Double
) 