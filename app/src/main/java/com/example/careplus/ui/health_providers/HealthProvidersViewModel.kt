package com.example.careplus.ui.health_providers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.CaregiverData
import com.example.careplus.data.repository.CaregiverRepository
import com.example.careplus.data.model.CaregiverResponse
import com.example.careplus.data.repository.AuthRepository
import kotlinx.coroutines.launch

class HealthProvidersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaregiverRepository()
    private val _caregivers = MutableLiveData<Result<CaregiverResponse>>()
    val caregivers: LiveData<Result<CaregiverResponse>> = _caregivers

    private val _myDoctors = MutableLiveData<Result<CaregiverResponse>>()
    val myDoctors: LiveData<Result<CaregiverResponse>> = _myDoctors

    private val _myCaregivers = MutableLiveData<Result<CaregiverResponse>>()
    val myCaregivers: LiveData<Result<CaregiverResponse>> = _myCaregivers

    private val sessionManager = SessionManager(application)

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

    fun fetchMyDoctors() {
        viewModelScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val response = repository.fetchMyDoctors(patientId)
                    _myDoctors.value = Result.success(response)
                } else {
                    _myDoctors.value = Result.failure(Exception("Please logout then login"))
                }
            } catch (e: Exception) {
                _myDoctors.value = Result.failure(e)
            }
        }
    }

    fun fetchMyCaregivers() {
        viewModelScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val response = repository.fetchMyCaregivers(patientId)
                    _myCaregivers.value = Result.success(response)
                } else {
                    _myCaregivers.value = Result.failure(Exception("Please logout then login"))
                }
            } catch (e: Exception) {
                _myCaregivers.value = Result.failure(e)
            }
        }
    }
} 