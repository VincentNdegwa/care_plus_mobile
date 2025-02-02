package com.example.careplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationFrequencyResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUnitResource

@Entity(tableName = "medication_forms")
data class MedicationFormEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val patient_id: Int?
) {
    fun toResource() = MedicationFormResource(id, name, patient_id)
}

@Entity(tableName = "medication_routes")
data class MedicationRouteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String
) {
    fun toResource() = MedicationRouteResource(id, name, description)
}

@Entity(tableName = "medication_units")
data class MedicationUnitEntity(
    @PrimaryKey val id: Int,
    val name: String
) {
    fun toResource() = MedicationUnitResource(id, name)
}

@Entity(tableName = "medication_frequencies")
data class MedicationFrequencyEntity(
    @PrimaryKey val id: Int,
    val frequency: String
) {
    fun toResource() = MedicationFrequencyResource(id, frequency)
} 