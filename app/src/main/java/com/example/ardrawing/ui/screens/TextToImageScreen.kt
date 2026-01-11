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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import kotlinx.coroutines.delay

@Composable
fun TextToImageScreen(
    onBackClick: () -> Unit,
    onUseToDraw: () -> Unit
) {
    // State
    var promptText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedImageVisible by remember { mutableStateOf(false) }

    // Handle Back Press
    BackHandler {
        if (generatedImageVisible) {
            generatedImageVisible = false
        } else {
            onBackClick()
        }
    }

    // Fake generation delay logic
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            delay(2000) // Mock 2s generation
            isGenerating = false
            generatedImageVisible = true
        }
    }

    Scaffold(
        containerColor = Color.White,
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
                        onUseToDraw()
                    } else {
                        isGenerating = true
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
                InputState(
                    text = promptText,
                    onTextChange = { promptText = it }
                )
            } else {
                // RESULT STATE
                ResultState(prompt = promptText)
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF4285F4)) // Blue circle as per design
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
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
        Box(
            modifier = Modifier.size(40.dp), 
            contentAlignment = Alignment.CenterEnd
        ) {
            if (showDone) {
                Text(
                    text = "Done",
                    color = Color(0xFF4285F4), // Blue
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onDoneClick() }
                )
            }
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
            containerColor = Color(0xFF4285F4),
            disabledContainerColor = Color(0xFF4285F4).copy(alpha = 0.5f)
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
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9F9F9)) 
                .padding(horizontal = 16.dp),
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
                cursorBrush = SolidColor(Color(0xFF4285F4)),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResultState(prompt: String) {
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
            // Mock Image 
            Image(
                painter = painterResource(R.drawable.create_with_ai), 
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentScale = ContentScale.Fit
            )
            
            // Icons Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Heart Icon (Top Left)
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = Color(0xFF4285F4),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopStart)
                )
                
                // Refresh Icon (Top Right)
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Regenerate",
                    tint = Color(0xFF4285F4),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}
