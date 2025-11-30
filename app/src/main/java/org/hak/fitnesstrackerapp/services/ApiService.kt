package org.hak.fitnesstrackerapp.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.hak.fitnesstrackerapp.models.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): ApiResponse<User>

    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>

    @GET("workouts.php")
    suspend fun getWorkouts(@Query("user_id") userId: Int): ApiResponse<List<Workout>>

    @POST("workouts.php")
    suspend fun saveWorkout(@Body workout: Workout): ApiResponse<Workout>

    @GET("goals.php")
    suspend fun getGoals(@Query("user_id") userId: Int): ApiResponse<List<Goal>>

    @POST("goals.php")
    suspend fun saveGoal(@Body goal: Goal): ApiResponse<Goal>

    @PUT("goals.php")
    suspend fun updateGoal(@Body goal: Goal): ApiResponse<Goal>

    companion object {
        private const val BASE_URL = "http://192.168.1.6/fitness-tracker/api/"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)
