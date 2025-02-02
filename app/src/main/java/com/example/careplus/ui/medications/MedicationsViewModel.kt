package com.example.careplus.ui.medications

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careplus.data.model.SimpleProfile
import com.example.careplus.data.repository.MedicationRepository
import com.example.careplus.data.repository.ProfileRepository
import com.example.careplus.data.SessionManager
import com.example.careplus.data.filter_model.FilterMedications
import com.example.careplus.data.local.AppDatabase
import com.example.careplus.data.local.entity.MedicationFormEntity
import com.example.careplus.data.local.entity.MedicationFrequencyEntity
import com.example.careplus.data.local.entity.MedicationRouteEntity
import com.example.careplus.data.local.entity.MedicationUnitEntity
import com.example.careplus.data.model.CreateMedicationRequest
import com.example.careplus.data.model.CreateMedicationResponse
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.MedicationFormResource
import com.example.careplus.data.model.MedicationFrequencyResource
import com.example.careplus.data.model.MedicationRouteResource
import com.example.careplus.data.model.MedicationUnitResource
import com.example.careplus.data.model.MedicationUpdateRequest
import com.example.careplus.data.model.MedicationUpdateResponse
import kotlinx.coroutines.launch

class MedicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = MedicationRepository(sessionManager)
    private val database = AppDatabase.getDatabase(application)
    private val resourceDao = database.medicationResourceDao()

    private val _medicationDetails = MutableLiveData<Result<MedicationDetails>>()
    val medicationDetails: LiveData<Result<MedicationDetails>> = _medicationDetails

    private val _updateResult = MutableLiveData<Result<MedicationUpdateResponse>>()
    val updateResult: LiveData<Result<MedicationUpdateResponse>> = _updateResult

    private val _newMedicationResult = MutableLiveData<Result<CreateMedicationResponse>>()
    val newMedicationResult : LiveData<Result<CreateMedicationResponse>> = _newMedicationResult

    private val _forms = MutableLiveData<Result<List<MedicationFormResource>>>()
    val forms: LiveData<Result<List<MedicationFormResource>>> = _forms

    private val _routes = MutableLiveData<Result<List<MedicationRouteResource>>>()
    val routes: LiveData<Result<List<MedicationRouteResource>>> = _routes

    private val _units = MutableLiveData<Result<List<MedicationUnitResource>>>()
    val units: LiveData<Result<List<MedicationUnitResource>>> = _units

    private val _frequencies = MutableLiveData<Result<List<MedicationFrequencyResource>>>()
    val frequencies: LiveData<Result<List<MedicationFrequencyResource>>> = _frequencies

    private val _medications = MutableLiveData<Result<List<MedicationDetails>>>()
    val medications: LiveData<Result<List<MedicationDetails>>> = _medications

    private val _profile = MutableLiveData<Result<SimpleProfile>>()
    val profile: LiveData<Result<SimpleProfile>> = _profile

    // Add new properties for pagination
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    private var currentFilter: FilterMedications? = null

    // Add loading state for pagination
    private val _paginationLoading = MutableLiveData<Boolean>()
    val paginationLoading: LiveData<Boolean> = _paginationLoading

    init {
        observeLocalDatabase()
        loadResources()
    }

    fun fetchMedications(filter: FilterMedications? = null, isFirstLoad: Boolean = true) {
        if (isFirstLoad) {
            resetPagination()
            currentFilter = filter
        }

        if (isLoading || isLastPage) return

        viewModelScope.launch {
            try {
                isLoading = true
                _paginationLoading.value = true
                
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val paginatedFilter = currentFilter?.copy(
                        patient_id = patientId.toLong(),
                        page_number = currentPage,
                        per_page = 20
                    ) ?: FilterMedications(
                        patient_id = patientId.toLong(),
                        page_number = currentPage,
                        per_page = 20
                    )

                    val response = repository.getMedications(patientId, paginatedFilter)
                    val newMedications = response.data ?: emptyList()

                    // Update pagination state
                    isLastPage = response.pagination?.let { pagination ->
                        currentPage >= pagination.total_pages
                    } ?: true

                    // Update medications list
                    if (isFirstLoad) {
                        _medications.value = Result.success(newMedications)
                    } else {
                        val currentMedications = _medications.value?.getOrNull() ?: emptyList()
                        _medications.value = Result.success(currentMedications + newMedications)
                    }

                    // Increment page number for next load
                    if (!isLastPage) {
                        currentPage++
                    }
                } else {
                    _medications.value = Result.failure(Exception("Patient ID not found"))
                }
            } catch (e: Exception) {
                _medications.value = Result.failure(e)
            } finally {
                isLoading = false
                _paginationLoading.value = false
            }
        }
    }

    fun fetchMedicationForms() {
        viewModelScope.launch {
            try {
                val forms = repository.getMedicationForms()
                _forms.value = Result.success(forms)
            } catch (e: Exception) {
                _forms.value = Result.failure(e)
            }
        }
    }

    fun fetchMedicationRoutes() {
        viewModelScope.launch {
            try {
                val routes = repository.getMedicationRoutes()
                _routes.value = Result.success(routes)
            } catch (e: Exception) {
                _routes.value = Result.failure(e)
            }
        }
    }

    fun fetchMedicationUnits() {
        viewModelScope.launch {
            try {
                val units = repository.getMedicationUnits()
                _units.value = Result.success(units)
            } catch (e: Exception) {
                _units.value = Result.failure(e)
            }
        }
    }

    fun fetchMedicationFrequencies() {
        viewModelScope.launch {
            try {
                val frequencies = repository.getMedicationFrequencies()
                _frequencies.value = Result.success(frequencies)
            } catch (e: Exception) {
                _frequencies.value = Result.failure(e)
            }
        }
    }

