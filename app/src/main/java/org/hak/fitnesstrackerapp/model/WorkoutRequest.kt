package org.azm.fitness_app.model

import com.google.gson.annotations.SerializedName

data class WorkoutRequest(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("type")
    val type: String,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("distance")
    val distance: Double? = null,

    @SerializedName("calories")
    val calories: Int,

    @SerializedName("notes")
    val notes: String = "",

    @SerializedName("date")
    val date: String,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncRequest(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("workouts")
    val workouts: List<Map<String, Any>>
)