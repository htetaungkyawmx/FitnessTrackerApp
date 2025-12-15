package org.hak.fitnesstrackerapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateString) ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDateOnly(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateString) ?: Date())
        } catch (e: Exception) {
            dateString.substring(0, 10)
        }
    }

    fun isToday(dateString: String): Boolean {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return false

            val today = Calendar.getInstance()
            val targetDate = Calendar.getInstance()
            targetDate.time = date

            today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == targetDate.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == targetDate.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            false
        }
    }

    fun getStartOfWeek(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun getStartOfMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}