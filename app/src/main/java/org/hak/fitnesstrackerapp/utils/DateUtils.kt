package org.hak.fitnesstrackerapp.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

// Toast extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

// Date formatting functions
fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatDateShort(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(date)
}

fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatDateForAPI(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}

fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${remainingMinutes}m"
    }
}

fun formatDistance(distance: Double): String {
    return String.format("%.2f km", distance)
}

fun formatPace(pace: Double): String {
    val minutes = pace.toInt()
    val seconds = ((pace - minutes) * 60).toInt()
    return String.format("%d:%02d min/km", minutes, seconds)
}

fun formatSpeed(speed: Double): String {
    return String.format("%.1f km/h", speed)
}

fun formatCalories(calories: Double): String {
    return String.format("%.0f cal", calories)
}

// Date calculation functions
fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

fun getStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
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

fun isToday(date: Date): Boolean {
    val today = Calendar.getInstance()
    val targetDate = Calendar.getInstance()
    targetDate.time = date
    return today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR)
}

fun isThisWeek(date: Date): Boolean {
    val startOfWeek = getStartOfWeek()
    return date >= startOfWeek
}

fun isThisMonth(date: Date): Boolean {
    val startOfMonth = getStartOfMonth()
    return date >= startOfMonth
}

fun getDaysBetween(startDate: Date, endDate: Date): Int {
    val difference = endDate.time - startDate.time
    return (difference / (24 * 60 * 60 * 1000)).toInt()
}

// Extension functions
fun Date.toReadableString(): String {
    return formatDate(this)
}

fun Date.toShortDateString(): String {
    return formatDateShort(this)
}

fun Date.toTimeString(): String {
    return formatTime(this)
}

fun Int.toDurationString(): String {
    return formatDuration(this)
}

fun Double.toDistanceString(): String {
    return formatDistance(this)
}

fun Double.toPaceString(): String {
    return formatPace(this)
}

fun Double.toSpeedString(): String {
    return formatSpeed(this)
}

fun Double.toCaloriesString(): String {
    return formatCalories(this)
}

// Validation functions
fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return email.matches(emailRegex.toRegex())
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

fun isValidUsername(username: String): Boolean {
    return username.length >= 3 && username.matches("^[a-zA-Z0-9_]+\$".toRegex())
}
