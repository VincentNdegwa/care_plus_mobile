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

class HealthProvidersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaregiverRepository()
    private val _caregivers = MutableLiveData<Result<List<CaregiverData>>>()
    val caregivers: LiveData<Result<List<CaregiverData>>> = _caregivers

    private val _myDoctors = MutableLiveData<Result<CaregiverResponse>>()
    val myDoctors: LiveData<Result<CaregiverResponse>> = _myDoctors

    private val _myCaregivers = MutableLiveData<Result<CaregiverResponse>>()
    val myCaregivers: LiveData<Result<CaregiverResponse>> = _myCaregivers

    private val sessionManager = SessionManager(application)

    // Filtered list of caregivers
    private val _filteredCaregivers = MutableLiveData<Result<List<CaregiverData>>>()
    val filteredCaregivers: LiveData<Result<List<CaregiverData>>> = _filteredCaregivers

    // Current filter criteria
    private var currentSpecialization: String? = null
    private var currentClinicName: String? = null
    private var currentAgencyName: String? = null
    private var currentSearchQuery: String = ""

    init {
        fetchAllCaregivers()
    }

    fun fetchAllCaregivers() {
        viewModelScope.launch {
            try {
                val response = repository.fetchAllCaregivers()
                _caregivers.value = Result.success(response.data)
                // Initialize filtered list with all caregivers
                _filteredCaregivers.value = Result.success(response.data)
            } catch (e: Exception) {
                _caregivers.value = Result.failure(e)
                _filteredCaregivers.value = Result.failure(e)
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

    fun searchCaregivers(query: String) {
        currentSearchQuery = query
        applyFiltersAndSearch()
    }

    fun applyFilters(
        specialization: String?,
        clinicName: String?,
        agencyName: String?
    ) {
        currentSpecialization = specialization
        currentClinicName = clinicName
        currentAgencyName = agencyName
        applyFiltersAndSearch()
    }

    private fun applyFiltersAndSearch() {
        _caregivers.value?.onSuccess { originalList ->
            val filteredList = originalList.filter { caregiver ->
                val matchesSearch = if (currentSearchQuery.isBlank()) {
                    true
                } else {
                    caregiver.name.contains(currentSearchQuery, ignoreCase = true) ||
                    caregiver.email.contains(currentSearchQuery, ignoreCase = true)
                }

                val matchesSpecialization = if (currentSpecialization.isNullOrBlank()) {
                    true
                } else {
                    caregiver.user_role.specialization?.contains(currentSpecialization!!, ignoreCase = true) == true
                }

                val matchesClinic = if (currentClinicName.isNullOrBlank()) {
                    true
                } else {
                    caregiver.user_role?.clinic_name?.contains(currentClinicName!!, ignoreCase = true) == true
                }

                val matchesAgency = if (currentAgencyName.isNullOrBlank()) {
                    true
                } else {
                    // Assuming there's an agency_name field, adjust according to your data model
                    caregiver.user_role?.clinic_name?.contains(currentAgencyName!!, ignoreCase = true) == true
                }

                matchesSearch && matchesSpecialization && matchesClinic && matchesAgency
            }

            _filteredCaregivers.value = Result.success(filteredList)
        }?.onFailure { exception ->
            _filteredCaregivers.value = Result.failure(exception)
        }
    }

    fun clearFilters() {
        currentSpecialization = null
        currentClinicName = null
        currentAgencyName = null
        currentSearchQuery = ""
        _caregivers.value?.onSuccess { originalList ->
            _filteredCaregivers.value = Result.success(originalList)
        }
    }
} 