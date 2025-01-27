package com.example.careplus.ui.medications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.repository.MedicationRepository
import kotlinx.coroutines.launch

class MedicationDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)

    private val _medication = MutableLiveData<Result<MedicationDetailResponse>>()
    val medication: LiveData<Result<MedicationDetailResponse>> = _medication

    fun fetchMedicationDetails(medicationId: Long) {
        viewModelScope.launch {
            try {
                val medicationDetails = repository.getMedicationById(medicationId)
                _medication.value = Result.success(medicationDetails)
            } catch (e: Exception) {
                _medication.value = Result.failure(e)
            }
        }
    }
} 