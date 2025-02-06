package com.example.careplus.ui.medications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.repository.MedicationRepository
import kotlinx.coroutines.launch

class MedicationScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)

    private val repository = MedicationRepository(sessionManager)

    fun generateScheduleTimes(medicationId: Int): LiveData<Result<List<String>>> {
        val result = MutableLiveData<Result<List<String>>>()
        viewModelScope.launch {
            try {
                val response = repository.generateScheduleTimes()
//                result.value = Result.Success(response)
            } catch (e: Exception) {
//                result.value = Result.Error(e)
            }
        }
        return result
    }

    fun createSchedule(request: CreateScheduleRequest): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        viewModelScope.launch {
            try {
                repository.createSchedule()
//                result.value = Result.Success(Unit)
            } catch (e: Exception) {
//                result.value = Result.Error(e)
            }
        }
        return result
    }
} 