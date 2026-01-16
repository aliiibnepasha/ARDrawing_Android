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
import androidx.compose.ui.res.colorResource
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
import com.example.ardrawing.data.repository.TextToImageRepository
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
    
    // API Repository
    val textToImageRepository = remember { TextToImageRepository() }
    
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3 // Style, Difficulty, Prompt. The Result is a separate state/screen logically.

    // Selections - Will be set to first style by default in StepChooseStyle
    var selectedStyle by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>("Easy") }
    var promptText by remember { mutableStateOf("") }
    
    // Result State
    var isGenerating by remember { mutableStateOf(false) }
    var generatedImageVisible by remember { mutableStateOf(false) }
    var generatedImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Check if current prompt is favorite
    LaunchedEffect(promptText, generatedImageVisible) {
        if (generatedImageVisible && promptText.isNotEmpty()) {
            isFavorite = favoriteRepository.isFavorite(promptText)
        }
    }
    
    // Generate image using API when user clicks Generate button
    fun generateImage() {
        if (promptText.isBlank()) return
        
        scope.launch {
            isGenerating = true
            errorMessage = null
            generatedImageBitmap = null
            
            val result = textToImageRepository.generateImage(
                prompt = promptText.trim(),
                aspectRatio = "1:1"
            )
            
            isGenerating = false
            
            result.onSuccess { bitmap ->
                generatedImageBitmap = bitmap
                generatedImageVisible = true
            }.onFailure { error ->
                // Always show user-friendly message, ignore technical error details
                errorMessage = "Failed to generate image"
                generatedImageVisible = false
            }
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
                                        .background(if (isActive) colorResource(R.color.app_blue) else Color(0xFFE0E0E0))
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
                        color = colorResource(R.color.app_blue),
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
                            // Generate Action - Call API
                            generateImage()
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
                        containerColor = colorResource(R.color.app_blue),
                        disabledContainerColor = colorResource(R.color.app_blue).copy(alpha = 0.5f)
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
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.app_blue))
                ) {
                    Text("Use To Draw", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        
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
                    3 -> Column {
                        StepDescribeImage(
                            text = promptText,
                            onTextChange = { promptText = it }
                        )
                        
                        // Error message display
                        errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            } else {
                StepResult(
                    prompt = promptText,
                    imageBitmap = generatedImageBitmap,
                    isFavorite = isFavorite,
                    onFavoriteClick = {
                        scope.launch {
                            if (generatedImageBitmap != null) {
                                if (isFavorite) {
                                    // Remove from favorites
                                    favoriteRepository.deleteFavoriteByPrompt(promptText)
                                    isFavorite = false
                                } else {
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
                                }
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
    
    val styles = remember(styleImages) {
        styleImages.take(6).mapIndexed { index, imageFile ->
            StyleItem(
                name = styleNames.getOrElse(index) { "Style ${index + 1}" },
                assetPath = "styles/$imageFile"
            )
        }
    }
    
    // Set first style as default if nothing is selected
    LaunchedEffect(styles) {
        if (selectedStyle == null && styles.isNotEmpty()) {
            onStyleSelected(styles.first().name)
        }
    }
    
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Choose style",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

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
                        .background(
                            color = Color.White, // All cards have white background
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) colorResource(R.color.app_blue) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onStyleSelected(style.name) }
                        .padding(12.dp), // Increased padding to prevent image cut-off
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Image with proper sizing - no background
                    Image(
                        painter = rememberAssetImagePainter(style.assetPath),
                        contentDescription = style.name,
                        modifier = Modifier
                            .size(80.dp) // Bigger image size
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit // Use Fit to show full image without cut-off
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
                            color = if (isSelected) colorResource(R.color.app_blue) else Color(0xFFEEEEEE),
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
                        color = if (isSelected) colorResource(R.color.app_blue) else Color.Black
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
                cursorBrush = SolidColor(colorResource(R.color.app_blue)),
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
