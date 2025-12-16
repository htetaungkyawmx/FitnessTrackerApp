package org.hak.fitnesstrackerapp.network

import org.hak.fitnesstrackerapp.models.DashboardStats
import org.hak.fitnesstrackerapp.models.FitnessActivity
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("register.php")
    suspend fun register(@Body user: UserRequest): Response<ApiResponse>

    @POST("login.php")
    suspend fun login(@Body credentials: LoginRequest): Response<ApiResponse>

    @POST("add_activity.php")
    suspend fun addActivity(@Body activity: ActivityRequest): Response<ApiResponse>

    @PUT("update_activity.php")
    suspend fun updateActivity(@Body activity: ActivityRequest): Response<ApiResponse>

    @PUT("update_profile.php")
    suspend fun updateProfile(@Body profileData: Map<String, Any>): Response<BaseResponse>

    @HTTP(method = "DELETE", path = "delete_activity.php", hasBody = true)
    suspend fun deleteActivity(@Body deleteRequest: DeleteRequest): Response<ApiResponse>

    @GET("get_activities.php")
    suspend fun getActivities(@Query("user_id") userId: Int): Response<ActivitiesResponse>

    @GET("get_stats.php")
    suspend fun getStats(@Query("user_id") userId: Int): Response<StatsResponse>
}

// Request data classes
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
    val date: String,
    val exercise_name: String? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null
)

data class DeleteRequest(
    val id: Int
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
)

data class BaseResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
)

data class ActivitiesResponse(
    val success: Boolean,
    val message: String,
    val data: ActivitiesData? = null
) {
    fun getActivities(): List<FitnessActivity> {
        return data?.activities ?: emptyList()
    }

    fun getTotal(): Int {
        return data?.total ?: 0
    }
}

data class ActivitiesData(
    val activities: List<FitnessActivity> = emptyList(),
    val total: Int = 0
)

data class StatsResponse(
    val success: Boolean,
    val message: String,
    val data: StatsData? = null
) {
    fun getStats(): DashboardStats {
        return DashboardStats(
            steps = data?.steps ?: 0,
            calories = data?.calories ?: 0,
            distance = data?.distance ?: 0.0,
            duration = data?.duration ?: 0,
            totalActivities = data?.total_activities ?: 0,
            activityTypes = data?.activity_types ?: emptyList()
        )
    }
}

data class StatsData(
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Double = 0.0,
    val duration: Int = 0,
    val total_activities: Int = 0,
    val activity_types: List<String> = emptyList()
)