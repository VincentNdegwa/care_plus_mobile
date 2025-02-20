package com.example.careplus.ui.diagnosis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.diagnosis.DiagnosisResponse
import com.example.careplus.data.model.diagnosis.DiagnosisFilterRequest
import com.example.careplus.data.repository.DiagnosisRepository
import kotlinx.coroutines.launch

class DiagnosisViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiagnosisRepository(SessionManager(application))
    
    private val _diagnoses = MutableLiveData<Result<DiagnosisResponse>>()
    val diagnoses: LiveData<Result<DiagnosisResponse>> = _diagnoses
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentPage = 1
    private var totalPages = 1
    private var isLastPage = false
    
    private var currentFilter: DiagnosisFilterRequest? = null

    fun getCurrentFilter() = currentFilter

    fun loadDiagnoses(patientId: Int, refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            isLastPage = false
        }

        if (isLastPage) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPatientDiagnoses(
                    patientId = patientId,
                    pageNumber = currentPage,
                    perPage = 10
                )

                result.onSuccess { response ->
                    currentPage = response.pagination.current_page ?: 1
                    totalPages = response.pagination.total_pages ?: 1
                    isLastPage = currentPage >= totalPages
                }

                _diagnoses.value = result
            } catch (e: Exception) {
                _diagnoses.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchDiagnoses(type: String, query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.searchDiagnoses(type, query)
                _diagnoses.value = result
            } catch (e: Exception) {
                _diagnoses.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun filterDiagnoses(request: DiagnosisFilterRequest) {
        currentFilter = request
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.filterDiagnoses(request)
                _diagnoses.value = result
            } catch (e: Exception) {
                _diagnoses.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (!isLastPage) {
            _diagnoses.value?.getOrNull()?.data?.firstOrNull()?.patient?.id?.let { patientId ->
                loadDiagnoses(patientId, false)
            }
        }
    }
} 