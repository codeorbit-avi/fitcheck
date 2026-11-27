package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.ScanRecord
import com.example.data.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.log10

@JsonClass(generateAdapter = true)
data class BodyFatJsonResult(
    val bodyFatPercentage: Float,
    val confidenceScore: Float,
    val leanBodyMassKg: Float,
    val fatMassKg: Float,
    val bmi: Float,
    val waistToHeightRatio: Float,
    val estimatedMuscleMassKg: Float,
    val bodyShape: String,
    val fitnessCategory: String,
    val analysis: String,
    val dietSuggestions: List<String>,
    val workoutSuggestions: List<String>,
    val dailyRecommendations: List<String>
)

object BodyFatAnalyzerService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(BodyFatJsonResult::class.java)

    /**
     * Estimates body fat and returns details. If bitmap is provided, includes it in the Gemini request.
     */
    suspend fun analyzeBodyFat(
        profile: UserProfile,
        frontBitmap: Bitmap? = null,
        sideBitmap: Bitmap? = null,
        backBitmap: Bitmap? = null
    ): BodyFatJsonResult = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        
        // If API Key is placeholder or blank, or if network fails, use the robust local formula.
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext calculateLocalEstimation(profile)
        }

        try {
            val parts = mutableListOf<GeminiPart>()
            
            // Build text prompt
            val prompt = """
                You are an expert anthropometric computer vision AI model. Analyze the following user physical stats to estimate their body fat percentage, muscle mass, BMI, body shape, and fitness category.
                
                PHYSICAL STATS:
                - Height: ${profile.heightCm} cm
                - Weight: ${profile.weightKg} kg
                - Age: ${profile.age} years
                - Sex: ${profile.sex}
                - Activity Level: ${profile.activityLevel}
                - Waist Circumference: ${profile.waistCm ?: "Not provided"} cm
                - Neck Circumference: ${profile.neckCm ?: "Not provided"} cm
                - Hip Circumference: ${profile.hipCm ?: "Not provided"} cm
                
                If images are attached, perform full-body posture modeling, 2D segmentation, body landmark detection, and silhouette volume estimation.
                
                Provide a professional-grade fitness estimation. Your response MUST be valid JSON and ONLY return a JSON object matching this schema. Do not enclose it in markdown blocks except raw JSON content:
                {
                  "bodyFatPercentage": Float,
                  "confidenceScore": Float,
                  "leanBodyMassKg": Float,
                  "fatMassKg": Float,
                  "bmi": Float,
                  "waistToHeightRatio": Float,
                  "estimatedMuscleMassKg": Float,
                  "bodyShape": "Hourglass|Rectangle|Triangle|Inverted Triangle|Oval|Trapezoid|Apple|Pear",
                  "fitnessCategory": "Essential Fat|Athletic|Fit|Acceptable|Obese",
                  "analysis": "A brief analysis detailing body composition, silhouette segmentation, and key landmarks.",
                  "dietSuggestions": ["Suggestion 1", "Suggestion 2", "Suggestion 3"],
                  "workoutSuggestions": ["Suggestion 1", "Suggestion 2", "Suggestion 3"],
                  "dailyRecommendations": ["Recommendation 1", "Recommendation 2"]
                }
            """.trimIndent()
            
            parts.add(GeminiPart(text = prompt))

            // Add images if available
            frontBitmap?.let {
                parts.add(GeminiPart(inlineData = InlineData(mimeType = "image/jpeg", data = it.toBase64())))
            }
            sideBitmap?.let {
                parts.add(GeminiPart(inlineData = InlineData(mimeType = "image/jpeg", data = it.toBase64())))
            }
            backBitmap?.let {
                parts.add(GeminiPart(inlineData = InlineData(mimeType = "image/jpeg", data = it.toBase64())))
            }

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GenerationConfig(
                    responseFormat = ResponseFormat(mimeType = "application/json"),
                    temperature = 0.2f
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(
                    text = "You are an advanced anthropometric computer vision and fitness coach AI. Return strictly structured JSON."
                )))
            )

            val response = GeminiClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (jsonText != null) {
                // Parse JSON
                val cleanedJson = cleanJsonBlock(jsonText)
                val parsed = adapter.fromJson(cleanedJson)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
            
            // Fallback to local on parse failures
            return@withContext calculateLocalEstimation(profile)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to local on networking exceptions
            return@withContext calculateLocalEstimation(profile)
        }
    }

    private fun cleanJsonBlock(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Fallback anthropometric estimation when offline or key is missing.
     * Uses US Navy Circumference Method or Jackson-Pollock BMI formula.
     */
    fun calculateLocalEstimation(profile: UserProfile): BodyFatJsonResult {
        val height = profile.heightCm
        val weight = profile.weightKg
        val age = profile.age
        val isMale = profile.sex.lowercase() == "male"
        
        // Calculate BMI
        val heightMeters = height / 100f
        val bmi = weight / (heightMeters * heightMeters)

        // Estimated Waist to Height
        val waist = profile.waistCm ?: (weight * 0.7f + 30f) // raw estimation if null
        val neck = profile.neckCm ?: (height * 0.22f)
        val hip = profile.hipCm ?: (weight * 0.8f + 35f)
        
        val waistToHeight = waist / height

        // 1. Navy BF Calculation
        var bodyFatPercentage = 0f
        var confidenceScore = 0.65f // Baseline local confidence

        if (profile.waistCm != null && profile.neckCm != null) {
            confidenceScore = 0.85f // High accuracy anthropometric
            try {
                if (isMale) {
                    // Men Navy formula
                    val logWaistNeck = log10(waist - neck)
                    val logHeight = log10(height)
                    val density = 1.0324 - 0.19077 * logWaistNeck + 0.15456 * logHeight
                    bodyFatPercentage = (495f / density.toFloat()) - 450f
                } else {
                    // Women Navy formula
                    val logWaistHipNeck = log10(waist + hip - neck)
                    val logHeight = log10(height)
                    val density = 1.29579 - 0.35004 * logWaistHipNeck + 0.22100 * logHeight
                    bodyFatPercentage = (495f / density.toFloat()) - 450f
                }
            } catch (e: Exception) {
                bodyFatPercentage = 0f
            }
        }

        // 2. Fallback to Jackson-Pollock BMI formula if Navy BF yields invalid results or circumferences missing
        if (bodyFatPercentage <= 2f || bodyFatPercentage >= 60f) {
            bodyFatPercentage = if (isMale) {
                1.20f * bmi + 0.23f * age - 16.2f
            } else {
                1.20f * bmi + 0.23f * age - 5.4f
            }
            if (profile.waistCm == null) {
                confidenceScore = 0.50f // Lower confidence if no circumferences
            }
        }

        // Clip body fat to realistic ranges
        bodyFatPercentage = bodyFatPercentage.coerceIn(3f, 55f)

        // Lean Mass & Fat Mass
        val fatMass = weight * (bodyFatPercentage / 100f)
        val leanMass = weight - fatMass

        // Muscle Mass Estimation (Skeletal muscle mass approx 40-50% of weight or ~55% of lean mass)
        val estimatedMuscleMass = leanMass * 0.54f

        // Fitness Category & Body Shape
        val fitnessCategory = when {
            isMale -> {
                when {
                    bodyFatPercentage < 6f -> "Essential Fat"
                    bodyFatPercentage < 14f -> "Athletic"
                    bodyFatPercentage < 18f -> "Fit"
                    bodyFatPercentage < 25f -> "Acceptable"
                    else -> "Obese"
                }
            }
            else -> {
                when {
                    bodyFatPercentage < 14f -> "Essential Fat"
                    bodyFatPercentage < 21f -> "Athletic"
                    bodyFatPercentage < 25f -> "Fit"
                    bodyFatPercentage < 32f -> "Acceptable"
                    else -> "Obese"
                }
            }
        }

        val bodyShape = if (isMale) {
            when {
                waistToHeight < 0.45f -> "Inverted Triangle"
                waistToHeight < 0.53f -> "Trapezoid"
                waistToHeight < 0.58f -> "Rectangle"
                else -> "Oval"
            }
        } else {
            when {
                waistToHeight < 0.42f -> "Hourglass"
                waistToHeight < 0.49f -> "Pear"
                waistToHeight < 0.54f -> "Rectangle"
                else -> "Apple"
            }
        }

        // Diet suggestions
        val dietSuggestions = if (fitnessCategory == "Obese" || bodyFatPercentage > 20) {
            listOf(
                "Maintain a safe daily caloric deficit of 300-500 kcal.",
                "Prioritize high-protein food sources (1.6g - 2.2g per kg of bodyweight) to preserve skeletal muscle mass.",
                "Incorporate fiber-rich complex carbohydrates (oats, sweet potato, green vegetables) for satiety."
            )
        } else if (bodyFatPercentage < 10 && isMale) {
            listOf(
                "Aim for a slight caloric surplus (+250 kcal) to support clean muscle hypertrophy.",
                "Eat high-quality essential fats (avocados, nuts, extra virgin olive oil) for optimal endocrine support.",
                "Keep dynamic protein intake at 2.0g per kg of body weight."
            )
        } else {
            listOf(
                "Maintain isocaloric balance (eucaloric) with a 40/30/30 macronutrient split (Carb/Protein/Fat).",
                "Stay fully hydrated (minimum 3.5L of water daily for metabolic efficiency).",
                "Consume post-workout nutrition with a 2:1 ratio of fast carbohydrates to protein."
            )
        }

        // Workout suggestions
        val workoutSuggestions = if (fitnessCategory == "Obese" || bodyFatPercentage > 22) {
            listOf(
                "Perform resistance training 3-4 times weekly using compound movements to stimulate muscle tissues.",
                "Incorporate Low-Intensity Steady State (LISS) cardio for 30-45 minutes on off-days.",
                "Focus on progressive overload in major lifting patterns (Squats, Presses, Pulls)."
            )
        } else {
            listOf(
                "Execute an optimized Hypertrophy program (e.g., Push/Pull/Legs or Upper/Lower split).",
                "Limit high-volume cardio to 2 sessions per week to maximize strength gains.",
                "Incorporate explosive athletic drills or active recovery to maintain speed and agility."
            )
        }

        val dailyRecommendations = listOf(
            "Track daily protein targets and log all macro intakes.",
            "Prioritize 7.5 to 8.5 hours of high-quality uninterrupted sleep for muscle recovery.",
            "Complete a fast body check-in: 10-minute deep breathing and light stretching."
        )

        return BodyFatJsonResult(
            bodyFatPercentage = String.format("%.1f", bodyFatPercentage).toFloat(),
            confidenceScore = String.format("%.2f", confidenceScore).toFloat(),
            leanBodyMassKg = String.format("%.1f", leanMass).toFloat(),
            fatMassKg = String.format("%.1f", fatMass).toFloat(),
            bmi = String.format("%.1f", bmi).toFloat(),
            waistToHeightRatio = String.format("%.2f", waistToHeight).toFloat(),
            estimatedMuscleMassKg = String.format("%.1f", estimatedMuscleMass).toFloat(),
            bodyShape = bodyShape,
            fitnessCategory = fitnessCategory,
            analysis = "Estimating using US Navy circumferences ($waist cm waist, $neck cm neck) and body metrics. This anthropometric profile provides consistent physical estimations suitable for tracking training and diet progress.",
            dietSuggestions = dietSuggestions,
            workoutSuggestions = workoutSuggestions,
            dailyRecommendations = dailyRecommendations
        )
    }
}
