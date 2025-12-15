package org.hak.fitnesstrackerapp.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FitnessActivity(
    val id: Int = 0,
    val userId: Int = 0,
    val type: String = "",
    val duration: Int = 0,
    val distance: Double = 0.0,
    val calories: Int = 0,
    val note: String = "",
    val dateString: String = "",
    val createdAt: String = "",
    val exerciseName: String? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null
) {
    val date: Date
        get() = parseDateString()

    private fun parseDateString(): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateString) ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun getShortDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateString) ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}