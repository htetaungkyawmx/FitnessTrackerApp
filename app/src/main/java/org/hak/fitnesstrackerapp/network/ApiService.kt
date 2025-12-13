package org.hak.fitnesstrackerapp.network

import org.hak.fitnesstrackerapp.models.DashboardStats
import org.hak.fitnesstrackerapp.models.FitnessActivity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("register.php")
    suspend fun register(@Body user: UserRequest): Response<ApiResponse>

    @POST("login.php")
    suspend fun login(@Body credentials: LoginRequest): Response<ApiResponse>

    @POST("add_activity.php")
    suspend fun addActivity(@Body activity: ActivityRequest): Response<ApiResponse>

    @PUT("update_activity.php")
    suspend fun updateActivity(@Body activity: UpdateActivityRequest): Response<ApiResponse>

    @DELETE("delete_activity.php")
    suspend fun deleteActivity(@Body deleteRequest: DeleteRequest): Response<ApiResponse>

    @GET("get_activities.php")
    suspend fun getActivities(@Query("user_id") userId: Int): Response<List<FitnessActivity>>

    @GET("get_stats.php")
    suspend fun getStats(@Query("user_id") userId: Int): Response<DashboardStats>
}

data class UserRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ActivityRequest(
    val user_id: Int,
    val type: String,
    val duration: Int,
    val distance: Double,
    val calories: Int,
    val note: String,
    val date: String
)

data class UpdateActivityRequest(
    val id: Int,
    val type: String,
    val duration: Int,
    val distance: Double,
    val calories: Int,
    val note: String,
    val date: String
)

data class DeleteRequest(
    val id: Int
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)