//    fun createMedication(request: CreateMedicationRequest): LiveData<Result<CreateMedicationResponse>> {
//        val result = MutableLiveData<Result<CreateMedicationResponse>>()
//        viewModelScope.launch {
//            try {
//                result.value = repository.createMedication(request)
//            } catch (e: Exception) {
//                result.value = Result.failure(e)
//            }
//        }
//        return result
//    }


    fun updateMedication(medicationId: Int, updateData: MedicationUpdateRequest) {
        viewModelScope.launch {
            try {
                val result = repository.updateMedication(medicationId.toLong(), updateData)
                result.onSuccess { response->
                    if (response.error){
                        _updateResult.value = Result.failure(Exception(response.message))
                    }else{
                        _updateResult.value = Result.success(response)
                    }
                }
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }
    fun  createMedication(request: CreateMedicationRequest): LiveData<Result<CreateMedicationResponse>> {
        val result = MutableLiveData<Result<CreateMedicationResponse>>()
        viewModelScope.launch {
            try {
                val response = repository.createMedication(request)
                result.value = response
            } catch (e: Exception) {
                result.value = Result.failure(e)
            }
        }
        return result
    }


    private fun observeLocalDatabase() {
        viewModelScope.launch {
            // Collect forms
            launch {
                resourceDao.getAllForms().collect { forms ->
                    _forms.value = Result.success(forms.map { it.toResource() })
                }
            }

            // Collect routes
            launch {
                resourceDao.getAllRoutes().collect { routes ->
                    _routes.value = Result.success(routes.map { it.toResource() })
                }
            }

            // Collect units
            launch {
                resourceDao.getAllUnits().collect { units ->
                    _units.value = Result.success(units.map { it.toResource() })
                }
            }

            // Collect frequencies
            launch {
                resourceDao.getAllFrequencies().collect { frequencies ->
                    _frequencies.value = Result.success(frequencies.map { it.toResource() })
                }
            }
        }
    }

    private fun loadResources() {
        viewModelScope.launch {
            try {
                // Check if we need to fetch from server
                val needsFetch = resourceDao.getFormsCount() == 0 ||
                        resourceDao.getRoutesCount() == 0 ||
                        resourceDao.getUnitsCount() == 0 ||
                        resourceDao.getFrequenciesCount() == 0

                if (needsFetch) {
                    // Fetch from server and update local database
                    val forms = repository.getMedicationForms()
                    val routes = repository.getMedicationRoutes()
                    val units = repository.getMedicationUnits()
                    val frequencies = repository.getMedicationFrequencies()

                    // Update LiveData immediately
                    _forms.value = Result.success(forms)
                    _routes.value = Result.success(routes)
                    _units.value = Result.success(units)
                    _frequencies.value = Result.success(frequencies)

                    // Update local database
                    resourceDao.insertForms(forms.map {
                        MedicationFormEntity(it.id, it.name, it.patient_id)
                    })
                    resourceDao.insertRoutes(routes.map {
                        MedicationRouteEntity(it.id, it.name, it.description)
                    })
                    resourceDao.insertUnits(units.map {
                        MedicationUnitEntity(it.id, it.name)
                    })
                    resourceDao.insertFrequencies(frequencies.map {
                        MedicationFrequencyEntity(it.id, it.frequency)
                    })
                }
            } catch (e: Exception) {
                // If fetching fails, emit error results
                _forms.value = Result.failure(e)
                _routes.value = Result.failure(e)
                _units.value = Result.failure(e)
                _frequencies.value = Result.failure(e)
            }
        }
    }

    fun setMedicationDetails(details: MedicationDetails) {
        _medicationDetails.value = Result.success(details)
    }

    // Function to reset pagination state
    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        isLoading = false
    }

    // Function to load next page
    fun loadNextPage() {
        if (!isLoading && !isLastPage) {
            fetchMedications(currentFilter, isFirstLoad = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // No need to explicitly close the database as Room handles this
    }
} 