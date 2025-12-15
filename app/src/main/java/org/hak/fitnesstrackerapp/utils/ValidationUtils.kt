package org.hak.fitnesstrackerapp.utils

import android.util.Patterns

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateActivityInput(
        type: String,
        duration: String,
        distance: String,
        calories: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (type.isEmpty()) {
            errors["type"] = "Activity type is required"
        }

        if (duration.isEmpty()) {
            errors["duration"] = "Duration is required"
        } else if (duration.toIntOrNull() == null) {
            errors["duration"] = "Duration must be a number"
        } else if (duration.toInt() <= 0) {
            errors["duration"] = "Duration must be greater than 0"
        }

        if (distance.isEmpty()) {
            errors["distance"] = "Distance is required"
        } else if (distance.toDoubleOrNull() == null) {
            errors["distance"] = "Distance must be a number"
        } else if (distance.toDouble() < 0) {
            errors["distance"] = "Distance cannot be negative"
        }

        if (calories.isEmpty()) {
            errors["calories"] = "Calories is required"
        } else if (calories.toIntOrNull() == null) {
            errors["calories"] = "Calories must be a number"
        } else if (calories.toInt() <= 0) {
            errors["calories"] = "Calories must be greater than 0"
        }

        return errors
    }

    fun validateWeightliftingInput(
        sets: String,
        reps: String,
        weight: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (sets.isEmpty()) {
            errors["sets"] = "Sets is required"
        } else if (sets.toIntOrNull() == null) {
            errors["sets"] = "Sets must be a number"
        } else if (sets.toInt() <= 0) {
            errors["sets"] = "Sets must be greater than 0"
        }

        if (reps.isEmpty()) {
            errors["reps"] = "Reps is required"
        } else if (reps.toIntOrNull() == null) {
            errors["reps"] = "Reps must be a number"
        } else if (reps.toInt() <= 0) {
            errors["reps"] = "Reps must be greater than 0"
        }

        if (weight.isEmpty()) {
            errors["weight"] = "Weight is required"
        } else if (weight.toDoubleOrNull() == null) {
            errors["weight"] = "Weight must be a number"
        } else if (weight.toDouble() <= 0) {
            errors["weight"] = "Weight must be greater than 0"
        }

        return errors
    }
}