package org.hak.fitnesstrackerapp.model

data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val iconRes: Int,
    val unlocked: Boolean,
    val dateUnlocked: String? = null
)