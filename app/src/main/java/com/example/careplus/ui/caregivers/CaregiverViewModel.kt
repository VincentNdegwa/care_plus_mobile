package com.example.careplus.ui.caregivers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.repository.CaregiverRepository
import com.example.careplus.data.model.CaregiverResponse
import kotlinx.coroutines.launch

class CaregiverViewModel : ViewModel() {
    private val repository = CaregiverRepository()
    private val _caregivers = MutableLiveData<Result<CaregiverResponse>>()
    val caregivers: LiveData<Result<CaregiverResponse>> = _caregivers

    fun fetchAllCaregivers() {
        viewModelScope.launch {
            try {
                val response = repository.fetchAllCaregivers()
                _caregivers.value = Result.success(response)
            } catch (e: Exception) {
                _caregivers.value = Result.failure(e)
            }
        }
    }
} 