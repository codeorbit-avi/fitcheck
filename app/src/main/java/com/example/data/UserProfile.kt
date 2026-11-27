package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single row profile
    val name: String = "Fitness Enthusiast",
    val age: Int = 28,
    val sex: String = "Male",
    val heightCm: Float = 175f,
    val weightKg: Float = 72f,
    val activityLevel: String = "Moderately Active",
    val waistCm: Float? = 82f,
    val neckCm: Float? = 38f,
    val hipCm: Float? = 95f,
    val targetBodyFat: Float = 12f,
    val isPremium: Boolean = false
)
