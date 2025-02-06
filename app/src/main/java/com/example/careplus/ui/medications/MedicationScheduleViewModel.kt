package com.example.careplus.ui.medications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.repository.MedicationRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class GenerateScheduleTimesRequest(
    val medication_id: Int,
    val start_datetime: String,
    val timezone: String
)

class MedicationScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)

    private val _scheduleTimeSlots = MutableLiveData<Result<List<String>>>()
    val scheduleTimeSlots: LiveData<Result<List<String>>> = _scheduleTimeSlots

    private val _scheduleCreated = MutableLiveData<Result<Unit>>()
    val scheduleCreated: LiveData<Result<Unit>> = _scheduleCreated

    fun generateScheduleTimes(medicationId: Int, startDateTime: LocalDateTime) {
        viewModelScope.launch {
            try {
                val request = GenerateScheduleTimesRequest(
                    medication_id = medicationId,
                    start_datetime = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    timezone = ZoneId.systemDefault().id
                )
                
                val result = repository.generateScheduleTimes(request)
                _scheduleTimeSlots.value = result
            } catch (e: Exception) {
                _scheduleTimeSlots.value = Result.failure(e)
            }
        }
    }

    fun createSchedule(request: CreateScheduleRequest) {
        viewModelScope.launch {
            try {
                val result = repository.createSchedule(request)
                _scheduleCreated.value = result
            } catch (e: Exception) {
                _scheduleCreated.value = Result.failure(e)
            }
        }
    }
} 