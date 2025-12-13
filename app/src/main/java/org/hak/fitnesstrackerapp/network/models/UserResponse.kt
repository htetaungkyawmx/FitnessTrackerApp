package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String? = null, // Make nullable with default

    @SerializedName("profile")
    val profile: ProfileResponse? = null,

    @SerializedName("settings")
    val settings: SettingsResponse? = null,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    // Safe getter for token
    fun getToken(): String {
        return token ?: throw IllegalStateException("Token not available in UserResponse")
    }

    // Helper to create a UserResponse with token
    fun withToken(token: String): UserResponse {
        return this.copy(token = token)
    }
}