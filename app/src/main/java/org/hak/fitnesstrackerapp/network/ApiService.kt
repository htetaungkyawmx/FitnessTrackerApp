package org.hak.fitnesstrackerapp.network

import org.hak.fitnesstrackerapp.models.DashboardStats
import org.hak.fitnesstrackerapp.models.FitnessActivity
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("register.php")
    suspend fun register(@Body user: Map<String, Any>): ApiResponse

    @POST("login.php")
    suspend fun login(@Body credentials: Map<String, Any>): ApiResponse

    @POST("add_activity.php")
    suspend fun addActivity(@Body activity: Map<String, Any>): ApiResponse

    @PUT("update_activity.php")
    suspend fun updateActivity(@Body activity: Map<String, Any>): ApiResponse

    @HTTP(method = "DELETE", path = "delete_activity.php", hasBody = true)
    suspend fun deleteActivity(@Body deleteRequest: Map<String, Any>): ApiResponse

    @GET("get_activities.php")
    suspend fun getActivities(
        @Query("user_id") userId: Int,
        @Query("type") type: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("limit") limit: Int = 50
    ): ActivitiesResponse

    @GET("get_stats.php")
    suspend fun getStats(
        @Query("user_id") userId: Int,
        @Query("period") period: String = "today"
    ): StatsResponse
}

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
)

data class ActivitiesResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
) {
    fun getActivities(): List<FitnessActivity> {
        val activities = mutableListOf<FitnessActivity>()
        val activitiesList = data?.get("activities") as? List<Map<String, Any>>

        activitiesList?.forEach { activityMap ->
            val activity = FitnessActivity(
                id = (activityMap["id"] as? Number)?.toInt() ?: 0,
                userId = (activityMap["user_id"] as? Number)?.toInt() ?: 0,
                type = activityMap["type"] as? String ?: "",
                duration = (activityMap["duration"] as? Number)?.toInt() ?: 0,
                distance = (activityMap["distance"] as? Number)?.toDouble() ?: 0.0,
                calories = (activityMap["calories"] as? Number)?.toInt() ?: 0,
                note = activityMap["note"] as? String ?: "",
                dateString = activityMap["date"] as? String ?: "",
                createdAt = activityMap["created_at"] as? String ?: "",
                exerciseName = activityMap["exercise_name"] as? String,
                sets = (activityMap["sets"] as? Number)?.toInt(),
                reps = (activityMap["reps"] as? Number)?.toInt(),
                weight = (activityMap["weight"] as? Number)?.toDouble()
            )
            activities.add(activity)
        }
        return activities
    }

    fun getTotal(): Int {
        return (data?.get("total") as? Number)?.toInt() ?: 0
    }
}

data class StatsResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
) {
    fun getStats(): DashboardStats {
        return DashboardStats(
            steps = (data?.get("steps") as? Number)?.toInt() ?: 0,
            calories = (data?.get("calories") as? Number)?.toInt() ?: 0,
            distance = (data?.get("distance") as? Number)?.toDouble() ?: 0.0,
            duration = (data?.get("duration") as? Number)?.toInt() ?: 0,
            totalActivities = (data?.get("total_activities") as? Number)?.toInt() ?: 0,
            activityTypes = (data?.get("activity_types") as? List<String>) ?: emptyList()
        )
    }
}