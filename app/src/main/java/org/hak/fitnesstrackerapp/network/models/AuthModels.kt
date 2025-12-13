package org.hak.fitnesstrackerapp.network.models

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceToken: String? = null
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val birthDate: String? = null
)

data class AuthResponse(
    val user: UserResponse,
    val token: TokenResponse,
    val isNewUser: Boolean = false
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val profile: ProfileResponse? = null,
    val settings: SettingsResponse? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val token: String
)

data class ProfileResponse(
    val heightCm: Double?,
    val weightKg: Double?,
    val birthDate: String?,
    val gender: String?,
    val fitnessLevel: String?
)

data class SettingsResponse(
    val notificationsEnabled: Boolean,
    val darkMode: Boolean,
    val measurementSystem: String, // "metric" or "imperial"
    val weeklyGoal: Int,
    val dailyReminder: Boolean
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int, // seconds
    val tokenType: String = "Bearer"
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)