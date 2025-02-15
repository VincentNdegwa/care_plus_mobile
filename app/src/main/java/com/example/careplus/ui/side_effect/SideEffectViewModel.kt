package com.example.careplus.ui.side_effect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.SessionManager
import com.example.careplus.data.model.PaginationData
import com.example.careplus.data.model.side_effect.*
import com.example.careplus.data.repository.SideEffectRepository
import kotlinx.coroutines.launch

class SideEffectViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = SideEffectRepository(sessionManager)

    private val _sideEffects = MutableLiveData<Result<FetchSideEffectsResponse>>()
    val sideEffects: LiveData<Result<FetchSideEffectsResponse>> = _sideEffects

    private val _sideEffect = MutableLiveData<Result<SideEffect>>()
    val sideEffect: LiveData<Result<SideEffect>> = _sideEffect

    private val _createResult = MutableLiveData<Result<SideEffect>>()
    val createResult: LiveData<Result<SideEffect>> = _createResult

    private val _updateResult = MutableLiveData<Result<SideEffect>>()
    val updateResult: LiveData<Result<SideEffect>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Pagination state
    private data class PaginationState(
        var currentPage: Int = 1,
        var isLastPage: Boolean = false,
        var isLoading: Boolean = false,
        var currentFilter: FetchSideEffectsRequest? = null
    )

    private val sideEffectsPagination = PaginationState()

    private val _paginationLoading = MutableLiveData<Boolean>()
    val paginationLoading: LiveData<Boolean> = _paginationLoading

    fun fetchSideEffects(request: FetchSideEffectsRequest? = null, isFirstLoad: Boolean = true) {
        if (isFirstLoad) {
            resetPagination()
            sideEffectsPagination.currentFilter = request
        }

        if (sideEffectsPagination.isLoading || sideEffectsPagination.isLastPage) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                sideEffectsPagination.isLoading = true
                _paginationLoading.value = true

                val patientId = getPatientId()
                if (patientId!=null){
                    val paginatedRequest = sideEffectsPagination.currentFilter?.copy(
                        page_number = sideEffectsPagination.currentPage,
                        per_page = 20
                    ) ?: FetchSideEffectsRequest(
                        patient_id = patientId,
                        page_number = sideEffectsPagination.currentPage,
                        per_page = 20
                    )

                    val result = repository.fetchSideEffects(paginatedRequest)

                    result.onSuccess { response ->
                        sideEffectsPagination.isLastPage =
                            sideEffectsPagination.currentPage >= response.pagination.last_page

                        if (isFirstLoad) {
                            _sideEffects.value = Result.success(response)
                        } else {
                            val currentList = _sideEffects.value?.getOrNull()
                            if (currentList != null) {
                                val combinedResponse = FetchSideEffectsResponse(
                                    data = currentList.data + response.data,
                                    error= response.error,
                                    pagination = PaginationData(
                                        current_page = response.pagination.current_page,
                                        last_page = response.pagination.last_page,
                                        per_page = response.pagination.per_page,
                                        total_items = response.pagination.total_items,
                                        total_pages = response.pagination.total_pages
                                    ),
                                )
                                _sideEffects.value = Result.success(combinedResponse)
                            } else {
                                _sideEffects.value = Result.success(response)
                            }
                        }

                        if (!sideEffectsPagination.isLastPage) {
                            sideEffectsPagination.currentPage++
                        }
                    }.onFailure {
                        _sideEffects.value = Result.failure(it)
                    }
                }

            } catch (e: Exception) {
                _sideEffects.value = Result.failure(e)
            } finally {
                sideEffectsPagination.isLoading = false
                _paginationLoading.value = false
                _isLoading.value = false
            }
        }
    }

    private fun resetPagination() {
        sideEffectsPagination.apply {
            currentPage = 1
            isLastPage = false
            isLoading = false
            currentFilter = null
        }
    }

    fun loadNextPage() {
        if (!sideEffectsPagination.isLoading && !sideEffectsPagination.isLastPage) {
            fetchSideEffects(sideEffectsPagination.currentFilter, isFirstLoad = false)
        }
    }

    fun getSideEffect(sideEffectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getSideEffect(sideEffectId)
                _sideEffect.value = result
            } catch (e: Exception) {
                _sideEffect.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSideEffect(request: CreateSideEffectRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.createSideEffect(request)
                _createResult.value = result
            } catch (e: Exception) {
                _createResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSideEffect(sideEffectId: Int, request: UpdateSideEffectRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.updateSideEffect(sideEffectId, request)
                _updateResult.value = result
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSideEffect(sideEffectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.deleteSideEffect(sideEffectId)
                _deleteResult.value = result
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper function to get patient ID
    fun getPatientId(): Int? = sessionManager.getUser()?.patient?.id
} 
