package com.example.careplus.data.filter_model

data class FilterCareProviders(
    val agency_name: String? = null,
    val gender: String? = null,
    val role: String? = null,
    val search: String? = null,
    val specialization: String? = null,
    //for pagination
    val per_page: Int? = null,
    val page_number: Int? = null
)