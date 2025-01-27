package com.example.careplus.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.Schedule
import com.example.careplus.data.model.UserProfile
import com.example.careplus.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = AuthRepository(sessionManager)

    private val _profile = MutableLiveData<Result<UserProfile>>()
    val profile: LiveData<Result<UserProfile>> = _profile

    private val _schedules = MutableLiveData<Result<List<Schedule>>>()
    val schedules: LiveData<Result<List<Schedule>>> = _schedules

    fun fetchProfile() {
        viewModelScope.launch {
            _profile.value = repository.getProfile()
        }
    }

    fun fetchMedicationSchedules() {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val patientId = sessionManager.getUser()?.patient?.id
            
            if (patientId != null) {
                try {
                    val result = repository.getMedicationSchedules(patientId, today)
                    _schedules.value = result
                } catch (e: Exception) {
                    _schedules.value = Result.failure(e)
                }
            }
        }
    }
} 