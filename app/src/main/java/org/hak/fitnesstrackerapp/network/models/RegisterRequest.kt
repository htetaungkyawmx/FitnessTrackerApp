package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for user registration
 * All fields are required for registration
 *
 * Example JSON:
 * {
 *   "username": "john_doe",
 *   "email": "john@example.com",
 *   "password": "securepassword123",
 *   "confirm_password": "securepassword123",
 *   "height_cm": 175.5,
 *   "weight_kg": 70.2,
 *   "birth_date": "1990-05-15"
 * }
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirm_password")
    val confirmPassword: String,

    @SerializedName("height_cm")
    val heightCm: Double? = null,

    @SerializedName("weight_kg")
    val weightKg: Double? = null,

    @SerializedName("birth_date")
    val birthDate: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("fitness_level")
    val fitnessLevel: String? = null
) {
    companion object {
        /**
         * Creates a RegisterRequest with basic required fields
         */
        fun createBasic(
            username: String,
            email: String,
            password: String,
            confirmPassword: String
        ): RegisterRequest {
            return RegisterRequest(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword
            )
        }

        /**
         * Creates a RegisterRequest with profile information
         */
        fun createWithProfile(
            username: String,
            email: String,
            password: String,
            confirmPassword: String,
            heightCm: Double? = null,
            weightKg: Double? = null,
            birthDate: String? = null,
            gender: String? = null,
            fitnessLevel: String? = null
        ): RegisterRequest {
            return RegisterRequest(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                heightCm = heightCm,
                weightKg = weightKg,
                birthDate = birthDate,
                gender = gender,
                fitnessLevel = fitnessLevel
            )
        }
    }

    /**
     * Validates the registration request
     * @return ValidationResult indicating success or error
     */
    fun validate(): ValidationResult {
        return when {
            username.isEmpty() -> ValidationResult.Error("Username is required")
            username.length < 3 -> ValidationResult.Error("Username must be at least 3 characters")
            username.length > 20 -> ValidationResult.Error("Username must be at most 20 characters")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) ->
                ValidationResult.Error("Username can only contain letters, numbers, and underscores")

            email.isEmpty() -> ValidationResult.Error("Email is required")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error("Invalid email format")

            password.isEmpty() -> ValidationResult.Error("Password is required")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            !password.any { it.isDigit() } ->
                ValidationResult.Error("Password must contain at least one digit")
            !password.any { it.isUpperCase() } ->
                ValidationResult.Error("Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } ->
                ValidationResult.Error("Password must contain at least one lowercase letter")

            confirmPassword.isEmpty() -> ValidationResult.Error("Please confirm your password")
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")

            heightCm != null && (heightCm < 50 || heightCm > 250) ->
                ValidationResult.Error("Height must be between 50 and 250 cm")

            weightKg != null && (weightKg < 20 || weightKg > 300) ->
                ValidationResult.Error("Weight must be between 20 and 300 kg")

            birthDate != null && !isValidDateFormat(birthDate) ->
                ValidationResult.Error("Birth date must be in YYYY-MM-DD format")

            gender != null && gender !in listOf("male", "female", "other") ->
                ValidationResult.Error("Gender must be male, female, or other")

            fitnessLevel != null && fitnessLevel !in listOf("beginner", "intermediate", "advanced") ->
                ValidationResult.Error("Fitness level must be beginner, intermediate, or advanced")

            else -> ValidationResult.Success
        }
    }

    private fun isValidDateFormat(date: String): Boolean {
        return try {
            val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            regex.matches(date)
        } catch (e: Exception) {
            false
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
