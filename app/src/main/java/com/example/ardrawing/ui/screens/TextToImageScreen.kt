package com.example.ardrawing.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.data.local.database.AppDatabase
import com.example.ardrawing.data.repository.FavoriteRepository
import com.example.ardrawing.data.repository.TextToImageRepository
import com.example.ardrawing.ui.components.WaterWaveBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TextToImageScreen(
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
    
    // State
    var promptText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedImageVisible by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var generatedImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Check if current prompt is favorite
    LaunchedEffect(promptText, generatedImageVisible) {
        if (generatedImageVisible && promptText.isNotEmpty()) {
            isFavorite = favoriteRepository.isFavorite(promptText)
        }
    }

    // Handle Back Press
    BackHandler {
        if (generatedImageVisible) {
            generatedImageVisible = false
            errorMessage = null
        } else {
            onBackClick()
        }
    }

    // Generate image when user clicks Generate button
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

    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()
        
        // 2. Foreground Content
    Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
        topBar = {
            TopHeader(
                title = "Text To Image",
                onBackClick = {
                    if (generatedImageVisible) {
                        generatedImageVisible = false
                    } else {
                        onBackClick()
                    }
                },
                showDone = generatedImageVisible,
                onDoneClick = { /* Done Action */ }
            )
        },
        bottomBar = {
            BottomButton(
                text = if (generatedImageVisible) "Use To Draw" else "Generate",
                enabled = if (generatedImageVisible) true else promptText.isNotEmpty(),
                isGenerating = isGenerating,
                onClick = {
                    if (generatedImageVisible) {
                        generatedImageBitmap?.let { bitmap ->
                            onUseToDraw(bitmap)
                        }
                    } else {
                        generateImage()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            if (!generatedImageVisible) {
                // INPUT STATE
                Column {
                    InputState(
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
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            } else {
                // RESULT STATE
                ResultState(
                    prompt = promptText,
                    isFavorite = isFavorite,
                    imageBitmap = generatedImageBitmap,
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
                                                imagePath = imagePath
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

@Composable
private fun TopHeader(
    title: String,
    onBackClick: () -> Unit,
    showDone: Boolean,
    onDoneClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Image(
            painter = painterResource(R.drawable.back_arrow_ic),
            contentDescription = "Back",
            modifier = Modifier
                .size(32.dp)
                .clickable { onBackClick() }
            )
        
        // Centered Title
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        
        // Right Side (Spacer or Done)
        if (showDone) {
            Text(
                text = "Done",
                color = colorResource(R.color.app_blue), // Blue
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                modifier = Modifier.clickable { onDoneClick() }
            )
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
private fun BottomButton(
    text: String,
    enabled: Boolean,
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isGenerating,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.app_blue),
            disabledContainerColor = colorResource(R.color.app_blue).copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                color = Color.White, 
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun InputState(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = "Describe the image you want to create",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1C)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Text Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp, max = 220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9F9F9)) 
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "Example: Girl studying",
                    color = Color(0xFF9E9E9E), 
                    fontSize = 15.sp
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = Color.Black
                ),
                cursorBrush = SolidColor(colorResource(R.color.app_blue)),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResultState(
    prompt: String,
    isFavorite: Boolean = false,
    imageBitmap: android.graphics.Bitmap? = null,
    onFavoriteClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = prompt,
            fontSize = 16.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Image Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
        ) {
            // Generated Image or Mock Image
            if (imageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.create_with_ai), 
                    contentDescription = null,
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
                // Heart Icon (Top Left) - Favorite Button
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
