package org.hak.fitnesstrackerapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.models.FitnessActivity
import org.hak.fitnesstrackerapp.network.RetrofitClient

class ActivityViewModel : ViewModel() {
    private val apiService = RetrofitClient.instance

    private val _activities = MutableLiveData<List<FitnessActivity>>()
    val activities: LiveData<List<FitnessActivity>> = _activities

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadActivities(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getActivities(userId)
                if (response.isSuccessful) {
                    val activitiesResponse = response.body()
                    if (activitiesResponse?.success == true) { // Fixed here
                        _activities.value = activitiesResponse.getActivities() // Fixed here
                    } else {
                        _error.value = activitiesResponse?.message ?: "Failed to load activities" // Fixed here
                    }
                } else {
                    _error.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Note: Remove the filterActivities function since our API doesn't support filters
    // or modify it to filter locally
    fun filterActivitiesLocally(userId: Int, type: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getActivities(userId)
                if (response.isSuccessful) {
                    val activitiesResponse = response.body()
                    if (activitiesResponse?.success == true) {
                        val allActivities = activitiesResponse.getActivities()
                        // Filter locally
                        val filtered = if (type != null) {
                            allActivities.filter { it.type.equals(type, ignoreCase = true) }
                        } else {
                            allActivities
                        }
                        _activities.value = filtered
                    } else {
                        _error.value = activitiesResponse?.message ?: "Failed to load activities"
                    }
                } else {
                    _error.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}