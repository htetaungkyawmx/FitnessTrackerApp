package org.hak.fitnesstrackerapp.model

import java.util.*

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val birthDate: Date? = null,
    val createdAt: Date,
    val updatedAt: Date? = null
) {
    companion object {
        fun calculateBMI(heightCm: Double?, weightKg: Double?): Double? {
            return if (heightCm != null && weightKg != null && heightCm > 0) {
                val heightM = heightCm / 100
                weightKg / (heightM * heightM)
            } else {
                null
            }
        }

        fun calculateAge(birthDate: Date?): Int? {
            return birthDate?.let {
                val now = Calendar.getInstance()
                val birth = Calendar.getInstance().apply { time = it }

                var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            }
        }
    }

    val bmi: Double?
        get() = calculateBMI(heightCm, weightKg)

    val age: Int?
        get() = calculateAge(birthDate)
}