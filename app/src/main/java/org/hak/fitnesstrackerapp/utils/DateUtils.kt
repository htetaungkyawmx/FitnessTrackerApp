package org.hak.fitnesstrackerapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm:ss"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val DISPLAY_DATE_FORMAT = "MMM dd, yyyy"
    private const val DISPLAY_TIME_FORMAT = "hh:mm a"

    fun formatDate(date: Date, pattern: String = DATE_FORMAT): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun parseDate(dateString: String, pattern: String = DATE_FORMAT): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentDate(): String {
        return formatDate(Date())
    }

    fun getCurrentDateTime(): String {
        return formatDate(Date(), DATE_TIME_FORMAT)
    }

    fun formatForDisplay(date: Date): String {
        return formatDate(date, DISPLAY_DATE_FORMAT)
    }

    fun formatTimeForDisplay(date: Date): String {
        return formatDate(date, DISPLAY_TIME_FORMAT)
    }

    fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getStartOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    fun getDaysBetween(start: Date, end: Date): Int {
        val diff = end.time - start.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance()
        target.time = date

        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val target = Calendar.getInstance()
        target.time = date

        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun getRelativeTime(date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < 60000 -> "Just now" // Less than 1 minute
            diff < 3600000 -> "${diff / 60000} minutes ago" // Less than 1 hour
            diff < 86400000 -> "${diff / 3600000} hours ago" // Less than 1 day
            diff < 604800000 -> "${diff / 86400000} days ago" // Less than 1 week
            diff < 2592000000L -> "${diff / 604800000} weeks ago" // Less than 1 month
            diff < 31536000000L -> "${diff / 2592000000L} months ago" // Less than 1 year
            else -> "${diff / 31536000000L} years ago"
        }
    }
}