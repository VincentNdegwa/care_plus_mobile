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
import com.example.careplus.data.filter_model.FilterCareProviders

class HealthProvidersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaregiverRepository()
    private val _caregivers = MutableLiveData<Result<List<CaregiverData>>>()
    val caregivers: LiveData<Result<List<CaregiverData>>> = _caregivers

    private val _myDoctors = MutableLiveData<Result<CaregiverResponse>>()
    val myDoctors: LiveData<Result<CaregiverResponse>> = _myDoctors

    private val _myCaregivers = MutableLiveData<Result<CaregiverResponse>>()
    val myCaregivers: LiveData<Result<CaregiverResponse>> = _myCaregivers

    private val sessionManager = SessionManager(application)

    // Pagination state for each list
    private data class PaginationState(
        var currentPage: Int = 1,
        var isLastPage: Boolean = false,
        var isLoading: Boolean = false,
        var currentFilter: FilterCareProviders? = null
    )

    private val allCaregiversPagination = PaginationState()
    private val myDoctorsPagination = PaginationState()
    private val myCaregiversPagination = PaginationState()

    private val _paginationLoading = MutableLiveData<Boolean>()
    val paginationLoading: LiveData<Boolean> = _paginationLoading

    fun fetchAllCaregivers(filter: FilterCareProviders? = null, isFirstLoad: Boolean = true) {
        if (isFirstLoad) {
            resetPagination(allCaregiversPagination)
            allCaregiversPagination.currentFilter = filter
        }

        if (allCaregiversPagination.isLoading || allCaregiversPagination.isLastPage) return

        viewModelScope.launch {
            try {
                allCaregiversPagination.isLoading = true
                _paginationLoading.value = true

                val paginatedFilter = allCaregiversPagination.currentFilter?.copy(
                    page_number = allCaregiversPagination.currentPage,
                    per_page = 20
                ) ?: FilterCareProviders(
                    page_number = allCaregiversPagination.currentPage,
                    per_page = 20
                )

                val response = repository.fetchAllCaregivers(paginatedFilter)
                val newCaregivers = response.data

                allCaregiversPagination.isLastPage = allCaregiversPagination.currentPage >= response.last_page

                if (isFirstLoad) {
                    _caregivers.value = Result.success(newCaregivers)
                } else {
                    val currentList = _caregivers.value?.getOrNull() ?: emptyList()
                    _caregivers.value = Result.success(currentList + newCaregivers)
                }

                if (!allCaregiversPagination.isLastPage) {
                    allCaregiversPagination.currentPage++
                }
            } catch (e: Exception) {
                _caregivers.value = Result.failure(e)
            } finally {
                allCaregiversPagination.isLoading = false
                _paginationLoading.value = false
            }
        }
    }

    fun fetchMyDoctors(filter: FilterCareProviders? = null, isFirstLoad: Boolean = true) {
        if (isFirstLoad) {
            resetPagination(myDoctorsPagination)
            myDoctorsPagination.currentFilter = filter
        }

        if (myDoctorsPagination.isLoading || myDoctorsPagination.isLastPage) return

        viewModelScope.launch {
            try {
                myDoctorsPagination.isLoading = true
                _paginationLoading.value = true

                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val paginatedFilter = myDoctorsPagination.currentFilter?.copy(
                        page_number = myDoctorsPagination.currentPage,
                        per_page = 20
                    ) ?: FilterCareProviders(
                        page_number = myDoctorsPagination.currentPage,
                        per_page = 20
                    )

                    val response = repository.fetchMyDoctors(patientId, paginatedFilter)
                    val newDoctors = response

                    myDoctorsPagination.isLastPage = myDoctorsPagination.currentPage >= response.last_page

                    if (isFirstLoad) {
                        _myDoctors.value = Result.success(newDoctors)
                    } else {
                        val currentList = _myDoctors.value?.getOrNull()
                        val combinedResponse = if (currentList != null) {
                            CaregiverResponse(
                                current_page = response.current_page,
                                data = currentList.data + newDoctors.data,
                                last_page = response.last_page,
                                per_page = response.per_page,
                                total = response.total
                            )
                        } else {
                            newDoctors
                        }
                        _myDoctors.value = Result.success(combinedResponse)
                    }

                    if (!myDoctorsPagination.isLastPage) {
                        myDoctorsPagination.currentPage++
                    }
                }
            } catch (e: Exception) {
                _myDoctors.value = Result.failure(e)
            } finally {
                myDoctorsPagination.isLoading = false
                _paginationLoading.value = false
            }
        }
    }

    fun fetchMyCaregivers(filter: FilterCareProviders? = null, isFirstLoad: Boolean = true) {
        // Similar implementation as fetchMyDoctors but for caregivers
        if (isFirstLoad) {
            resetPagination(myCaregiversPagination)
            myCaregiversPagination.currentFilter = filter
        }

        if (myCaregiversPagination.isLoading || myCaregiversPagination.isLastPage) return

        viewModelScope.launch {
            try {
                myCaregiversPagination.isLoading = true
                _paginationLoading.value = true

                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val paginatedFilter = myCaregiversPagination.currentFilter?.copy(
                        page_number = myCaregiversPagination.currentPage,
                        per_page = 20
                    ) ?: FilterCareProviders(
                        page_number = myCaregiversPagination.currentPage,
                        per_page = 20
                    )

                    val response = repository.fetchMyCaregivers(patientId,paginatedFilter)
                    val newCaregivers = response

                    myCaregiversPagination.isLastPage = myCaregiversPagination.currentPage >= response.last_page

                    if (isFirstLoad) {
                        _myCaregivers.value = Result.success(newCaregivers)
                    } else {
                        val currentList = _myCaregivers.value?.getOrNull()
                        val combinedResponse = if (currentList != null) {
                            CaregiverResponse(
                                current_page = response.current_page,
                                data = currentList.data + newCaregivers.data,
                                last_page = response.last_page,
                                per_page = response.per_page,
                                total = response.total
                            )
                        } else {
                            newCaregivers
                        }
                        _myCaregivers.value = Result.success(combinedResponse)
                    }

                    if (!myCaregiversPagination.isLastPage) {
                        myCaregiversPagination.currentPage++
                    }
                }
            } catch (e: Exception) {
                _myCaregivers.value = Result.failure(e)
            } finally {
                myCaregiversPagination.isLoading = false
                _paginationLoading.value = false
            }
        }
    }

    private fun resetPagination(state: PaginationState) {
        state.currentPage = 1
        state.isLastPage = false
        state.isLoading = false
    }

    // Functions to load next page for each list
    fun loadNextPageAllCaregivers() {
        if (!allCaregiversPagination.isLoading && !allCaregiversPagination.isLastPage) {
            fetchAllCaregivers(allCaregiversPagination.currentFilter, isFirstLoad = false)
        }
    }

    fun loadNextPageMyDoctors() {
        if (!myDoctorsPagination.isLoading && !myDoctorsPagination.isLastPage) {
            fetchMyDoctors(myDoctorsPagination.currentFilter, isFirstLoad = false)
        }
    }

    fun loadNextPageMyCaregivers() {
        if (!myCaregiversPagination.isLoading && !myCaregiversPagination.isLastPage) {
            fetchMyCaregivers(myCaregiversPagination.currentFilter, isFirstLoad = false)
        }
    }

    fun getPatientId(): Int? = sessionManager.getUser()?.patient?.id

} 