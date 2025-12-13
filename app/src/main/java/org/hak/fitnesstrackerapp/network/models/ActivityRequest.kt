package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName
import org.hak.fitnesstrackerapp.model.ActivityType
import org.hak.fitnesstrackerapp.utils.DateUtils

/**
 * Request model for creating or updating an activity
 *
 * Example JSON for running:
 * {
 *   "activity_type": "running",
 *   "duration_minutes": 30,
 *   "distance_km": 5.0,
 *   "calories_burned": 300,
 *   "notes": "Morning run in the park",
 *   "start_time": "2024-01-15 07:30:00",
 *   "end_time": "2024-01-15 08:00:00",
 *   "location": {
 *     "latitude": 51.5074,
 *     "longitude": -0.1278,
 *     "altitude": 35.0,
 *     "accuracy": 10.5
 *   }
 * }
 *
 * Example JSON for weightlifting:
 * {
 *   "activity_type": "weightlifting",
 *   "duration_minutes": 60,
 *   "calories_burned": 250,
 *   "notes": "Chest and triceps workout"
 * }
 */
data class ActivityRequest(
    @SerializedName("activity_type")
    val activityType: String,

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    @SerializedName("distance_km")
    val distanceKm: Double? = null,

    @SerializedName("calories_burned")
    val caloriesBurned: Int? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("start_time")
    val startTime: String? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("location")
    val location: LocationRequest? = null
) {
    companion object {
        /**
         * Creates a simple activity request without optional fields
         */
        fun createSimple(
            activityType: String,
            durationMinutes: Int
        ): ActivityRequest {
            return ActivityRequest(
                activityType = activityType,
                durationMinutes = durationMinutes
            )
        }

        /**
         * Creates an activity request with distance (for running/cycling)
         */
        fun createWithDistance(
            activityType: String,
            durationMinutes: Int,
            distanceKm: Double,
            caloriesBurned: Int? = null,
            notes: String? = null
        ): ActivityRequest {
            return ActivityRequest(
                activityType = activityType,
                durationMinutes = durationMinutes,
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                notes = notes
            )
        }

        /**
         * Creates an activity request with location tracking
         */
        fun createWithLocation(
            activityType: String,
            durationMinutes: Int,
            distanceKm: Double? = null,
            caloriesBurned: Int? = null,
            notes: String? = null,
            latitude: Double,
            longitude: Double,
            altitude: Double? = null,
            accuracy: Float? = null
        ): ActivityRequest {
            return ActivityRequest(
                activityType = activityType,
                durationMinutes = durationMinutes,
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                notes = notes,
                startTime = DateUtils.getCurrentDateTime(),
                endTime = DateUtils.getCurrentDateTime(),
                location = LocationRequest(
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude,
                    accuracy = accuracy
                )
            )
        }

        /**
         * Creates a timed activity with start and end times
         */
        fun createTimed(
            activityType: String,
            startTime: String,
            endTime: String,
            distanceKm: Double? = null,
            caloriesBurned: Int? = null,
            notes: String? = null
        ): ActivityRequest {
            val start = DateUtils.parseDate(startTime)
            val end = DateUtils.parseDate(endTime)
            val duration = if (start != null && end != null) {
                ((end.time - start.time) / (1000 * 60)).toInt()
            } else {
                0
            }

            return ActivityRequest(
                activityType = activityType,
                durationMinutes = duration,
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                notes = notes,
                startTime = startTime,
                endTime = endTime
            )
        }
    }

    /**
     * Validates the activity request
     * @return ValidationResult indicating success or error
     */
    fun validate(): ValidationResult {
        return when {
            activityType.isEmpty() -> ValidationResult.Error("Activity type is required")
            activityType !in listOf("running", "cycling", "weightlifting", "swimming", "yoga", "walking", "other") ->
                ValidationResult.Error("Invalid activity type")

            durationMinutes <= 0 -> ValidationResult.Error("Duration must be greater than 0")
            durationMinutes > 1440 -> ValidationResult.Error("Duration cannot exceed 24 hours (1440 minutes)")

            distanceKm != null && distanceKm <= 0 -> ValidationResult.Error("Distance must be greater than 0")
            distanceKm != null && distanceKm > 1000 -> ValidationResult.Error("Distance cannot exceed 1000 km")

            caloriesBurned != null && caloriesBurned <= 0 -> ValidationResult.Error("Calories must be greater than 0")
            caloriesBurned != null && caloriesBurned > 10000 -> ValidationResult.Error("Calories cannot exceed 10000")

            notes != null && notes.length > 500 -> ValidationResult.Error("Notes cannot exceed 500 characters")

            startTime != null && !isValidDateTime(startTime) ->
                ValidationResult.Error("Start time must be in YYYY-MM-DD HH:mm:ss format")

            endTime != null && !isValidDateTime(endTime) ->
                ValidationResult.Error("End time must be in YYYY-MM-DD HH:mm:ss format")

            location != null -> location.validate()

            else -> ValidationResult.Success
        }
    }

    private fun isValidDateTime(dateTime: String): Boolean {
        return try {
            DateUtils.parseDate(dateTime, DateUtils.DATE_TIME_FORMAT) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the ActivityType enum from string
     */
    val typeEnum: ActivityType
        get() = ActivityType.fromString(activityType)

    /**
     * Calculates pace if distance is available
     */
    val pace: String?
        get() = distanceKm?.let {
            val paceMinPerKm = durationMinutes / it
            val minutes = paceMinPerKm.toInt()
            val seconds = ((paceMinPerKm - minutes) * 60).toInt()
            String.format("%d:%02d min/km", minutes, seconds)
        }

    /**
     * Checks if activity is distance-based (running, cycling, walking)
     */
    val isDistanceBased: Boolean
        get() = activityType in listOf("running", "cycling", "walking")

    /**
     * Checks if activity is strength-based (weightlifting)
     */
    val isStrengthBased: Boolean
        get() = activityType == "weightlifting"

    /**
     * Checks if activity is mind-body (yoga)
     */
    val isMindBody: Boolean
        get() = activityType == "yoga"

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}

/**
 * Location data for activity tracking
 */
data class LocationRequest(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("altitude")
    val altitude: Double? = null,

    @SerializedName("accuracy")
    val accuracy: Float? = null
) {
    fun validate(): ActivityRequest.ValidationResult {
        return when {
            latitude < -90 || latitude > 90 ->
                ActivityRequest.ValidationResult.Error("Latitude must be between -90 and 90")
            longitude < -180 || longitude > 180 ->
                ActivityRequest.ValidationResult.Error("Longitude must be between -180 and 180")
            altitude != null && (altitude < -1000 || altitude > 10000) ->
                ActivityRequest.ValidationResult.Error("Altitude must be between -1000 and 10000 meters")
            accuracy != null && accuracy < 0 ->
                ActivityRequest.ValidationResult.Error("Accuracy cannot be negative")
            else -> ActivityRequest.ValidationResult.Success
        }
    }

    /**
     * Checks if location data is valid (within reasonable bounds)
     */
    val isValid: Boolean
        get() = latitude in -90.0..90.0 &&
                longitude in -180.0..180.0 &&
                (altitude == null || altitude in -1000.0..10000.0) &&
                (accuracy == null || accuracy >= 0)
}
