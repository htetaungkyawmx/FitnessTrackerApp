package org.hak.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.repository.FitnessRepository
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _dashboardState = MutableLiveData<DashboardState>()
    val dashboardState: LiveData<DashboardState> = _dashboardState

    private val _statsState = MutableLiveData<StatsState>()
    val statsState: LiveData<StatsState> = _statsState

    fun loadDashboardData() {
        _dashboardState.value = DashboardState.Loading

        viewModelScope.launch {
            try {
                val activitiesResult = repository.getActivities()
                val statsResult = repository.getStats()

                if (activitiesResult.isSuccess && statsResult.isSuccess) {
                    _dashboardState.value = DashboardState.Success(
                        activitiesResult.getOrNull()!!,
                        statsResult.getOrNull()!!
                    )
                } else {
                    _dashboardState.value = DashboardState.Error(
                        activitiesResult.exceptionOrNull()?.message ?:
                        statsResult.exceptionOrNull()?.message ?: "Failed to load data"
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadStats() {
        _statsState.value = StatsState.Loading

        viewModelScope.launch {
            try {
                val result = repository.getStats()
                if (result.isSuccess) {
                    _statsState.value = StatsState.Success(result.getOrNull()!!)
                } else {
                    _statsState.value = StatsState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load stats"
                    )
                }
            } catch (e: Exception) {
                _statsState.value = StatsState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// State Sealed Classes
sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val activitiesResponse: org.hak.fitnesstrackerapp.network.models.ActivitiesResponse,
        val statsResponse: org.hak.fitnesstrackerapp.network.models.StatsResponse
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

sealed class StatsState {
    object Loading : StatsState()
    data class Success(val statsResponse: org.hak.fitnesstrackerapp.network.models.StatsResponse) : StatsState()
    data class Error(val message: String) : StatsState()
}