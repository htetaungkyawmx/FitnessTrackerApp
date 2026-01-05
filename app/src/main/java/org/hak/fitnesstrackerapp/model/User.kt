package org.hak.fitnesstrackerapp.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("password")
    val password: String = "",

    @SerializedName("age")
    val age: Int = 0,

    @SerializedName("weight")
    val weight: Double = 0.0,

    @SerializedName("height")
    val height: Double = 0.0
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
    val token: String? = null
)