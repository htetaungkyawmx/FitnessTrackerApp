package org.hak.fitnesstrackerapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.models.DashboardStats
import org.hak.fitnesstrackerapp.network.RetrofitClient

class StatsViewModel : ViewModel() {
    private val apiService = RetrofitClient.instance

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadStats(userId: Int) { // Fixed - removed period parameter
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getStats(userId)
                if (response.isSuccessful) {
                    val statsResponse = response.body()
                    if (statsResponse?.success == true) { // Fixed here
                        _stats.value = statsResponse.getStats() // Fixed here
                    } else {
                        _error.value = statsResponse?.message ?: "Failed to load stats" // Fixed here
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