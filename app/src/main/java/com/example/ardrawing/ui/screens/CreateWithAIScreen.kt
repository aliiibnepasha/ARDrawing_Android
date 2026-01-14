package com.example.ardrawing.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.ui.components.WaterWaveBackground
import kotlinx.coroutines.delay

@Composable
fun CreateWithAIScreen(
    onBackClick: () -> Unit,
    onUseToDraw: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3 // Style, Difficulty, Prompt. The Result is a separate state/screen logically.

    // Selections
    var selectedStyle by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>("Easy") }
    var promptText by remember { mutableStateOf("") }
    
    // Result State
    var isGenerating by remember { mutableStateOf(false) }
    var generatedImageVisible by remember { mutableStateOf(false) }

    // Handle Back Press
    BackHandler {
        if (generatedImageVisible) {
            generatedImageVisible = false
            currentStep = 3
        } else if (currentStep > 1) {
            currentStep--
        } else {
            onBackClick()
        }
    }

    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()
        
        // 2. Foreground Content
        Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
        // Removed explicit contentWindowInsets override to let Scaffold handle defaults, 
        // but we need to pad the custom topBar manually.
        topBar = {
            if (!generatedImageVisible) {
                // Wizard Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars) // Fix status bar overlap
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Back Button
                        Image(
                            painter = painterResource(R.drawable.back_arrow_ic),
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    if (currentStep > 1) currentStep-- else onBackClick()
                                }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(totalSteps) { index ->
                                val stepNum = index + 1
                                val isActive = stepNum <= currentStep
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isActive) Color(0xFF4285F4) else Color(0xFFE0E0E0))
                                )
                            }
                        }
                    }
                }
            } else {
                // Result Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars) // Fix status bar overlap
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.back_arrow_ic),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                generatedImageVisible = false
                                currentStep = 3
                            }
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "AI Image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = { /* Done action */ }) {
                         Text("Done", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            if (!generatedImageVisible) {
                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            // Generate Action
                            isGenerating = true
                        }
                    },
                    enabled = (currentStep == 1 && selectedStyle != null) || 
                              (currentStep == 2 && selectedDifficulty != null) ||
                              (currentStep == 3 && promptText.isNotEmpty()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars) // Fix navigation bar overlap
                        .padding(20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4),
                        disabledContainerColor = Color(0xFF4285F4).copy(alpha = 0.5f)
                    )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White, 
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (currentStep == 3) "Generate" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onUseToDraw,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars) // Fix navigation bar overlap
                        .padding(20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("Use To Draw", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        
        // Fake generation delay logic
        LaunchedEffect(isGenerating) {
            if (isGenerating) {
                delay(2000)
                isGenerating = false
                generatedImageVisible = true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!generatedImageVisible) {
                when (currentStep) {
                    1 -> StepChooseStyle(
                        selectedStyle = selectedStyle,
                        onStyleSelected = { selectedStyle = it }
                    )
                    2 -> StepSelectDifficulty(
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelected = { selectedDifficulty = it }
                    )
                    3 -> StepDescribeImage(
                        text = promptText,
                        onTextChange = { promptText = it }
                    )
                }
            } else {
                StepResult(prompt = promptText)
            }
        }
        }
    }
}

// ================= STEP 1: CHOOSE STYLE =================
@Composable
fun StepChooseStyle(
    selectedStyle: String?,
    onStyleSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Choose style",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        val styles = listOf(
            StyleItem("Anime", R.drawable.home_avtr), // Placeholders
            StyleItem("People", R.drawable.text_avtr),
            StyleItem("Game", R.drawable.photo_to_sketch),
            StyleItem("Pencil", R.drawable.magic_pen),
            StyleItem("Cute", R.drawable.text_icon), // Rabbit-ish?
            StyleItem("Aesthetic", R.drawable.create_with_ai)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(styles) { style ->
                val isSelected = selectedStyle == style.name
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF4285F4) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onStyleSelected(style.name) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(style.iconRes),
                        contentDescription = style.name,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = style.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

data class StyleItem(val name: String, val iconRes: Int)

// ================= STEP 2: SELECT DIFFICULTY =================
@Composable
fun StepSelectDifficulty(
    selectedDifficulty: String?,
    onDifficultySelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Select difficulty level",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        val levels = listOf("Easy", "Intermediate", "PRO", "Color", "Color PRO")

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            levels.forEach { level ->
                val isSelected = selectedDifficulty == level
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF4285F4) else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(if (isSelected) Color(0xFFF5F9FF) else Color(0xFFF8F8F8))
                        .clickable { onDifficultySelected(level) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder Icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = level,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF4285F4) else Color.Black
                    )
                }
            }
        }
    }
}

// ================= STEP 3: DESCRIBE IMAGE =================
@Composable
fun StepDescribeImage(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Describe the image you want to create",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8F8F8))
                .padding(16.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "Example: Girl studying",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                ),
                cursorBrush = SolidColor(Color(0xFF4285F4)),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ================= STEP 4: RESULT =================
@Composable
fun StepResult(prompt: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = prompt,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square result
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(24.dp))
        ) {
             // Mock Result Image (Rabbit placeholder or similar)
             // Using create_with_ai for now as it's an vector/image
             Image(
                 painter = painterResource(R.drawable.create_with_ai), // Using available asset
                 contentDescription = "Generated Image",
                 modifier = Modifier
                     .align(Alignment.Center)
                     .size(200.dp),
                 contentScale = ContentScale.Fit
             )

            // Retry Icon
            IconButton(
                onClick = { /* Retry */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                 Icon(
                     imageVector = Icons.Default.Refresh,
                     contentDescription = "Regenerate",
                     tint = Color(0xFF4285F4)
                 )
            }
        }
    }
}
