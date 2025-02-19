package com.example.careplus.ui.medications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.*
import com.example.careplus.data.repository.MedicationRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MedicationDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)

    private val _medication = MutableLiveData<Result<MedicationDetails>>()
    val medication: LiveData<Result<MedicationDetails>> = _medication

    private val _takeMedicationResult = MutableLiveData<Result<TakeMedicationResponse>>()
    val takeMedicationResult: LiveData<Result<TakeMedicationResponse>> = _takeMedicationResult

    private val _stopMedicationResult = MutableLiveData<Result<StopMedicationResponse>>()
    val stopMedicationResult: LiveData<Result<StopMedicationResponse>> = _stopMedicationResult

    private val _snoozeMedicationResult = MutableLiveData<Result<SnoozeMedicationResponse>>()
    val snoozeMedicationResult: LiveData<Result<SnoozeMedicationResponse>> = _snoozeMedicationResult

    private val _resumeMedicationResult = MutableLiveData<Result<ResumeMedicationResponse>>()
    val resumeMedicationResult: LiveData<Result<ResumeMedicationResponse>> = _resumeMedicationResult

    private val _deleteMedicationResult = MutableLiveData<Result<DeleteResponse>?>()
    val deleteMedicationResult: LiveData<Result<DeleteResponse>?> = _deleteMedicationResult

    private val _takeNowResult = MutableLiveData<Result<TakeNowResponse>>()
    val takeNowResult : LiveData<Result<TakeNowResponse>> = _takeNowResult

    fun fetchMedicationDetails(medicationId: Long) {
        viewModelScope.launch {
            try {
                val medicationDetails = repository.getMedicationById(medicationId.toInt())
                _medication.value = Result.success(medicationDetails)
            } catch (e: Exception) {
                _medication.value = Result.failure(e)
            }
        }
    }

    fun setMedicationDetails(details: MedicationDetails) {
        _medication.value = Result.success(details)
    }

    fun takeMedication(scheduleId: Int, takenAt: LocalDateTime? = null) {
        viewModelScope.launch {
            try {
                val result = repository.takeMedication(scheduleId, takenAt)
                _takeMedicationResult.value = result
            } catch (e: Exception) {
                _takeMedicationResult.value = Result.failure(e)
            }
        }
    }

    fun stopMedication(medicationId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.stopMedication(medicationId)
                _stopMedicationResult.value = result
            } catch (e: Exception) {
                _stopMedicationResult.value = Result.failure(e)
            }
        }
    }

    fun snoozeMedication(scheduleId: Int, minutes: Int) {
        viewModelScope.launch {
            try {
                val result = repository.snoozeMedication(scheduleId, minutes)
                _snoozeMedicationResult.value = result
            } catch (e: Exception) {
                _snoozeMedicationResult.value = Result.failure(e)
            }
        }
    }

    fun resumeMedication(medicationId: Int, extendDays: Boolean = false) {
        viewModelScope.launch {
            try {
                val result = repository.resumeMedication(medicationId, extendDays)
                _resumeMedicationResult.value = result
            } catch (e: Exception) {
                _resumeMedicationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteMedication(medicationId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.deleteMedication(medicationId)

                result.onSuccess { response ->
                    if (response.error){
                        _deleteMedicationResult.value = Result.failure(Exception(response.message))
                    }else{
                        _deleteMedicationResult.value = Result.success(response)
                    }

                }.onFailure { exception ->
                    _deleteMedicationResult.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _deleteMedicationResult.value = Result.failure(e)
            }
        }
    }

    fun takeNow(medicationId: Int) {
        viewModelScope.launch {
            try {
                // Get current UTC datetime formatted as string
                val currentUtcDateTime = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                val request = TakeNowRequest(
                    medication_id = medicationId,
                    date_time = currentUtcDateTime
                )
                val result = repository.takeNow(request)
                _takeNowResult.value = result
            } catch (e: Exception) {
                _takeNowResult.value = Result.failure(e)
            }
        }
    }

    // Helper function to clear results after handling them
//    fun clearTakeMedicationResult() {
//        _takeMedicationResult.value = null
//    }
//
//    fun clearStopMedicationResult() {
//        _stopMedicationResult.value = null
//    }
//
//    fun clearSnoozeMedicationResult() {
//        _snoozeMedicationResult.value = null
//    }
//
//    fun clearResumeMedicationResult() {
//        _resumeMedicationResult.value = null
//    }
} 