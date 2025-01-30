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
import kotlinx.coroutines.launch

class EditMedicationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)

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
        loadMedicationResources()
    }

    private fun loadMedicationResources() {
        viewModelScope.launch {
            try {
                _forms.value = Result.success(repository.getMedicationForms())
                _routes.value = Result.success(repository.getMedicationRoutes())
                _units.value = Result.success(repository.getMedicationUnits())
                _frequencies.value = Result.success(repository.getMedicationFrequencies())
            } catch (e: Exception) {
                // If any resource fails to load, we'll show the error in the UI
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
} 