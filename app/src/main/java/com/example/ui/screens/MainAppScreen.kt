package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.ScanRecord
import com.example.data.UserProfile
import com.example.viewmodel.BodyFatViewModel
import com.example.viewmodel.Screen
import com.example.ui.theme.*
import com.google.accompanist.permissions.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BodyFatAppContent(viewModel: BodyFatViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen()
            is Screen.Onboarding -> OnboardingScreen(viewModel)
            is Screen.Login -> MockLoginScreen { viewModel.navigateTo(Screen.Dashboard) }
            is Screen.Register -> MockRegisterScreen { viewModel.navigateTo(Screen.Dashboard) }
            is Screen.Dashboard -> MainNavigationLayout(viewModel, Screen.Dashboard) { DashboardScreen(viewModel) }
            is Screen.LiveScan -> LiveScanScreen(viewModel)
            is Screen.Processing -> ProcessingScreen(viewModel)
            is Screen.Results -> ResultsScreen(viewModel)
            is Screen.ProgressHistory -> MainNavigationLayout(viewModel, Screen.ProgressHistory) { ProgressHistoryScreen(viewModel) }
            is Screen.AICoach -> MainNavigationLayout(viewModel, Screen.AICoach) { AICoachScreen(viewModel) }
            is Screen.WorkoutPlans -> MainNavigationLayout(viewModel, Screen.AICoach) { WorkoutPlansScreen(viewModel) }
            is Screen.DietPlans -> MainNavigationLayout(viewModel, Screen.AICoach) { DietPlansScreen(viewModel) }
            is Screen.Profile -> MainNavigationLayout(viewModel, Screen.Profile) { ProfileScreen(viewModel) }
            is Screen.Subscription -> SubscriptionScreen(viewModel) { viewModel.navigateTo(Screen.Profile) }
            is Screen.ArchOverview -> ArchOverviewScreen(onBack = { viewModel.navigateTo(Screen.Dashboard) })
        }
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun MainNavigationLayout(
    viewModel: BodyFatViewModel,
    activeTab: Screen,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurfaceGray,
                contentColor = NeonCyan,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab is Screen.Dashboard,
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MediumGray,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab is Screen.ProgressHistory,
                    onClick = { viewModel.navigateTo(Screen.ProgressHistory) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MediumGray,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab is Screen.AICoach,
                    onClick = { viewModel.navigateTo(Screen.AICoach) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Coach") },
                    label = { Text("AI Coach", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MediumGray,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab is Screen.Profile,
                    onClick = { viewModel.navigateTo(Screen.Profile) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MediumGray,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBackground)
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

// --- 1. SPLASH SCREEN ---

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated scanner-like bounding box
            val infiniteTransition = rememberInfiniteTransition(label = "SplashScanner")
            val scannerLineY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ScannerY"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // High-tech vector body shape simulated inside the box
                Icon(
                    imageVector = Icons.Default.AccessibilityNew,
                    contentDescription = "Body silhouette",
                    tint = ElectricBlue,
                    modifier = Modifier.fillMaxSize()
                )

                // Laser scan line overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (124 * scannerLineY).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    NeonCyan,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI BODY FAT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = NeonCyan
                )
            )
            Text(
                text = "ANALYZER",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Computer Vision Anthropometrics",
                color = TextGray,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// --- 2. ONBOARDING & SETUP ---

@Composable
fun OnboardingScreen(viewModel: BodyFatViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var onboardingStep by remember { mutableStateOf(0) } // 0: Welcome info, 1: Profile forms

    // Forms fields
    var name by remember { mutableStateOf("Fitness Enthusiast") }
    var age by remember { mutableStateOf(28) }
    var sex by remember { mutableStateOf("Male") }
    var heightCm by remember { mutableStateOf(175f) }
    var weightKg by remember { mutableStateOf(72f) }
    var activityLevel by remember { mutableStateOf("Moderately Active") }
    var waistCm by remember { mutableStateOf("82") }
    var neckCm by remember { mutableStateOf("38") }
    var hipCm by remember { mutableStateOf("95") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (onboardingStep == 0) {
                // Welcome and informational onboarding
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Onboarding",
                        tint = NeonCyan,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Scan. Estimate. Optimize.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Estimate your body fat percentage, lean mass, and skeletal metrics from a simple body photo and direct measurements using localized computer vision modeling and Google Gemini intelligence.",
                        fontSize = 14.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Disclaimer notice
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray),
                        border = BorderStroke(1.dp, OutlineGray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = WarningYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Important: Results are high-fidelity estimates for fitness planning purposes only and do not constitute a medical diagnosis.",
                                fontSize = 12.sp,
                                color = TextGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = { onboardingStep = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_continue_btn")
                ) {
                    Text("Get Started", color = MidnightBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

            } else {
                // Profile & measurement configuration
                Text(
                    text = "Configure Your Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Measurements help anchor the anthropometric computer vision regression models.",
                    fontSize = 13.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = OutlineGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Age
                            OutlinedTextField(
                                value = age.toString(),
                                onValueChange = { age = it.toIntOrNull() ?: age },
                                label = { Text("Age (years)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OutlineGray
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            // Sex
                            var expandedSex by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = sex,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Sex", color = TextGray) },
                                    trailingIcon = {
                                        IconButton(onClick = { expandedSex = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = NeonCyan)
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = OutlineGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = expandedSex,
                                    onDismissRequest = { expandedSex = false },
                                    modifier = Modifier.background(DarkSurfaceGray)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Male", color = TextWhite) },
                                        onClick = { sex = "Male"; expandedSex = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Female", color = TextWhite) },
                                        onClick = { sex = "Female"; expandedSex = false }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Height Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Height", color = TextWhite, fontWeight = FontWeight.Bold)
                                Text("${heightCm.toInt()} cm", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                valueRange = 120f..220f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = OutlineGray
                                )
                            )
                        }
                    }

                    item {
                        // Weight Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weight", color = TextWhite, fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", weightKg)} kg", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                valueRange = 40f..150f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = OutlineGray
                                )
                            )
                        }
                    }

                    item {
                        // Activity Level Selection
                        Column {
                            Text("Activity Level", color = TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            val levels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active")
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                levels.forEach { level ->
                                    val isSelected = activityLevel == level
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonCyan else DarkSurfaceGray)
                                            .border(1.dp, if (isSelected) NeonCyan else OutlineGray, RoundedCornerShape(8.dp))
                                            .clickable { activityLevel = level }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = level,
                                            color = if (isSelected) MidnightBackground else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Optional Circumferences (Enhances Accuracy)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Waist
                            OutlinedTextField(
                                value = waistCm,
                                onValueChange = { waistCm = it },
                                label = { Text("Waist (cm)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OutlineGray
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            // Neck
                            OutlinedTextField(
                                value = neckCm,
                                onValueChange = { neckCm = it },
                                label = { Text("Neck (cm)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OutlineGray
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            // Hip
                            OutlinedTextField(
                                value = hipCm,
                                onValueChange = { hipCm = it },
                                label = { Text("Hip (cm)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OutlineGray
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val profile = UserProfile(
                            name = name,
                            age = age,
                            sex = sex,
                            heightCm = heightCm,
                            weightKg = weightKg,
                            activityLevel = activityLevel,
                            waistCm = waistCm.toFloatOrNull(),
                            neckCm = neckCm.toFloatOrNull(),
                            hipCm = hipCm.toFloatOrNull()
                        )
                        viewModel.saveProfile(profile)
                        viewModel.navigateTo(Screen.Dashboard)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_onboarding_profile")
                ) {
                    Text("Save & Enter Dashboard", color = MidnightBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// --- MOCK AUTH SCREENS ---

@Composable
fun MockLoginScreen(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Login to AI Body Fat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onSuccess) { Text("Login") }
        }
    }
}

@Composable
fun MockRegisterScreen(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Register Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onSuccess) { Text("Register") }
        }
    }
}


// --- 3. HOME DASHBOARD ---

@Composable
fun DashboardScreen(viewModel: BodyFatViewModel) {
    val context = LocalContext.current
    val recordsState = viewModel.allRecords.collectAsState(initial = emptyList())
    val profileState = viewModel.userProfile.collectAsState(initial = null)
    val latestRecordState = viewModel.latestRecord.collectAsState(initial = null)

    val records = recordsState.value
    val profile = profileState.value
    val latestRecord = latestRecordState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcoming header card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${profile?.name?.split(" ")?.firstOrNull() ?: "User"}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = "Premium Fitness Silhouette Scanning",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                // High tech system diagram button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MediumGray)
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(Screen.ArchOverview) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schema, contentDescription = "Developer Desk", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dev Desk", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Body Fat Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineGray, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Latest Scan Body Fat %",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val bf = latestRecord?.bodyFatPercentage ?: 0f
                    val category = latestRecord?.fitnessCategory ?: "No Scans Yet"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Canvas Circle Gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                drawArc(
                                    color = OutlineGray,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                                // Active color gradient sweep
                                val sweep = (bf / 50f) * 270f
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(ElectricBlue, NeonCyan)),
                                    startAngle = 135f,
                                    sweepAngle = sweep.coerceIn(0f, 270f),
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (bf > 0f) "$bf%" else "--",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "Confidence: ${latestRecord?.confidenceScore ?: "N/A"}",
                                    fontSize = 9.sp,
                                    color = TextGray
                                )
                            }
                        }

                        // Basic detailed text values
                        Column {
                            Text("Lean Mass: ${latestRecord?.leanBodyMassKg ?: "--"} kg", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Fat Mass: ${latestRecord?.fatMassKg ?: "--"} kg", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Muscle: ${latestRecord?.estimatedMuscleMassKg ?: "--"} kg", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(category, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Scanning Floating Card Action
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricBlue, LaserPurple)
                        )
                    )
                    .clickable { viewModel.navigateTo(Screen.LiveScan) }
                    .padding(18.dp)
                    .testTag("start_scan_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Launch Active Scan",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Uses YOLOv11 silhouette capture and Google Gemini multimodality analysis.",
                            color = TextWhite.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TextWhite.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = "Camera", tint = TextWhite, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // Mini Charts and Secondary Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BMI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("BMI", color = TextGray, fontSize = 12.sp)
                        Text(
                            text = if (latestRecord != null) "${latestRecord!!.bmi}" else "--",
                            color = ElectricBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                latestRecord == null -> "No data"
                                latestRecord!!.bmi < 18.5f -> "Underweight"
                                latestRecord!!.bmi < 25f -> "Normal"
                                else -> "Overweight"
                            },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Waist-Height Ratio
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Waist/Height Ratio", color = TextGray, fontSize = 12.sp)
                        Text(
                            text = if (latestRecord != null) "${latestRecord!!.waistToHeightRatio}" else "--",
                            color = LaserPurple,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                latestRecord == null -> "No data"
                                latestRecord!!.waistToHeightRatio < 0.5f -> "Optimal"
                                else -> "High Risk"
                            },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Historical linear regression visual simulation (Compose Canvas Chart)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineGray, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Body Fat Progress Timeline",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (records.size < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Perform multiple scans to view timeline progress chart.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Drawing local line chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val reversedList = records.reversed()
                            val maxVal = reversedList.maxOf { it.bodyFatPercentage } + 3f
                            val minVal = (reversedList.minOf { it.bodyFatPercentage } - 3f).coerceAtLeast(0f)
                            val range = maxVal - minVal
                            val listSize = reversedList.size

                            val points = reversedList.mapIndexed { index, rec ->
                                val x = (index.toFloat() / (listSize - 1)) * this.size.width
                                val y = this.size.height - (((rec.bodyFatPercentage - minVal) / range) * this.size.height)
                                Offset(x, y)
                            }

                            // Draw grid lines
                            drawLine(Color.Gray.copy(alpha = 0.2f), Offset(0f, this.size.height/2), Offset(this.size.width, this.size.height/2), strokeWidth = 1f)

                            // Draw line and points
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = NeonCyan,
                                    start = points[i],
                                    end = points[i+1],
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            points.forEach { pt ->
                                drawCircle(
                                    color = ElectricBlue,
                                    radius = 6.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = NeonCyan,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick actions to direct advice
        item {
            Text("Diet & Training Routines", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MediumGray)
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(Screen.DietPlans) }
                        .padding(14.dp)
                ) {
                    Column {
                        Icon(Icons.Default.Restaurant, contentDescription = "Diet", tint = NeonCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Diet suggestions", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Macronutrient breakdowns", color = TextGray, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MediumGray)
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(Screen.WorkoutPlans) }
                        .padding(14.dp)
                ) {
                    Column {
                        Icon(Icons.Default.FitnessCenter, contentDescription = "Workout", tint = ElectricBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Workout suggestions", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Hypertrophy routines", color = TextGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- 4. LIVE SCAN (CAMERA & LANDMARKS) ---

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LiveScanScreen(viewModel: BodyFatViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val activeAngle by viewModel.activeCameraAngle.collectAsState()
    val frontImage by viewModel.frontImage.collectAsState()
    val sideImage by viewModel.sideImage.collectAsState()
    val backImage by viewModel.backImage.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                } else {
                    cameraProviderFuture.addListener({
                        try {
                            cameraProviderFuture.get().unbindAll()
                        } catch (e: Exception) {
                            Log.e("CameraX", "Error unbinding on dispose", e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            } catch (e: Exception) {
                Log.e("CameraX", "Error in DisposableEffect dispose", e)
            }
        }
    }

    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextWhite)
                }
                Text("Body Silhouette Capture", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                // Reset photos button
                Text(
                    text = "Reset",
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { viewModel.resetPhotos() }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching selector Front, Side, Back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val angles = listOf("Front", "Side", "Back")
                angles.forEach { angle ->
                    val isSelected = activeAngle == angle
                    val hasPhoto = when(angle) {
                        "Front" -> frontImage != null
                        "Side" -> sideImage != null
                        "Back" -> backImage != null
                        else -> false
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan else DarkSurfaceGray)
                            .border(1.dp, if (isSelected) NeonCyan else OutlineGray, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setActiveCameraAngle(angle) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = angle,
                                color = if (isSelected) MidnightBackground else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (hasPhoto) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Captured", tint = if (isSelected) MidnightBackground else SuccessGreen, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Real Camera Frame or High Fidelity Mock Silhouette Capture Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(2.dp, OutlineGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (cameraPermissionState.status.isGranted) {
                    // Standard CameraX viewfinder
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().apply {
                                    setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (e: Exception) {
                                    Log.e("CameraX", "Binding failed", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback visual indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideocamOff, contentDescription = "Camera Permission Required", tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Grant Camera Permission to Scan", color = TextGray, fontSize = 13.sp)
                    }
                }

                // HIGH TECH COMPRESSED SKELETON POSE AND SILHOUETTE MESH DRAWN ON CANVAS
                val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
                val radarAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "RadarAngle"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw guide landing crosshairs
                    val stroke = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    drawLine(Color.Cyan.copy(alpha = 0.3f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1.dp.toPx())
                    drawLine(Color.Cyan.copy(alpha = 0.3f), Offset(0f, size.height * 0.4f), Offset(size.width, size.height * 0.4f), strokeWidth = 1.dp.toPx())

                    // Draw body framing box
                    drawRect(
                        color = NeonCyan.copy(alpha = 0.25f),
                        topLeft = Offset(size.width * 0.15f, size.height * 0.1f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.8f),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                    )

                    // Draw simulated body landmark nodes (Pose estimation points)
                    val points = listOf(
                        Offset(size.width * 0.5f, size.height * 0.18f), // Head
                        Offset(size.width * 0.5f, size.height * 0.28f), // Chest
                        Offset(size.width * 0.4f, size.height * 0.28f), // Left Shoulder
                        Offset(size.width * 0.6f, size.height * 0.28f), // Right Shoulder
                        Offset(size.width * 0.35f, size.height * 0.45f), // Left Elbow
                        Offset(size.width * 0.65f, size.height * 0.45f), // Right Elbow
                        Offset(size.width * 0.42f, size.height * 0.55f), // Left Hip
                        Offset(size.width * 0.58f, size.height * 0.55f), // Right Hip
                        Offset(size.width * 0.43f, size.height * 0.72f), // Left Knee
                        Offset(size.width * 0.57f, size.height * 0.72f), // Right Knee
                        Offset(size.width * 0.43f, size.height * 0.88f), // Left Ankle
                        Offset(size.width * 0.57f, size.height * 0.88f)  // Right Ankle
                    )

                    // Draw skeleton connections
                    val connections = listOf(
                        Pair(0, 1), Pair(1, 2), Pair(1, 3), Pair(2, 4), Pair(3, 5),
                        Pair(2, 6), Pair(3, 7), Pair(6, 7), Pair(6, 8), Pair(7, 9), Pair(8, 10), Pair(9, 11)
                    )

                    connections.forEach { conn ->
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.5f),
                            start = points[conn.first],
                            end = points[conn.second],
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    points.forEach { pt ->
                        drawCircle(
                            color = ElectricBlue,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // Captured Image preview overlay (bottom-left)
                val activeBitmap = when(activeAngle) {
                    "Front" -> frontImage
                    "Side" -> sideImage
                    "Back" -> backImage
                    else -> null
                }
                if (activeBitmap != null) {
                    Image(
                        bitmap = activeBitmap.asImageBitmap(),
                        contentDescription = "Angle preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                    )
                }
            }

            // Capture CTA Trigger Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large capture circle button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(TextWhite)
                        .clickable {
                            // High precision mock image generator matching angle
                            val width = 480
                            val height = 640
                            val mockBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(mockBitmap)
                            val paint = android.graphics.Paint()
                            paint.color = android.graphics.Color.DKGRAY
                            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                            
                            // Save captured bitmap to ViewModel
                            when (activeAngle) {
                                "Front" -> viewModel.setFrontImage(mockBitmap)
                                "Side" -> viewModel.setSideImage(mockBitmap)
                                "Back" -> viewModel.setBackImage(mockBitmap)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, MidnightBackground, CircleShape)
                            .background(ElectricBlue)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val readyToAnalyze = frontImage != null
                Button(
                    onClick = { viewModel.runBodyFatAnalysis() },
                    enabled = readyToAnalyze,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        disabledContainerColor = OutlineGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_capture_btn")
                ) {
                    Text(
                        text = if (readyToAnalyze) "Execute AI Analysis Pipeline" else "Capture Front View to Analyze",
                        color = if (readyToAnalyze) MidnightBackground else TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// --- 5. PROCESSING SCREEN ---

@Composable
fun ProcessingScreen(viewModel: BodyFatViewModel) {
    val steps = viewModel.pipelineSteps
    val logs by viewModel.analysisLogs.collectAsState()
    val activeIndex by viewModel.currentStepIndex.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Inference Engine",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Pulse loading indicator
            CircularProgressIndicator(
                color = ElectricBlue,
                trackColor = OutlineGray,
                modifier = Modifier.size(80.dp),
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scrollable console/logs output showing MediaPipe, YOLOv11 and SAM 2 logs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(logs.reversed()) { log ->
                        Text(
                            text = log,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Executing pipeline calculations: Step ${activeIndex + 1} of ${steps.size}",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- 6. RESULTS SCREEN ---

@Composable
fun ResultsScreen(viewModel: BodyFatViewModel) {
    val result by viewModel.analysisResult.collectAsState()

    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Dashboard", tint = NeonCyan)
                }
                Text("Estimation Results", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                // Save/Done button
                Text(
                    text = "Done",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Dashboard) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineGray, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Body Composition Analysis",
                            fontSize = 14.sp,
                            color = TextGray,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Large body fat percentage circle gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(130.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { (result?.bodyFatPercentage ?: 0f) / 50f },
                                color = NeonCyan,
                                strokeWidth = 8.dp,
                                modifier = Modifier.size(130.dp),
                                trackColor = OutlineGray
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${result?.bodyFatPercentage ?: "--"}%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "Estimated Body Fat",
                                    fontSize = 11.sp,
                                    color = TextWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI", color = TextGray, fontSize = 12.sp)
                                Text("${result?.bmi ?: "--"}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Confidence", color = TextGray, fontSize = 12.sp)
                                Text("${result?.confidenceScore ?: "--"}", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Category", color = TextGray, fontSize = 12.sp)
                                Text("${result?.fitnessCategory ?: "--"}", color = LaserPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // AI Coach assessment block
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = "AI Coach", tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Silhouette Assessment", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result?.analysis ?: "No scan analysis generated.",
                            color = TextWhite,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Grid layout for weight compositions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lean Mass
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Lean Body Mass", color = TextGray, fontSize = 11.sp)
                            Text("${result?.leanBodyMassKg ?: "--"} kg", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Fat Mass
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Estimated Fat Mass", color = TextGray, fontSize = 11.sp)
                            Text("${result?.fatMassKg ?: "--"} kg", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Muscle Mass
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Muscle Mass", color = TextGray, fontSize = 11.sp)
                            Text("${result?.estimatedMuscleMassKg ?: "--"} kg", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Suggestions title
            item {
                Text("Recommendations Profile", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Diet Suggestions List
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restaurant, contentDescription = "Diet Suggestions", tint = ElectricBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diet & Caloric Adjustments", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        result?.dietSuggestions?.forEach { sug ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("•", color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.width(12.dp))
                                Text(sug, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            // Workout Suggestions List
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = "Workout Suggestions", tint = LaserPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Workout & Split Suggestions", color = LaserPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        result?.workoutSuggestions?.forEach { sug ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("•", color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.width(12.dp))
                                Text(sug, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            // Disclaimers Footer
            item {
                Text(
                    text = "Medical Disclaimer: Estimations are generated from anthropometric heuristics and multimodal Vision transformer regression models. These indices do not establish clinical wellness diagnoses. Consult medical providers before starting any intense calorie deficit or powerlifting routines.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

// --- 7. PROGRESS HISTORY & COMPARE ---

@Composable
fun ProgressHistoryScreen(viewModel: BodyFatViewModel) {
    val recordsState = viewModel.allRecords.collectAsState(initial = emptyList())
    val records = recordsState.value

    var showCompareMode by remember { mutableStateOf(false) }
    var selectedBeforeIndex by remember { mutableStateOf(-1) }
    var selectedAfterIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Inference Log History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Switch to Compare Mode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showCompareMode) NeonCyan else MediumGray)
                    .clickable { showCompareMode = !showCompareMode }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (showCompareMode) "Logs List" else "Compare",
                    color = if (showCompareMode) MidnightBackground else NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudQueue, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No scans logged in local database.", color = TextGray, fontSize = 14.sp)
                }
            }
        } else if (showCompareMode) {
            // Before & After comparison visualizer
            Column(modifier = Modifier.weight(1f)) {
                Text("Select Two Scans to Compare", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable selectors
                Text("Before Scan Point", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records.size) { idx ->
                        val rec = records[idx]
                        val isSelected = selectedBeforeIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricBlue else DarkSurfaceGray)
                                .border(1.dp, if (isSelected) ElectricBlue else OutlineGray, RoundedCornerShape(8.dp))
                                .clickable { selectedBeforeIndex = idx }
                                .padding(12.dp)
                        ) {
                            Column {
                                val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(rec.date))
                                Text(dateStr, color = if (isSelected) MidnightBackground else TextWhite, fontWeight = FontWeight.Bold)
                                Text("${rec.bodyFatPercentage}%", color = if (isSelected) MidnightBackground else NeonCyan, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("After Scan Point", color = LaserPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records.size) { idx ->
                        val rec = records[idx]
                        val isSelected = selectedAfterIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LaserPurple else DarkSurfaceGray)
                                .border(1.dp, if (isSelected) LaserPurple else OutlineGray, RoundedCornerShape(8.dp))
                                .clickable { selectedAfterIndex = idx }
                                .padding(12.dp)
                        ) {
                            Column {
                                val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(rec.date))
                                Text(dateStr, color = if (isSelected) MidnightBackground else TextWhite, fontWeight = FontWeight.Bold)
                                Text("${rec.bodyFatPercentage}%", color = if (isSelected) MidnightBackground else NeonCyan, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Comparison outputs
                if (selectedBeforeIndex >= 0 && selectedAfterIndex >= 0) {
                    val before = records[selectedBeforeIndex]
                    val after = records[selectedAfterIndex]
                    val bfDiff = after.bodyFatPercentage - before.bodyFatPercentage
                    val weightDiff = after.weightKg - before.weightKg

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Composition Variance", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Weight Variance", color = TextGray, fontSize = 12.sp)
                                    Text(
                                        text = "${if (weightDiff >= 0) "+" else ""}${String.format("%.1f", weightDiff)} kg",
                                        color = if (weightDiff <= 0) SuccessGreen else ErrorRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Column {
                                    Text("Body Fat % Variance", color = TextGray, fontSize = 12.sp)
                                    Text(
                                        text = "${if (bfDiff >= 0) "+" else ""}${String.format("%.1f", bfDiff)}%",
                                        color = if (bfDiff <= 0) SuccessGreen else ErrorRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select before and after scan points to model progress gradients.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            // Simple History list logs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineGray, RoundedCornerShape(12.dp))
                            .clickable {
                                // Reload result to results screen
                                viewModel.runBodyFatAnalysis()
                            },
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val dateStr = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date(rec.date))
                                Text(dateStr, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Weight: ${rec.weightKg} kg | BMI: ${rec.bmi}",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${rec.bodyFatPercentage}%", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text(rec.fitnessCategory, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 8. AI COACH & PLAN SCREENS ---

@Composable
fun AICoachScreen(viewModel: BodyFatViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val reply by viewModel.aiCoachResponse.collectAsState()
    val loading by viewModel.isAiCoachLoading.collectAsState()
    var userQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "AI Athletic Coach",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            "Powered by Google Gemini generative modeling.",
            fontSize = 12.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chat Bubble Scroll Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, OutlineGray, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (reply == null && !loading) {
                    // Prepopulate recommended quick actions
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = "Coach", tint = NeonCyan, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Ask Coach anything about body composition, macros, or lifting techniques.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        val suggestions = listOf(
                            "How do I break a 15% body fat plateau?",
                            "Give me a 4-day Hypertrophy training split.",
                            "What macro split is best for body recomposition?"
                        )

                        suggestions.forEach { sug ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MediumGray)
                                    .border(1.dp, OutlineGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        userQuery = sug
                                        viewModel.askAiCoach(sug)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(sug, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                } else {
                    // Render actual reply markdown scrollable text
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(
                            text = reply ?: "",
                            color = TextWhite,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input TextField Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userQuery,
                onValueChange = { userQuery = it },
                placeholder = { Text("Ask Coach...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = OutlineGray
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userQuery.isNotBlank()) {
                        viewModel.askAiCoach(userQuery)
                        keyboardController?.hide()
                    }
                }),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (userQuery.isNotBlank()) {
                        viewModel.askAiCoach(userQuery)
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MidnightBackground)
            }
        }
    }
}

// --- 9. WORKOUT & DIET DETAILS ---

@Composable
fun WorkoutPlansScreen(viewModel: BodyFatViewModel) {
    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                }
                Text("Workout Blueprints", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                InfoCard(
                    title = "Optimized Muscle Building (4-Day Split)",
                    description = "Focus on compound progressive overload. Maintain consistent dynamic rest parameters of 90-120s between work sets."
                )
            }
            item {
                SectionHeader(title = "Push Day (Chest, Shoulders, Triceps)", icon = Icons.Default.FitnessCenter)
                CodeBlock(
                    """
1. Incline Barbell Press: 4 sets x 6-8 reps (Heavy)
2. Dumbbell Flat Bench: 3 sets x 8-10 reps
3. Overhead Dumbbell Press: 3 sets x 8-10 reps
4. Standing Lateral Cable Raises: 4 sets x 12-15 reps
5. Cable Triceps Pushdowns: 3 sets x 10-12 reps
                    """.trimIndent()
                )
            }
            item {
                SectionHeader(title = "Pull Day (Back, Rear Delts, Biceps)", icon = Icons.Default.FitnessCenter)
                CodeBlock(
                    """
1. Conventional Deadlifts: 3 sets x 5 reps
2. Chest-Supported T-Bar Rows: 3 sets x 8-10 reps
3. Lat Pulldowns (Wide Grip): 4 sets x 10-12 reps
4. Incline Dumbbell Biceps Curls: 3 sets x 10-12 reps
5. Reverse Cable Flyes: 3 sets x 15 reps
                    """.trimIndent()
                )
            }
            item {
                SectionHeader(title = "Legs Day (Quad & Hamstring Focus)", icon = Icons.Default.FitnessCenter)
                CodeBlock(
                    """
1. Barbell Back Squats: 4 sets x 6-8 reps
2. Romanian Deadlifts: 3 sets x 8-10 reps
3. Bulgarian Split Squats: 3 sets x 10-12 reps per leg
4. Standing Calf Raises: 4 sets x 15 reps
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
fun DietPlansScreen(viewModel: BodyFatViewModel) {
    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                }
                Text("Macronutrient Blueprint", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                InfoCard(
                    title = "Lean Recomposition Diet Splittings",
                    description = "Aim to prioritize organic high-quality macronutrients to support energy thresholds while promoting continuous fat tissue reduction."
                )
            }

            item {
                SectionHeader(title = "Target Nutritional Split", icon = Icons.Default.Restaurant)
                CodeBlock(
                    """
Caloric Target: ~2100 kcal (Adjustment based on BMI)
- Protein: 40% (210g) - Supports nitrogen balance and skeletal repair
- Carbohydrates: 30% (157g) - Low-glycemic indexes for continuous energy
- Fats: 30% (70g) - For natural endocrine hormone production
                    """.trimIndent()
                )
            }

            item {
                SectionHeader(title = "Sample Day Meal Template", icon = Icons.Default.Restaurant)
                CodeBlock(
                    """
• Meal 1: 4 Egg Whites + 1 Whole Egg, 60g Rolled Oats, 50g Blueberries
• Meal 2: 200g Grilled Chicken Breast, 150g Sweet Potato, Steamed Asparagus
• Meal 3: 1 Scoop Whey Isolate, 30g Almonds
• Meal 4: 200g Lean Beef Tenderloin, 100g Cooked Jasmine Rice, Broccoli
                    """.trimIndent()
                )
            }
        }
    }
}

// --- 10. PROFILE & SUBSCRIPTIONS ---

@Composable
fun ProfileScreen(viewModel: BodyFatViewModel) {
    val profileState = viewModel.userProfile.collectAsState(initial = null)
    val profile = profileState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Physical Profile Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            "Manage physical indices and premium tiers.",
            fontSize = 12.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineGray, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(profile?.name ?: "User", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${profile?.age ?: 28} years old • ${profile?.sex ?: "Male"}", color = TextGray, fontSize = 12.sp)
                    }

                    // Subscription state badge
                    val isPremium = profile?.isPremium ?: false
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPremium) NeonCyan else MediumGray)
                            .clickable { viewModel.navigateTo(Screen.Subscription) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isPremium) "Premium Member" else "Upgrade to Pro",
                            color = if (isPremium) MidnightBackground else NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = OutlineGray)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Anchor Physical Metrics", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Height", color = TextGray)
                    Text("${profile?.heightCm?.toInt() ?: 175} cm", color = TextWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Weight", color = TextGray)
                    Text("${profile?.weightKg ?: 72f} kg", color = TextWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Activity Metric", color = TextGray)
                    Text(profile?.activityLevel ?: "Moderately Active", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clear local database history action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MediumGray)
                .border(1.dp, OutlineGray, RoundedCornerShape(12.dp))
                .clickable { viewModel.clearAllRecords() }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Clear database", tint = ErrorRed)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Clear Local Database Scans", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Irreversibly wipes logged scan records.", color = TextGray, fontSize = 11.sp)
                }
            }
        }
    }
}

// --- SUBSCRIPTION OPT IN ---

@Composable
fun SubscriptionScreen(viewModel: BodyFatViewModel, onBack: () -> Unit) {
    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                }
                Text("Unlock Premium Scan Analytics", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Box(modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Star, contentDescription = "Upgrade", tint = NeonCyan, modifier = Modifier.size(72.dp))

            Text(
                "Access Pro Computer Vision Pipelines",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Text(
                "Subscribers bypass standard queue rates and gain direct, unlimited access to cloud GPU clusters processing multi-angle pose land-marking and YOLOv11 silhouette segmentations with Google Gemini AI.",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeonCyan, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Monthly Elite Membership", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Unlimited cloud GPU scans & full AI coaching access.", color = TextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("${'$'}9.99 / Month", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }

            Button(
                onClick = {
                    viewModel.saveProfile(UserProfile(isPremium = true))
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Upgrade Now", color = MidnightBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
