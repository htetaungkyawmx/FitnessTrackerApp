package org.hak.fitnesstrackerapp.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): ValidationResult {
        return when {
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            !password.any { it.isDigit() } -> ValidationResult.Error("Password must contain at least one digit")
            !password.any { it.isUpperCase() } -> ValidationResult.Error("Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } -> ValidationResult.Error("Password must contain at least one lowercase letter")
            else -> ValidationResult.Success
        }
    }

    fun isValidUsername(username: String): ValidationResult {
        return when {
            username.length < 3 -> ValidationResult.Error("Username must be at least 3 characters")
            username.length > 20 -> ValidationResult.Error("Username must be at most 20 characters")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> ValidationResult.Error("Username can only contain letters, numbers and underscores")
            else -> ValidationResult.Success
        }
    }

    fun isValidHeight(height: Double?): ValidationResult {
        return when {
            height == null -> ValidationResult.Success // Optional field
            height < 50 -> ValidationResult.Error("Height must be at least 50 cm")
            height > 250 -> ValidationResult.Error("Height must be at most 250 cm")
            else -> ValidationResult.Success
        }
    }

    fun isValidWeight(weight: Double?): ValidationResult {
        return when {
            weight == null -> ValidationResult.Success // Optional field
            weight < 20 -> ValidationResult.Error("Weight must be at least 20 kg")
            weight > 300 -> ValidationResult.Error("Weight must be at most 300 kg")
            else -> ValidationResult.Success
        }
    }

    fun isValidDuration(duration: Int): ValidationResult {
        return when {
            duration <= 0 -> ValidationResult.Error("Duration must be greater than 0")
            duration > 1440 -> ValidationResult.Error("Duration must be at most 24 hours (1440 minutes)")
            else -> ValidationResult.Success
        }
    }

    fun isValidDistance(distance: Double?): ValidationResult {
        return when {
            distance == null -> ValidationResult.Success // Optional field
            distance <= 0 -> ValidationResult.Error("Distance must be greater than 0")
            distance > 1000 -> ValidationResult.Error("Distance must be at most 1000 km")
            else -> ValidationResult.Success
        }
    }

    fun isValidCalories(calories: Int?): ValidationResult {
        return when {
            calories == null -> ValidationResult.Success // Optional field
            calories <= 0 -> ValidationResult.Error("Calories must be greater than 0")
            calories > 10000 -> ValidationResult.Error("Calories must be at most 10000")
            else -> ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}