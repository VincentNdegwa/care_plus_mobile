package com.example.careplus.data.model

data class CaregiverResponse(
    val current_page: Int,
    val `data`: List<Data>,
    val last_page: Int,
    val per_page: Int,
    val total: Int
)