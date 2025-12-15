package org.hak.fitnesstrackerapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.models.DashboardStats
import org.hak.fitnesstrackerapp.network.ApiService
import org.hak.fitnesstrackerapp.network.RetrofitClient

class StatsViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadStats(userId: Int, period: String = "today") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getStats(userId, period)
                if (response.success) {
                    _stats.value = response.getStats()
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