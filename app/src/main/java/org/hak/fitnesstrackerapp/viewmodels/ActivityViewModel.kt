package org.hak.fitnesstrackerapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.models.FitnessActivity
import org.hak.fitnesstrackerapp.network.ApiService
import org.hak.fitnesstrackerapp.network.RetrofitClient

class ActivityViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance

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
                if (response.success) {
                    _activities.value = response.getActivities()
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterActivities(userId: Int, type: String? = null, dateFrom: String? = null, dateTo: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getActivities(userId, type, dateFrom, dateTo)
                if (response.success) {
                    _activities.value = response.getActivities()
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}