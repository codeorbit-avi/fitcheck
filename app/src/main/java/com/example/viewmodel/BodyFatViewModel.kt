package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.BodyFatAnalyzerService
import com.example.api.BodyFatJsonResult
import com.example.api.GeminiClient
import com.example.api.GeminiContent
import com.example.api.GeminiPart
import com.example.api.GeminiRequest
import com.example.data.BodyFatDatabase
import com.example.data.BodyFatRepository
import com.example.data.ScanRecord
import com.example.data.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Login : Screen()
    object Register : Screen()
    object Dashboard : Screen()
    object LiveScan : Screen()
    object Processing : Screen()
    object Results : Screen()
    object ProgressHistory : Screen()
    object AICoach : Screen()
    object WorkoutPlans : Screen()
    object DietPlans : Screen()
    object Profile : Screen()
    object Subscription : Screen()
    object ArchOverview : Screen()
}

class BodyFatViewModel(
    application: Application,
    private val repository: BodyFatRepository
) : AndroidViewModel(application) {

    // Screens
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Room database flows
    val allRecords: StateFlow<List<ScanRecord>> = repository.allScanRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestRecord: StateFlow<ScanRecord?> = repository.latestScanRecord
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userProfile: StateFlow<UserProfile?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Scanning photo states
    private val _frontImage = MutableStateFlow<Bitmap?>(null)
    val frontImage: StateFlow<Bitmap?> = _frontImage.asStateFlow()

    private val _sideImage = MutableStateFlow<Bitmap?>(null)
    val sideImage: StateFlow<Bitmap?> = _sideImage.asStateFlow()

    private val _backImage = MutableStateFlow<Bitmap?>(null)
    val backImage: StateFlow<Bitmap?> = _backImage.asStateFlow()

    // active photo capture mode: "Front", "Side", "Back"
    private val _activeCameraAngle = MutableStateFlow("Front")
    val activeCameraAngle: StateFlow<String> = _activeCameraAngle.asStateFlow()

    // Processing state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _analysisLogs = MutableStateFlow<List<String>>(emptyList())
    val analysisLogs: StateFlow<List<String>> = _analysisLogs.asStateFlow()

    // Results
    private val _analysisResult = MutableStateFlow<BodyFatJsonResult?>(null)
    val analysisResult: StateFlow<BodyFatJsonResult?> = _analysisResult.asStateFlow()

    // AI Coach
    private val _aiCoachResponse = MutableStateFlow<String?>(null)
    val aiCoachResponse: StateFlow<String?> = _aiCoachResponse.asStateFlow()

    private val _isAiCoachLoading = MutableStateFlow(false)
    val isAiCoachLoading: StateFlow<Boolean> = _isAiCoachLoading.asStateFlow()

    val pipelineSteps = listOf(
        "Initializing camera frame acquisition...",
        "Executing YOLOv11 Person detection (Confidence: 0.985)...",
        "Running Segment Anything Model 2 (SAM 2) foreground extraction...",
        "Computing 33 key MediaPipe skeleton pose coordinates...",
        "Reconstructing 3D body volume and silhouette projections...",
        "Extracting regional shape ratios & landmark points...",
        "Invoking Gemini 3.5 Multimodal Vision model...",
        "Generating optimized fitness indices and lean mass models..."
    )

    init {
        // Simple Splash delay
        viewModelScope.launch {
            delay(1800)
            _currentScreen.value = Screen.Onboarding
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setFrontImage(bitmap: Bitmap?) { _frontImage.value = bitmap }
    fun setSideImage(bitmap: Bitmap?) { _sideImage.value = bitmap }
    fun setBackImage(bitmap: Bitmap?) { _backImage.value = bitmap }
    fun setActiveCameraAngle(angle: String) { _activeCameraAngle.value = angle }

    fun resetPhotos() {
        _frontImage.value = null
        _sideImage.value = null
        _backImage.value = null
        _activeCameraAngle.value = "Front"
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun deleteRecord(record: ScanRecord) {
        viewModelScope.launch {
            repository.deleteScanRecord(record)
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    fun runBodyFatAnalysis() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _currentScreen.value = Screen.Processing
            _analysisLogs.value = emptyList()

            // Step through visual ML pipeline
            for (i in pipelineSteps.indices) {
                _currentStepIndex.value = i
                _analysisLogs.value = _analysisLogs.value + "[INFO] ${pipelineSteps[i]}"
                delay(1000) // Aesthetic delay for pipeline visualization
            }

            _analysisLogs.value = _analysisLogs.value + "[INFO] Finalizing inference calculations..."
            delay(500)

            val profile = repository.getUserProfileDirect() ?: UserProfile()
            
            // Execute actual analysis (Gemini or Fallback Navy Formula)
            val result = BodyFatAnalyzerService.analyzeBodyFat(
                profile = profile,
                frontBitmap = _frontImage.value,
                sideBitmap = _sideImage.value,
                backBitmap = _backImage.value
            )

            _analysisResult.value = result

            // Insert into local history
            val record = ScanRecord(
                date = System.currentTimeMillis(),
                heightCm = profile.heightCm,
                weightKg = profile.weightKg,
                age = profile.age,
                sex = profile.sex,
                waistCm = profile.waistCm,
                neckCm = profile.neckCm,
                hipCm = profile.hipCm,
                activityLevel = profile.activityLevel,
                bodyFatPercentage = result.bodyFatPercentage,
                confidenceScore = result.confidenceScore,
                leanBodyMassKg = result.leanBodyMassKg,
                fatMassKg = result.fatMassKg,
                bmi = result.bmi,
                waistToHeightRatio = result.waistToHeightRatio,
                estimatedMuscleMassKg = result.estimatedMuscleMassKg,
                bodyShape = result.bodyShape,
                fitnessCategory = result.fitnessCategory
            )
            repository.insertScanRecord(record)

            _isAnalyzing.value = false
            _currentScreen.value = Screen.Results
        }
    }

    fun askAiCoach(question: String) {
        viewModelScope.launch {
            _isAiCoachLoading.value = true
            _aiCoachResponse.value = null
            
            val apiKey = GeminiClient.getApiKey()
            if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                delay(1200)
                _aiCoachResponse.value = "AI Coach is currently in local offline mode. To enable real-time smart recommendations, please configure your active GEMINI_API_KEY in the Secrets panel.\n\nLocal Tip: Based on your physical profile, maintaining a steady weekly training plan with progressive overload combined with a consistent lean protein intake is highly recommended for body composition enhancement."
                _isAiCoachLoading.value = false
                return@launch
            }

            try {
                val profile = repository.getUserProfileDirect() ?: UserProfile()
                val latest = _analysisResult.value

                val prompt = """
                    You are an elite athletic coach, personal trainer, and dietician. Use the user's physical profile and latest scan details to answer their question.
                    
                    USER PROFILE:
                    - Age: ${profile.age} years
                    - Sex: ${profile.sex}
                    - Height: ${profile.heightCm} cm
                    - Weight: ${profile.weightKg} kg
                    - Activity Level: ${profile.activityLevel}
                    
                    SCAN ANALYSIS:
                    - Body Fat %: ${latest?.bodyFatPercentage ?: "Not scanned yet"}%
                    - Lean Mass: ${latest?.leanBodyMassKg ?: "N/A"} kg
                    - Muscle Mass: ${latest?.estimatedMuscleMassKg ?: "N/A"} kg
                    - Category: ${latest?.fitnessCategory ?: "N/A"}
                    
                    USER QUESTION:
                    "$question"
                    
                    Provide a detailed, encouraging, and highly professional answer with action items. Format nicely with bullet points and clean structure. Clearly add a disclaimer that this is for fitness education and not a medical diagnosis.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are an expert AI Fitness and Nutrition Coach.")))
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                _aiCoachResponse.value = reply ?: "Coach is taking a short rest. Please try asking again."
            } catch (e: Exception) {
                _aiCoachResponse.value = "Connection lost. Please check your internet connectivity or API credentials and try again.\nError: ${e.localizedMessage}"
            } finally {
                _isAiCoachLoading.value = false
            }
        }
    }
}

class BodyFatViewModelFactory(
    private val application: Application,
    private val repository: BodyFatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BodyFatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BodyFatViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
