package org.azm.fitness_app.network

import org.azm.fitness_app.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("register.php")
    fun register(@Body user: User): Call<LoginResponse>

    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Workout endpoints
    @POST("add_workout.php")
    fun addWorkout(@Body workout: WorkoutRequest): Call<ApiResponse>

    @GET("get_workouts.php")
    fun getWorkouts(@Query("user_id") userId: Int): Call<List<Workout>>

    @POST("sync_workouts.php")
    fun syncWorkouts(@Body request: Map<String, Any>): Call<ApiResponse>

    // Goal endpoints
    @POST("add_goal.php")
    fun addGoal(@Body goal: Goal): Call<ApiResponse>

    @GET("get_goals.php")
    fun getGoals(@Query("user_id") userId: Int): Call<List<Goal>>
}