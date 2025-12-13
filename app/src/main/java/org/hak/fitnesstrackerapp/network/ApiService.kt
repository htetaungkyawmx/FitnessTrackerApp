package org.hak.fitnesstrackerapp.network

import org.hak.fitnesstrackerapp.network.models.*
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponse>

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    // Activities
    @POST("add_activity.php")
    suspend fun addActivity(@Body request: ActivityRequest): ApiResponse<ActivityIdResponse>

    @GET("get_activities.php")
    suspend fun getActivities(): ApiResponse<ActivitiesResponse>

    @GET("search_activities.php")
    suspend fun searchActivities(
        @Query("type") type: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PaginatedActivitiesResponse>

    @HTTP(method = "DELETE", path = "delete_activity.php", hasBody = true)
    suspend fun deleteActivity(@Body request: DeleteActivityRequest): ApiResponse<Unit>

    @GET("get_daily_summary.php")
    suspend fun getDailySummary(@Query("date") date: String): ApiResponse<DailySummaryResponse>

    // Profile
    @GET("get_user_profile.php")
    suspend fun getUserProfile(): ApiResponse<UserProfileResponse>

    @PUT("update_profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserProfileResponse>

    @POST("change_password.php")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Unit>

    // Goals
    @POST("set_goal.php")
    suspend fun setGoal(@Body request: GoalRequest): ApiResponse<Unit>

    // Stats
    @GET("get_stats.php")
    suspend fun getStats(): ApiResponse<StatsResponse>
}
