package org.hak.fitnesstrackerapp.network

import org.hak.fitnesstrackerapp.network.models.*
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserResponse>

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): ApiResponse<UserResponse>

    @POST("change_password.php")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Unit>

    // Profile endpoints
    @PUT("update_profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserProfileResponse>

    @GET("get_user_profile.php")
    suspend fun getUserProfile(): ApiResponse<ProfileResponse>

    // Activity endpoints
    @POST("add_activity.php")
    suspend fun addActivity(@Body request: ActivityRequest): ApiResponse<ActivityIdResponse>

    @GET("get_activities.php")
    suspend fun getActivities(): ApiResponse<ActivitiesResponse>

}