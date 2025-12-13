package org.hak.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.network.models.*
import org.hak.fitnesstrackerapp.repository.FitnessRepository

class ActivityViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _addActivityState = MutableLiveData<AddActivityState>()
    val addActivityState: LiveData<AddActivityState> = _addActivityState

    private val _activitiesState = MutableLiveData<ActivitiesState>()
    val activitiesState: LiveData<ActivitiesState> = _activitiesState

    private val _deleteActivityState = MutableLiveData<DeleteActivityState>()
    val deleteActivityState: LiveData<DeleteActivityState> = _deleteActivityState

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    fun addActivity(
        activityType: String,
        duration: Int,
        distance: Double? = null,
        calories: Int? = null,
        notes: String? = null
    ) {
        if (duration <= 0) {
            _addActivityState.value = AddActivityState.Error("Duration must be greater than 0")
            return
        }

        _addActivityState.value = AddActivityState.Loading

        viewModelScope.launch {
            val result = repository.addActivity(
                activityType = activityType,
                durationMinutes = duration,
                distanceKm = distance,
                caloriesBurned = calories,
                notes = notes
            )

            when {
                result.isSuccess -> {
                    _addActivityState.value = AddActivityState.Success(result.getOrNull()!!)
                }
                result.isFailure -> {
                    _addActivityState.value = AddActivityState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to add activity"
                    )
                }
            }
        }
    }

    fun loadActivities() {
        _activitiesState.value = ActivitiesState.Loading

        viewModelScope.launch {
            val result = repository.getActivities()

            when {
                result.isSuccess -> {
                    _activitiesState.value = ActivitiesState.Success(result.getOrNull()!!)
                }
                result.isFailure -> {
                    _activitiesState.value = ActivitiesState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load activities"
                    )
                }
            }
        }
    }

    fun deleteActivity(activityId: Int) {
        _deleteActivityState.value = DeleteActivityState.Loading

        viewModelScope.launch {
            val result = repository.deleteActivity(activityId)

            when {
                result.isSuccess -> {
                    _deleteActivityState.value = DeleteActivityState.Success(activityId)
                }
                result.isFailure -> {
                    _deleteActivityState.value = DeleteActivityState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to delete activity"
                    )
                }
            }
        }
    }

    fun searchActivities(
        type: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int = 1
    ) {
        _searchState.value = SearchState.Loading

        viewModelScope.launch {
            val result = repository.searchActivities(type, dateFrom, dateTo, page)

            when {
                result.isSuccess -> {
                    _searchState.value = SearchState.Success(result.getOrNull()!!)
                }
                result.isFailure -> {
                    _searchState.value = SearchState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to search activities"
                    )
                }
            }
        }
    }
}

sealed class AddActivityState {
    object Loading : AddActivityState()
    data class Success(val response: ActivityIdResponse) : AddActivityState()
    data class Error(val message: String) : AddActivityState()
}

sealed class ActivitiesState {
    object Loading : ActivitiesState()
    data class Success(val response: ActivitiesResponse) : ActivitiesState()
    data class Error(val message: String) : ActivitiesState()
}

sealed class DeleteActivityState {
    object Loading : DeleteActivityState()
    data class Success(val activityId: Int) : DeleteActivityState()
    data class Error(val message: String) : DeleteActivityState()
}

sealed class SearchState {
    object Loading : SearchState()
    data class Success(val response: PaginatedActivitiesResponse) : SearchState()
    data class Error(val message: String) : SearchState()
}