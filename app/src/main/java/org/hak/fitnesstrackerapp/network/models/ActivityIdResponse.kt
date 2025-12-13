package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class ActivityIdResponse(
    @SerializedName("activity_id")
    val activityId: Int
)