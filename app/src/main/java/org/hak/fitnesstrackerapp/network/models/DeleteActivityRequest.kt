package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class DeleteActivityRequest(
    @SerializedName("activity_id")
    val activityId: Int
)