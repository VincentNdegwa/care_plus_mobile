package com.example.careplus.data.model

data class MedicationRequest(
    val patient_id: Int,
    val per_page: Int? = null,
    val page_number: Int? = null
) 