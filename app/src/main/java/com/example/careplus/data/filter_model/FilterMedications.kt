package com.example.careplus.data.filter_model

data class FilterMedications(
    val active: Boolean? = null ,
    val caregiver_id: Int? = null,
    val diagnosis_id: Int? = null,
    val doctor_id: Int? = null,
    val end_date: String? = null,
    val form_id: Int? = null,
    val patient_id: Int? = null,
    val route_id: Int? = null,
    val search: String? = null,
    val start_date: String? = null,
    //for pagination
    val per_page: Int? = null,
    val page_number: Int? = null
)