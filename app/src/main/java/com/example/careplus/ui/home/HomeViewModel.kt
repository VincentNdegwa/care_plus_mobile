package com.example.careplus.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.DashboardResponse
import com.example.careplus.data.model.Schedule
import com.example.careplus.data.model.TakenMedicationData
import com.example.careplus.data.model.UserProfile
import com.example.careplus.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    val repository = AuthRepository(sessionManager)

    private val _profile = MutableLiveData<Result<UserProfile>>()
    val profile: LiveData<Result<UserProfile>> = _profile

    private val _schedules = MutableLiveData<Result<List<Schedule>>>()
    val schedules: LiveData<Result<List<Schedule>>> = _schedules

    private  val _stats = MutableLiveData<Result<DashboardResponse>>()
    val stats: LiveData<Result<DashboardResponse>> = _stats

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

    fun fetchStats() {
        viewModelScope.launch {
            val patientId = sessionManager.getUser()?.patient?.id
            if (patientId != null) {
                try {
                    val result = repository.getDashboardStats(patientId)
                    _stats.value = result
                } catch (e: Exception) {
                    _stats.value = Result.failure(e)
                }
            }
        }
    }

    fun updateSceduleFromTakenMed(data: TakenMedicationData) {
        val currentSchedules = _schedules.value?.getOrNull() ?: return
        
        val updatedSchedules = currentSchedules.map { schedule ->
            if (schedule.id == data.id) {
                schedule.copy(
                    status = "Taken",
                    taken_at = data.taken_at
                )
            } else {
                schedule
            }
        }

        _schedules.value = Result.success(updatedSchedules)
    }
} 