package com.example.careplus.ui.medications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.model.SimpleProfile
import com.example.careplus.data.repository.MedicationRepository
import com.example.careplus.data.repository.ProfileRepository
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.MedicationDetails
import kotlinx.coroutines.launch

class MedicationsViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val medicationRepository = MedicationRepository(sessionManager)
    private val profileRepository = ProfileRepository(sessionManager)

    private val _medications = MutableLiveData<Result<List<MedicationDetails>>>()
    val medications: LiveData<Result<List<MedicationDetails>>> = _medications

    private val _profile = MutableLiveData<Result<SimpleProfile>>()
    val profile: LiveData<Result<SimpleProfile>> = _profile

    init {
        Log.d("MedicationsViewModel", "Initializing ViewModel")
        fetchMedications()
        fetchProfile()
    }

    private fun fetchMedications() {
        viewModelScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    Log.d("MedicationsViewModel", "Fetching medications")
                    val medications = medicationRepository.getMedications(patientId)
                    _medications.value = Result.success(medications)
                    Log.d("MedicationsViewModel", "Medications fetched: ${medications.size}")
                } else {
                    throw Exception("Patient ID not found")
                }
            } catch (e: Exception) {
                Log.e("MedicationsViewModel", "Error fetching medications", e)
                _medications.value = Result.failure(e)
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            try {
                Log.d("MedicationsViewModel", "Fetching profile")
                val profile = profileRepository.getProfile()
                _profile.value = Result.success(profile)
                Log.d("MedicationsViewModel", "Profile fetched: ${profile.name}")
            } catch (e: Exception) {
                Log.e("MedicationsViewModel", "Error fetching profile", e)
                _profile.value = Result.failure(e)
            }
        }
    }
} 