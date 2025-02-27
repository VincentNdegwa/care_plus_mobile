package com.example.careplus.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.settings.Settings
import com.example.careplus.data.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application):AndroidViewModel(application) {
    private val repository = SettingsRepository(SessionManager(application))

    private val _settings = MutableLiveData<Result<Settings>>()
    var settings: LiveData<Result<Settings>> = _settings

    fun getSettings(){
        try {
            viewModelScope.launch {
                val results = repository.getSettings()

                results.onSuccess { res->
                    _settings.value = Result.success(res)
                }
                results.onFailure { error->
                    _settings.value = Result.failure(error)
                }
            }
        }catch (e:Exception){
            _settings.value = Result.failure(e)
        }
    }

}