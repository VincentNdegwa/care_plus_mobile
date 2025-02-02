package com.example.careplus.ui.medications

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationFrequencyResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUnitResource
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.model.MedicationUpdateResponse
import com.example.careplus.data.model.MedicationUpdated
import com.example.careplus.data.repository.MedicationRepository
import com.example.careplus.data.local.AppDatabase
import com.example.careplus.data.local.entity.MedicationFormEntity
import com.example.careplus.data.local.entity.MedicationRouteEntity
import com.example.careplus.data.local.entity.MedicationUnitEntity
import com.example.careplus.data.local.entity.MedicationFrequencyEntity
import com.example.careplus.data.local.dao.MedicationResourceDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class EditMedicationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)
    private val database = AppDatabase.getDatabase(application)
    private val resourceDao = database.medicationResourceDao()

    private val _medicationDetails = MutableLiveData<Result<MedicationDetails>>()
    val medicationDetails: LiveData<Result<MedicationDetails>> = _medicationDetails

    private val _updateResult = MutableLiveData<Result<MedicationUpdateResponse>>()
    val updateResult: LiveData<Result<MedicationUpdateResponse>> = _updateResult

    private val _forms = MutableLiveData<Result<List<MedicationFormResource>>>()
    val forms: LiveData<Result<List<MedicationFormResource>>> = _forms

    private val _routes = MutableLiveData<Result<List<MedicationRouteResource>>>()
    val routes: LiveData<Result<List<MedicationRouteResource>>> = _routes

    private val _units = MutableLiveData<Result<List<MedicationUnitResource>>>()
    val units: LiveData<Result<List<MedicationUnitResource>>> = _units

    private val _frequencies = MutableLiveData<Result<List<MedicationFrequencyResource>>>()
    val frequencies: LiveData<Result<List<MedicationFrequencyResource>>> = _frequencies

    init {
        observeLocalDatabase()
        loadResources()
    }

    private fun observeLocalDatabase() {
        viewModelScope.launch {
            // Collect forms
            launch {
                resourceDao.getAllForms().collect { forms ->
                    _forms.value = Result.success(forms.map { it.toResource() })
                }
            }

            // Collect routes
            launch {
                resourceDao.getAllRoutes().collect { routes ->
                    _routes.value = Result.success(routes.map { it.toResource() })
                }
            }

            // Collect units
            launch {
                resourceDao.getAllUnits().collect { units ->
                    _units.value = Result.success(units.map { it.toResource() })
                }
            }

            // Collect frequencies
            launch {
                resourceDao.getAllFrequencies().collect { frequencies ->
                    _frequencies.value = Result.success(frequencies.map { it.toResource() })
                }
            }
        }
    }

    private fun loadResources() {
        viewModelScope.launch {
            try {
                // Check if we need to fetch from server
                val needsFetch = resourceDao.getFormsCount() == 0 || 
                                resourceDao.getRoutesCount() == 0 ||
                                resourceDao.getUnitsCount() == 0 ||
                                resourceDao.getFrequenciesCount() == 0

                if (needsFetch) {
                    // Fetch from server and update local database
                    val forms = repository.getMedicationForms()
                    val routes = repository.getMedicationRoutes()
                    val units = repository.getMedicationUnits()
                    val frequencies = repository.getMedicationFrequencies()

                    // Update LiveData immediately
                    _forms.value = Result.success(forms)
                    _routes.value = Result.success(routes)
                    _units.value = Result.success(units)
                    _frequencies.value = Result.success(frequencies)

                    // Update local database
                    resourceDao.insertForms(forms.map { 
                        MedicationFormEntity(it.id, it.name, it.patient_id) 
                    })
                    resourceDao.insertRoutes(routes.map { 
                        MedicationRouteEntity(it.id, it.name, it.description) 
                    })
                    resourceDao.insertUnits(units.map { 
                        MedicationUnitEntity(it.id, it.name) 
                    })
                    resourceDao.insertFrequencies(frequencies.map { 
                        MedicationFrequencyEntity(it.id, it.frequency) 
                    })
                }
            } catch (e: Exception) {
                // If fetching fails, emit error results
                _forms.value = Result.failure(e)
                _routes.value = Result.failure(e)
                _units.value = Result.failure(e)
                _frequencies.value = Result.failure(e)
            }
        }
    }

    fun setMedicationDetails(details: MedicationDetails) {
        _medicationDetails.value = Result.success(details)
    }

    fun updateMedication(medicationId: Int, updateData: MedicationUpdateRequest) {
        viewModelScope.launch {
            try {
                val result = repository.updateMedication(medicationId.toLong(), updateData)
                result.onSuccess { response->
                    if (response.error){
                        _updateResult.value = Result.failure(Exception(response.message))
                    }else{
                        _updateResult.value = Result.success(response)
                    }
                }
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // No need to explicitly close the database as Room handles this
    }
} 