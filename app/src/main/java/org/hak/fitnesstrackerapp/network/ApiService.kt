package org.hak.fitnesstrackerapp.network

import org.hak.fitnesstrackerapp.model.ApiResponse
import org.hak.fitnesstrackerapp.model.Goal
import org.hak.fitnesstrackerapp.model.LoginRequest
import org.hak.fitnesstrackerapp.model.LoginResponse
import org.hak.fitnesstrackerapp.model.User
import org.hak.fitnesstrackerapp.model.Workout
import org.hak.fitnesstrackerapp.model.WorkoutRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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