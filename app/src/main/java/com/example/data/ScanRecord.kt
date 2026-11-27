package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "scan_records")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val heightCm: Float,
    val weightKg: Float,
    val age: Int,
    val sex: String, // "Male" or "Female"
    val waistCm: Float?,
    val neckCm: Float?,
    val hipCm: Float?,
    val activityLevel: String,
    val bodyFatPercentage: Float,
    val confidenceScore: Float,
    val leanBodyMassKg: Float,
    val fatMassKg: Float,
    val bmi: Float,
    val waistToHeightRatio: Float,
    val estimatedMuscleMassKg: Float,
    val bodyShape: String,
    val fitnessCategory: String,
    val frontImagePath: String? = null,
    val sideImagePath: String? = null,
    val backImagePath: String? = null
) : Serializable
