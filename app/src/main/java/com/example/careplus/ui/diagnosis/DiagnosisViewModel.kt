package com.example.careplus.ui.diagnosis

import android.app.Application
import android.util.Log
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
        Log.d("DiagnoseViewModel", "In the model fetching data for $patientId")
        if (refresh) {
            currentPage = 1
            isLastPage = false
        }

        if (isLastPage) return
        listMyDiagnoses(patientId)
    }
    
    fun searchDiagnoses(query: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val result = repository.searchDiagnoses(query)
                _diagnoses.value = result
            } catch (e: Exception) {
                _diagnoses.value = Result.failure(e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun filterDiagnoses(request: DiagnosisFilterRequest) {
        currentFilter = request
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val result = repository.filterDiagnoses(request)
                _diagnoses.value = result
            } catch (e: Exception) {
                _diagnoses.value = Result.failure(e)
            } finally {
                _isLoading.postValue(false)
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
    fun listMyDiagnoses(patientId: Int){
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d("DiagnoseViewModel", "Making API call")
                val result = repository.getPatientDiagnoses(
                    patientId = patientId,
                    pageNumber = currentPage,
                    perPage = 10
                )

                Log.d("DiagnoseViewModel", "API call completed")

                result.onSuccess { response ->
                    Log.d("DiagnoseViewModel", "Success: ${response.data.size} items")
                    currentPage = response.pagination.current_page ?: 1
                    totalPages = response.pagination.total_pages ?: 1
                    isLastPage = currentPage >= totalPages
                }.onFailure {
                    Log.e("DiagnoseViewModel", "Error: ${it.message}")
                }

                _diagnoses.value = result
            } catch (e: Exception) {
                Log.e("DiagnoseViewModel", "Exception: ${e.message}")
                _diagnoses.value = Result.failure(e)
            } finally {
                _isLoading.postValue(false)
                Log.d("DiagnoseViewModel", "Setting final loading state to false")
            }
        }
    }
} 