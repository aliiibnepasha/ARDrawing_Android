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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ardrawing.R
import com.example.ardrawing.data.local.database.AppDatabase
import com.example.ardrawing.data.repository.FavoriteRepository
import com.example.ardrawing.data.utils.AssetUtils
import com.example.ardrawing.ui.components.WaterWaveBackground
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun CreateWithAIScreen(
    onBackClick: () -> Unit,
    onUseToDraw: (android.graphics.Bitmap) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val favoriteRepository = remember { FavoriteRepository(database.favoriteDao()) }
    
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3 // Style, Difficulty, Prompt. The Result is a separate state/screen logically.

    // Selections
    var selectedStyle by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>("Easy") }
    var promptText by remember { mutableStateOf("") }
    
    // Result State
    var isGenerating by remember { mutableStateOf(false) }
    var generatedImageVisible by remember { mutableStateOf(false) }
    var generatedImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    
    // Check if current prompt is favorite
    LaunchedEffect(promptText, generatedImageVisible) {
        if (generatedImageVisible && promptText.isNotEmpty()) {
            isFavorite = favoriteRepository.isFavorite(promptText)
        }
    }
    
    // Generate mock bitmap for the image (in real app, this would come from AI generation)
    LaunchedEffect(generatedImageVisible) {
        if (generatedImageVisible && generatedImageBitmap == null) {
            // Create a high-quality mock bitmap from the drawable
            val drawable = context.resources.getDrawable(R.drawable.create_with_ai, null)
            // Use larger size for better quality
            val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
            val height = width // Square
            val bitmap = android.graphics.Bitmap.createBitmap(
                width,
                height,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
            generatedImageBitmap = bitmap
        }
    }

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
                    
                    Text(
                        text = "Done",
                        color = Color(0xFF4285F4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        modifier = Modifier.clickable { /* Done action */ }
                    )
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
                    onClick = {
                        generatedImageBitmap?.let { bitmap ->
                            onUseToDraw(bitmap)
                        }
                    },
                    enabled = generatedImageBitmap != null,
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
                StepResult(
                    prompt = promptText,
                    imageBitmap = generatedImageBitmap,
                    isFavorite = isFavorite,
                    onFavoriteClick = {
                        scope.launch {
                            if (generatedImageBitmap != null) {
                                // Save image and favorite
                                val imagePath = com.example.ardrawing.utils.FavoriteImageUtils.saveFavoriteImage(
                                    generatedImageBitmap!!,
                                    context,
                                    promptText
                                )
                                if (imagePath != null) {
                                    favoriteRepository.insertFavorite(
                                        com.example.ardrawing.data.local.entity.Favorite(
                                            prompt = promptText,
                                            imagePath = imagePath,
                                            type = "create_with_ai"
                                        )
                                    )
                                    isFavorite = true
                                }
                            } else {
                                // Toggle favorite (remove if exists)
                                favoriteRepository.deleteFavoriteByPrompt(promptText)
                                isFavorite = false
                            }
                        }
                    }
                )
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
    val context = LocalContext.current
    
    // Load style images from assets/styles folder
    val styleImages = remember {
        AssetUtils.listImageFiles(context, "styles").sorted()
    }
    
    val styleNames = listOf("Anime", "People", "Game", "Pencil", "Cute", "Aesthetic")
    
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Choose style",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        val styles = remember(styleImages) {
            styleImages.take(6).mapIndexed { index, imageFile ->
                StyleItem(
                    name = styleNames.getOrElse(index) { "Style ${index + 1}" },
                    assetPath = "styles/$imageFile"
                )
            }
        }

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
                        painter = rememberAssetImagePainter(style.assetPath),
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

data class StyleItem(val name: String, val assetPath: String)

// ================= STEP 2: SELECT DIFFICULTY =================
@Composable
fun StepSelectDifficulty(
    selectedDifficulty: String?,
    onDifficultySelected: (String) -> Unit
) {
    val context = LocalContext.current
    
    // Load level images from assets/levels folder
    val levelImages = remember {
        AssetUtils.listImageFiles(context, "levels").sorted()
    }
    
    val levels = listOf("Easy", "Intermediate", "PRO", "Color", "Color PRO")
    
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Select difficulty level",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            levels.forEachIndexed { index, level ->
                val isSelected = selectedDifficulty == level
                val levelImagePath = if (index < levelImages.size) {
                    "levels/${levelImages[index]}"
                } else null
                
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
                    // Level Image
                    if (levelImagePath != null) {
                        Image(
                            painter = rememberAssetImagePainter(levelImagePath),
                            contentDescription = level,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        )
                    }
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
fun StepResult(
    prompt: String,
    imageBitmap: android.graphics.Bitmap? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
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
            // Generated Image or Mock Image
            if (imageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = "Generated Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
             Image(
                    painter = painterResource(R.drawable.create_with_ai),
                 contentDescription = "Generated Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Icons Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Favorite Icon (Top Left)
                Image(
                    painter = painterResource(
                        if (isFavorite) R.drawable.my_fav_blue_ic 
                        else R.drawable.my_fav_unfill
                    ),
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopStart)
                        .clickable { onFavoriteClick() }
                 )
            }
        }
    }
}
