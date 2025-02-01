package com.example.careplus.data.filter_model

data class FilterMedications(
    val active: Boolean? = null,
    val caregiver_id: Long? = null,
    val diagnosis_id: Long? = null,
    val doctor_id: Long? = null,
    val end_date: String? = null,
    val form_id: Long? = null,
    val patient_id: Long? = null,
    val route_id: Long? = null,
    val search: String? = null,
    val start_date: String? = null,
    //for pagination
    val per_page: Int? = null,
    val page_number: Int? = null
)