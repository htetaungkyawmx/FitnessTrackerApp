package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("user")
    val user: UserResponse,

    @SerializedName("token")
    val token: String
)