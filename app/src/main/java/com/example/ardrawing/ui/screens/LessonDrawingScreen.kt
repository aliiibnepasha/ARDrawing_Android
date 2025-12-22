package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.ardrawing.R
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.data.model.LessonStep
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDrawingScreen(
    lesson: Lesson,
    onBackClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    var currentStepIndex by remember { mutableStateOf(0) }
    var opacity by remember { mutableStateOf(1f) }
    var isLocked by remember { mutableStateOf(false) }
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    
    val currentStep = lesson.steps.getOrNull(currentStepIndex) ?: lesson.steps.first()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onCaptureClick,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray.copy(alpha = 0.3f),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Capture",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Drawing Canvas
            Image(
                painter = rememberAssetImagePainter(currentStep.imageAssetPath),
                contentDescription = currentStep.title,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .alpha(opacity)
                    .graphicsLayer {
                        scaleX = imageScale
                        scaleY = imageScale
                        translationX = imageOffsetX
                        translationY = imageOffsetY
                    }
                    .pointerInput(isLocked) {
                        if (!isLocked) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                imageScale *= zoom
                                imageOffsetX += pan.x
                                imageOffsetY += pan.y
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
            
            // Bottom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Step Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    lesson.steps.forEachIndexed { index, step ->
                        StepButton(
                            step = step,
                            isSelected = index == currentStepIndex,
                            onClick = { currentStepIndex = index }
                        )
                    }
                }
                
                // Bottom Sheet Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    // Slider Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2196F3),
                                activeTrackColor = Color(0xFF2196F3),
                                inactiveTrackColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = "${(opacity * 100).toInt()}%",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    // Action Icons Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Eye Icon (Visibility)
                        IconButton(
                            onClick = { /* TODO: Toggle visibility */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Visibility",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Lock Icon
                        IconButton(
                            onClick = { isLocked = !isLocked },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.lock),
                                contentDescription = if (isLocked) "Unlock" else "Lock",
                                tint = if (isLocked) Color.Black else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Checkmark Icon
                        IconButton(
                            onClick = { /* TODO: Complete step */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepButton(
    step: LessonStep,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAssetImagePainter(step.imageAssetPath),
                contentDescription = step.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Text(
                text = step.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

