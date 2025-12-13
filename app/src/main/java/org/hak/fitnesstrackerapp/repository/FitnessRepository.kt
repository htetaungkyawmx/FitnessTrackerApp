package org.hak.fitnesstrackerapp.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.network.models.*

class FitnessRepository {
    private val apiService = RetrofitClient.getApiService()

    suspend fun register(username: String, email: String, password: String, confirmPassword: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(
                    RegisterRequest(
                        username = username,
                        email = email,
                        password = password,
                        confirmPassword = password
                    )
                )
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Registration failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(
                    LoginRequest(username = username, password = password)
                )
                if (response.success && response.data != null) {
                    // Set auth token for future requests
                    RetrofitClient.setAuthToken(response.data.token)
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Login failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addActivity(
        activityType: String,
        durationMinutes: Int,
        distanceKm: Double? = null,
        caloriesBurned: Int? = null,
        notes: String? = null
    ): Result<ActivityIdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addActivity(
                    ActivityRequest(
                        activityType = activityType,
                        durationMinutes = durationMinutes,
                        distanceKm = distanceKm,
                        caloriesBurned = caloriesBurned,
                        notes = notes
                    )
                )
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to add activity"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getActivities(): Result<ActivitiesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getActivities()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get activities"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteActivity(activityId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteActivity(
                    DeleteActivityRequest(activityId)
                )
                if (response.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to delete activity"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchActivities(
        type: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int = 1
    ): Result<PaginatedActivitiesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchActivities(type, dateFrom, dateTo, page)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to search activities"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDailySummary(date: String): Result<DailySummaryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDailySummary(date)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get daily summary"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserProfile(): Result<ProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserProfile()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateProfile(
        heightCm: UpdateProfileRequest = null,
        weightKg: Double? = null,
        birthDate: String? = null
    ): Result<UserProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateProfile(
                    UpdateProfileRequest(heightCm, weightKg, birthDate)
                )
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to update profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setGoal(
        goalType: String,
        targetValue: Double,
        currentValue: Double = 0.0,
        deadline: String? = null
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.setGoal(
                    GoalRequest(goalType, targetValue, currentValue, deadline)
                )
                if (response.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to set goal"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.changePassword(
                    ChangePasswordRequest(currentPassword, newPassword, confirmPassword)
                )
                if (response.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to change password"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getStats(): Result<StatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStats()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get stats"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        RetrofitClient.clearAuthToken()
    }
}