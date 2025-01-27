package com.example.careplus.ui.medications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.MedicationDetailResponse
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.repository.MedicationRepository
import kotlinx.coroutines.launch

class EditMedicationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)

    private val _medicationDetails = MutableLiveData<Result<MedicationDetailResponse>>()
    val medicationDetails: LiveData<Result<MedicationDetailResponse>> = _medicationDetails

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    fun setMedicationDetails(details: MedicationDetailResponse) {
        _medicationDetails.value = Result.success(details)
    }

    fun updateMedication(medicationId: Long, updateData: MedicationUpdateRequest) {
        viewModelScope.launch {
            try {
                repository.updateMedication(medicationId, updateData)
                _updateResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }
} 