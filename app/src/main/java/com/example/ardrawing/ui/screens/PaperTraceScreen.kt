package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperTraceScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    // Image overlay state
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    var opacity by remember { mutableStateOf(1f) }
    
    // Lock state
    var isLocked by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isVerticallyFlipped by remember { mutableStateOf(false) }
    var isHorizontallyFlipped by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            if (!isFullscreen) {
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // White Paper Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Overlay Image
                Image(
                    painter = rememberAssetImagePainter(template.imageAssetPath),
                    contentDescription = "Sketch",
                    modifier = Modifier
                        .alpha(opacity)
                        .graphicsLayer {
                            scaleX = if (isHorizontallyFlipped) -imageScale else imageScale
                            scaleY = if (isVerticallyFlipped) -imageScale else imageScale
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
            }

            // Bottom Sheet Controls (hide in fullscreen)
            if (!isFullscreen) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Bottom Sheet Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        // Slider Row (Full Width)
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

                        // Action Icons Row (4 icons: Flip Vertical, Flip Horizontal, Lock, Fullscreen)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vertical Flip Icon (Top ↔ Bottom)
                            IconButton(
                                onClick = { isVerticallyFlipped = !isVerticallyFlipped },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.flip_reverse),
                                    contentDescription = "Vertical Flip",
                                    tint = if (isVerticallyFlipped) Color(0xFF2196F3) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Horizontal Flip Icon (Left ↔ Right)
                            IconButton(
                                onClick = { isHorizontallyFlipped = !isHorizontallyFlipped },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.flip_straight),
                                    contentDescription = "Horizontal Flip",
                                    tint = if (isHorizontallyFlipped) Color(0xFF2196F3) else Color.Gray,
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
                                    tint = if (isLocked) Color(0xFF2196F3) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Fullscreen Icon
                            IconButton(
                                onClick = { isFullscreen = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.full_screen),
                                    contentDescription = "Fullscreen",
                                    tint = if (isFullscreen) Color(0xFF2196F3) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fullscreen Exit Button (only visible in fullscreen mode, bottom right)
            if (isFullscreen) {
                IconButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.full_screen),
                        contentDescription = "Exit Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

