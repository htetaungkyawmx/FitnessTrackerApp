package org.hak.fitnesstrackerapp.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.models.*
import retrofit2.HttpException
import java.io.IOException

class NetworkManager {

    private val apiService = ApiService.create()

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.login(LoginRequest(username, password))
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.error ?: "Login failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.register(RegisterRequest(username, email, password))
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.error ?: "Registration failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWorkouts(userId: Int): Result<List<Workout>> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getWorkouts(userId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.error ?: "Failed to fetch workouts"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveWorkout(workout: Workout): Result<Workout> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.saveWorkout(workout)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.error ?: "Failed to save workout"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
