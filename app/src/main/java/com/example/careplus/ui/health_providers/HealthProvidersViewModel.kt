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
import java.util.Locale
import com.example.careplus.data.filter_model.FilterCareProviders

class HealthProvidersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaregiverRepository()
    private val _caregivers = MutableLiveData<Result<List<CaregiverData>>>()
    val caregivers: LiveData<Result<List<CaregiverData>>> = _caregivers

    private val _myDoctors = MutableLiveData<Result<CaregiverResponse>>()
    val myDoctors: LiveData<Result<CaregiverResponse>> = _myDoctors

    private val _myCaregivers = MutableLiveData<Result<CaregiverResponse>>()
    val myCaregivers: LiveData<Result<CaregiverResponse>> = _myCaregivers

    private val sessionManager = SessionManager(application)


    fun fetchAllCaregivers(filter: FilterCareProviders? = null) {
        viewModelScope.launch {
            try {
                val response = repository.fetchAllCaregivers(filter)
                _caregivers.value = Result.success(response.data)
            } catch (e: Exception) {
                _caregivers.value = Result.failure(e)
            }
        }
    }

    fun fetchMyDoctors(filter: FilterCareProviders? = null) {
        viewModelScope.launch {
            try {
                var patientId = getPatientId()
                if (patientId != null){
                    val response = repository.fetchMyDoctors(patientId,filter)
                    _myDoctors.value = Result.success(response)
                }else{
                    _myDoctors.value = Result.failure(Exception("Patient Id not found"))
                }
            } catch (e: Exception) {
                _myDoctors.value = Result.failure(e)
            }
        }
    }

    fun fetchMyCaregivers(filter: FilterCareProviders? = null) {
        viewModelScope.launch {
            try {
                var patientId = getPatientId()
                if (patientId != null){
                    val response = repository.fetchMyCaregivers(patientId,filter)
                    _myCaregivers.value = Result.success(response)
                }else{
                    _myCaregivers.value = Result.failure(Exception("Patient Id not found"))
                }
            } catch (e: Exception) {
                _myCaregivers.value = Result.failure(e)
            }
        }
    }

    fun getPatientId(): Int? = sessionManager.getUser()?.patient?.id

} 