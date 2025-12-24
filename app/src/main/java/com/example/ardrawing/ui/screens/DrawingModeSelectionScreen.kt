package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter

enum class DrawingMode(
    val title: String,
    val description: String
) {
    DRAW_WITH_CAMERA(
        title = "Draw with camera",
        description = "Use a tripod, a glass, or a stack of books to hold your phone in place while you draw."
    ),
    DRAW_WITH_AR(
        title = "Draw with screen",
        description = "Put a paper over your phone's screen and follow the lines to trace your image with ease."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingModeSelectionScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit,
    onContinueClick: (DrawingMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf<DrawingMode?>(DrawingMode.DRAW_WITH_CAMERA) }
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "", // Empty title - we'll show it in content
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            // Continue Button - Fixed at bottom using Scaffold's bottomBar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedMode?.let { onContinueClick(it) }
                        },
                        enabled = selectedMode != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63), // Pink
                            disabledContainerColor = Color(0xFFCCCCCC)
                        )
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(paddingValues) // Avoid overlap with topBar
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title - Only once
            Text(
                text = "Select the drawing mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Drawing Mode Cards - HORIZONTAL LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp), // Fixed height for cards
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrawingMode.values().forEach { mode ->
                    DrawingModeCard(
                        mode = mode,
                        isSelected = selectedMode == mode,
                        onClick = {
                            selectedMode = mode
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingModeCard(
    mode: DrawingMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFFE91E63) else Color(0xFFCCCCCC),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Radio Button Indicator (Top Left) - Positioned absolutely above everything
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.White, CircleShape) // White background to ensure visibility
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xFFCCCCCC),
                                shape = CircleShape
                            )
                            .background(Color.White, CircleShape)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top =16.dp), // Add top padding to avoid radio button overlap
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode Image Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = mode.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Description
                Text(
                    text = mode.description,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
