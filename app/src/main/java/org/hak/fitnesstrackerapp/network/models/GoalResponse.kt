package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class Goal(
    @SerializedName("goal_type")
    val goalType: String,

    @SerializedName("target_value")
    val targetValue: Double,

    @SerializedName("current_value")
    val currentValue: Double,

    @SerializedName("deadline")
    val deadline: String?
)