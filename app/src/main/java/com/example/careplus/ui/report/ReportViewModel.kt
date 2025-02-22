package com.example.careplus.ui.report

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.report.*
import com.example.careplus.data.repository.ReportRepository
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReportRepository(SessionManager(application))

    private val _medicationVsSideEffectCounts = MutableLiveData<Result<MedicationVsSideEffectCountsResponse>>()
    val medicationVsSideEffectCounts: LiveData<Result<MedicationVsSideEffectCountsResponse>> get() = _medicationVsSideEffectCounts

    private val _topSideEffects = MutableLiveData<Result<TopSideEffectsResponse>>()
    val topSideEffects: LiveData<Result<TopSideEffectsResponse>> get() = _topSideEffects

    private val _mostMissedMedications = MutableLiveData<Result<MostMissedMedicationsResponse>>()
    val mostMissedMedications: LiveData<Result<MostMissedMedicationsResponse>> get() = _mostMissedMedications

    private val _medicalAdherenceReport = MutableLiveData<Result<MedicalAdherenceReportResponse>>()
    val medicalAdherenceReport: LiveData<Result<MedicalAdherenceReportResponse>> get() = _medicalAdherenceReport

    private val _medicationAdherenceByMedication = MutableLiveData<Result<MedicationAdherenceByMedicationResponse>>()
    val medicationAdherenceByMedication: LiveData<Result<MedicationAdherenceByMedicationResponse>> get() = _medicationAdherenceByMedication

    fun fetchMedicationVsSideEffectCounts(request: MedicationVsSideEffectCountsRequest) {
        viewModelScope.launch {
            try {
                val result = repository.getMedicationVsSideEffectCounts(request)

                result.onSuccess { response ->
                    if (response.error) {
                        _medicationVsSideEffectCounts.value = Result.failure(Exception(response.message))
                    } else {
                        _medicationVsSideEffectCounts.value = Result.success(response)
                    }
                }.onFailure { exception ->
                    _medicationVsSideEffectCounts.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _medicationVsSideEffectCounts.value = Result.failure(e)
            }
        }
    }

    fun fetchTopSideEffects(request: TopSideEffectsRequest) {
        viewModelScope.launch {
            try {
                val result = repository.getTopSideEffects(request)

                result.onSuccess { response ->
                    if (response.error) {
                        _topSideEffects.value = Result.failure(Exception(response.message))
                    } else {
                        _topSideEffects.value = Result.success(response)
                    }
                }.onFailure { exception ->
                    _topSideEffects.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _topSideEffects.value = Result.failure(e)
            }
        }
    }

    fun fetchMostMissedMedications(request: MostMissedMedicationsRequest) {
        viewModelScope.launch {
            try {
                val result = repository.getMostMissedMedications(request)

                result.onSuccess { response ->
                    if (response.error) {
                        _mostMissedMedications.value = Result.failure(Exception(response.message))
                    } else {
                        _mostMissedMedications.value = Result.success(response)
                    }
                }.onFailure { exception ->
                    _mostMissedMedications.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _mostMissedMedications.value = Result.failure(e)
            }
        }
    }

    fun fetchMedicalAdherenceReport(request: MedicalAdherenceReportRequest) {
        viewModelScope.launch {
            try {
                val result = repository.getMedicalAdherenceReport(request)

                result.onSuccess { response ->
                    if (response.error) {
                        _medicalAdherenceReport.value = Result.failure(Exception(response.message))
                    } else {
                        _medicalAdherenceReport.value = Result.success(response)
                    }
                }.onFailure { exception ->
                    _medicalAdherenceReport.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _medicalAdherenceReport.value = Result.failure(e)
            }
        }
    }

    fun fetchMedicationAdherenceByMedication(request: MedicationAdherenceByMedicationRequest) {
        viewModelScope.launch {
            try {
                val result = repository.getMedicationAdherenceByMedication(request)

                result.onSuccess { response ->
                    if (response.error) {
                        _medicationAdherenceByMedication.value = Result.failure(Exception(response.message))
                    } else {
                        _medicationAdherenceByMedication.value = Result.success(response)
                    }
                }.onFailure { exception ->
                    _medicationAdherenceByMedication.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _medicationAdherenceByMedication.value = Result.failure(e)
            }
        }
    }
} 