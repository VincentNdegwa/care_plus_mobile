package com.example.careplus.ui.caregivers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.repository.CaregiverRepository
import com.example.careplus.data.model.CaregiverResponse
import kotlinx.coroutines.launch

class CaregiverViewModel : ViewModel() {
    private val repository = CaregiverRepository()
    private val _caregivers = MutableLiveData<Result<CaregiverResponse>>()
    val caregivers: LiveData<Result<CaregiverResponse>> = _caregivers

    private val _myDoctors = MutableLiveData<Result<CaregiverResponse>>()
    val myDoctors: LiveData<Result<CaregiverResponse>> = _myDoctors

    private val _myCaregivers = MutableLiveData<Result<CaregiverResponse>>()
    val myCaregivers: LiveData<Result<CaregiverResponse>> = _myCaregivers

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
    suspend fun fetchMyDoctors(patientId:Int) {
        viewModelScope.launch {
            try {
                val response = repository.fetchMyDoctors(patientId)
                _myDoctors.value = Result.success(response)
            }catch (e:Exception){
                _myDoctors.value = Result.failure(e)
            }
        }
    }
    suspend fun fetchMyCaregivers(patientId:Int) {
        viewModelScope.launch {
            try {
                val response = repository.fetchMyCaregivers(patientId)
                _myCaregivers.value = Result.success(response)
            }catch (e:Exception){
                _myCaregivers.value = Result.failure(e)
            }
        }
    }
} 