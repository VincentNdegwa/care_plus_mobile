package com.example.careplus.data.model.report

data class MedicationVsSideEffectCountsRequest(
    val patient_id: Int,
    val from_date: String? = null,
    val to_date: String? = null
)

data class TopSideEffectsRequest(
    val patient_id: Int,
    val from_date: String? = null,
    val to_date: String? = null
)

data class MostMissedMedicationsRequest(
    val patient_id: Int,
    val from_date: String? = null,
    val to_date: String? = null
)

data class MedicalAdherenceReportRequest(
    val patient_id: Int,
    val medication_id: Int? = null,
    val from_date: String? = null,
    val to_date: String? = null
)

data class MedicationAdherenceByMedicationRequest(
    val patient_id: Int,
    val from_date: String? = null,
    val to_date: String? = null
) 