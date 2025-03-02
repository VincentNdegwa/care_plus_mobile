package com.example.careplus.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.settings.Settings
import com.example.careplus.data.model.settings.TimezoneResponse
import com.example.careplus.data.model.settings.UpdateSettingsResponse
import com.example.careplus.data.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application):AndroidViewModel(application) {
    private val repository = SettingsRepository(SessionManager(application))

    private val _settings = MutableLiveData<Result<Settings>>()
    var settings: LiveData<Result<Settings>> = _settings

    private val _updateSetting = MutableLiveData<Result<UpdateSettingsResponse>>()
    var updateSetting: LiveData<Result<UpdateSettingsResponse>> = _updateSetting

    private val _timezones = MutableLiveData<Result<List<TimezoneResponse>>>()
    var timezones: LiveData<Result<List<TimezoneResponse>>> = _timezones

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getSettings(){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val results = repository.getSettings()

                results.onSuccess { res->
                    _settings.value = Result.success(res)
                }
                results.onFailure { error->
                    _settings.value = Result.failure(error)
                }
            }catch (e:Exception){
                _settings.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun  updateSettings(settings: Settings){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val results = repository.updateSettings(settings)

                results.onSuccess { res->
                    _updateSetting.value = Result.success(res)
                    if(!res.error){
                        _settings.value = Result.success(res.data)
                    }
                }
                results.onFailure { error->
                    _updateSetting.value = Result.failure(error)
                }
            }catch (e:Exception){
                _updateSetting.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchTimezones(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchTimezones(query)
                _timezones.value = results
            } catch (e: Exception) {
                _timezones.value = Result.failure(e)
            }
        }
    }
}