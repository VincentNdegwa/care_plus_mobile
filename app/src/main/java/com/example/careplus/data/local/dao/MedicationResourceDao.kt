package com.example.careplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.careplus.data.local.entity.MedicationFormEntity
import com.example.careplus.data.local.entity.MedicationFrequencyEntity
import com.example.careplus.data.local.entity.MedicationRouteEntity
import com.example.careplus.data.local.entity.MedicationUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationResourceDao {
    @Query("SELECT * FROM medication_forms")
    fun getAllForms(): Flow<List<MedicationFormEntity>>

    @Query("SELECT * FROM medication_routes")
    fun getAllRoutes(): Flow<List<MedicationRouteEntity>>

    @Query("SELECT * FROM medication_units")
    fun getAllUnits(): Flow<List<MedicationUnitEntity>>

    @Query("SELECT * FROM medication_frequencies")
    fun getAllFrequencies(): Flow<List<MedicationFrequencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForms(forms: List<MedicationFormEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<MedicationRouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<MedicationUnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrequencies(frequencies: List<MedicationFrequencyEntity>)

    @Query("SELECT COUNT(*) FROM medication_forms")
    suspend fun getFormsCount(): Int

    @Query("SELECT COUNT(*) FROM medication_routes")
    suspend fun getRoutesCount(): Int

    @Query("SELECT COUNT(*) FROM medication_units")
    suspend fun getUnitsCount(): Int

    @Query("SELECT COUNT(*) FROM medication_frequencies")
    suspend fun getFrequenciesCount(): Int
} 