package com.example.careplus.ui.medications

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.CreateMedicationScheduleResponse
import com.example.careplus.data.repository.MedicationRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.careplus.data.model.MedicationScheduleResponse

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

    private val _scheduleCreated = MutableLiveData<Result<CreateMedicationScheduleResponse>>()
    val scheduleCreated: LiveData<Result<CreateMedicationScheduleResponse>> = _scheduleCreated

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun generateScheduleTimes(medicationId: Int, startDateTime: LocalDateTime) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val request = GenerateScheduleTimesRequest(
                    medication_id = medicationId,
                    start_datetime = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    timezone = ZoneId.systemDefault().id
                )
                
                val result = repository.generateScheduleTimes(request)
                _scheduleTimeSlots.value = result
            } catch (e: Exception) {
                _scheduleTimeSlots.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSchedule(request: CreateScheduleRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.createSchedule(request)
                result.onSuccess { res ->
                    if (res.error) {
                        _scheduleCreated.value = Result.failure(Exception(res.message))
                    } else {
                        _scheduleCreated.value = Result.success(res)

                    }
                }.onFailure { exception ->
                    Log.e("MedicationScheduleViewModel", "Failed to create schedule", exception)
                    _scheduleCreated.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                Log.e("MedicationScheduleViewModel", "Exception in createSchedule", e)
                _scheduleCreated.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
} 