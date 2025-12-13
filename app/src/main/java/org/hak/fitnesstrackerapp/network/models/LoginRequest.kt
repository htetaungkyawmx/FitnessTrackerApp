package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for user login
 * Supports login with either username or email
 *
 * Example JSON:
 * {
 *   "username": "john_doe",  // OR "email": "john@example.com"
 *   "password": "securepassword123",
 *   "device_token": "fcm_token_here",
 *   "remember_me": true
 * }
 */
data class LoginRequest(
    // Use either username or email (not both required, backend will handle)
    @SerializedName("username")
    val username: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("password")
    val password: String,

    @SerializedName("device_token")
    val deviceToken: String? = null,

    @SerializedName("remember_me")
    val rememberMe: Boolean = false
) {
    companion object {
        /**
         * Creates a LoginRequest with username
         */
        fun withUsername(
            username: String,
            password: String,
            deviceToken: String? = null,
            rememberMe: Boolean = false
        ): LoginRequest {
            return LoginRequest(
                username = username,
                email = null,
                password = password,
                deviceToken = deviceToken,
                rememberMe = rememberMe
            )
        }

        /**
         * Creates a LoginRequest with email
         */
        fun withEmail(
            email: String,
            password: String,
            deviceToken: String? = null,
            rememberMe: Boolean = false
        ): LoginRequest {
            return LoginRequest(
                username = null,
                email = email,
                password = password,
                deviceToken = deviceToken,
                rememberMe = rememberMe
            )
        }
    }

    /**
     * Gets the login identifier (username or email)
     */
    val identifier: String?
        get() = username ?: email

    /**
     * Validates the login request
     * @return ValidationResult indicating success or error
     */
    fun validate(): ValidationResult {
        return when {
            identifier.isNullOrEmpty() -> ValidationResult.Error("Username or email is required")
            password.isEmpty() -> ValidationResult.Error("Password is required")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            else -> ValidationResult.Success
        }
    }

    /**
     * Checks if login is using email
     */
    val isEmailLogin: Boolean
        get() = email != null

    /**
     * Checks if login is using username
     */
    val isUsernameLogin: Boolean
        get() = username != null

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
