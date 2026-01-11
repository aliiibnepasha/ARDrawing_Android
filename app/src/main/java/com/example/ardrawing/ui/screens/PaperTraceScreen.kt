package com.example.ardrawing.ui.screens

import android.R.attr.tint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.utils.rememberAssetImagePainter

@Composable
fun PaperTraceScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit
) {

    /* ---------------- BACK HANDLER ---------------- */
    BackHandler { onBackClick() }

    /* ---------------- STATES ---------------- */
    var imageScale by remember { mutableFloatStateOf(1f) }
    var imageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var imageRotation by remember { mutableFloatStateOf(0f) }
    
    var opacity by remember { mutableFloatStateOf(0.5f) }
    var isLocked by remember { mutableStateOf(false) }

    // Bottom Panel State
    var isPanelVisible by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.statusBars // Handle Status Bar
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            /* ================= OVERLAY IMAGE ================= */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isPanelVisible) 180.dp else 60.dp), // Adjust padding
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            translationX = imageOffset.x
                            translationY = imageOffset.y
                        }
                        .pointerInput(isLocked) {
                            if (!isLocked) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    imageScale *= zoom
                                    imageOffset += pan
                                }
                            }
                        }
                ) {
                    // Actual Image
                    Image(
                        painter = rememberAssetImagePainter(template.imageAssetPath),
                        contentDescription = "Sketch",
                        modifier = Modifier
                            .size(300.dp)
                            .graphicsLayer {
                                rotationZ = imageRotation // Applied ONLY to Image
                            }
                            .alpha(opacity)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    // Overlay Controls
                    if (!isLocked) {
                        // Top Right: Lock
                        Image(
                            painter = painterResource(R.drawable.lock),
                            contentDescription = "Lock",
                            colorFilter = ColorFilter.tint(Color.Black),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                                .size(32.dp)
                                .clickable { isLocked = !isLocked }
                        )

                        // Bottom Left: Rotate Controls (Left & Right)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-12).dp, y = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rotate Left (flip1)
                            Image(
                                painter = painterResource(R.drawable.flip1),
                                contentDescription = "Rotate Left",
                                colorFilter = ColorFilter.tint(Color.Black),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { imageRotation -= 90f }
                            )
                            
                             // Rotate Right (flip2)
                            Image(
                                painter = painterResource(R.drawable.flip2),
                                contentDescription = "Rotate Right",
                                colorFilter = ColorFilter.tint(Color.Black),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { imageRotation += 90f }
                            )
                        }

                        // Bottom Right: Resize/Reset (full_screen)
                        Image(
                            painter = painterResource(R.drawable.full_screen),
                            contentDescription = "Reset",
                            colorFilter = ColorFilter.tint(Color.Black),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 12.dp, y = 12.dp)
                                .size(32.dp)
                                .clickable { 
                                    imageScale = 1f 
                                    imageOffset = androidx.compose.ui.geometry.Offset.Zero
                                    imageRotation = 0f
                                }
                        )
                    } else {
                         // Locked State Icon (lock)
                         Image(
                            painter = painterResource(R.drawable.lock),
                            contentDescription = "Unlock",
                             colorFilter = ColorFilter.tint(Color.Black),
                             modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                                .size(32.dp)
                                .clickable { isLocked = !isLocked }
                        )
                    }
                }
            }

            /* ================= TOP BAR ================= */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Image(
                    painter = painterResource(R.drawable.back_arrow_ic),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onBackClick)
                )

                Text(
                    text = "Screen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                TextButton(onClick = onBackClick) {
                    Text(
                        text = "Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                }
            }

            /* ================= BOTTOM CONTROLS ================= */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Control Panel (Bottom Layer)
                if (isPanelVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Text(
                            text = "Opacity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.mask),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Slider(
                                value = opacity,
                                onValueChange = { opacity = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4285F4),
                                    activeTrackColor = Color(0xFF4285F4),
                                    inactiveTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                            
                            Text(
                                text = "${(opacity * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Gallery Icon
                             IconButton(
                                onClick = { /* TODO: Gallery */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                 Icon(
                                    painter = painterResource(R.drawable.galllery),
                                    contentDescription = "Gallery",
                                    tint = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Zoom Presets (Inverse colors for light theme)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ZoomChip(
                                text = "0.5x", 
                                onClick = { imageScale = 0.5f }, 
                                selected = imageScale == 0.5f,
                                isDarkTheme = false
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ZoomChip(
                                text = "1.0x", 
                                onClick = { imageScale = 1.0f }, 
                                selected = imageScale == 1.0f,
                                isDarkTheme = false
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ZoomChip(
                                text = "2.0x", 
                                onClick = { imageScale = 2.0f }, 
                                selected = imageScale == 2.0f,
                                isDarkTheme = false
                            )
                        }
                    }
                }

                // Toggle Button (Top Layer)
                Image(
                    painter = painterResource(if (isPanelVisible) R.drawable.arrow_below else R.drawable.arrow_up),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .align(if (isPanelVisible) Alignment.TopCenter else Alignment.BottomCenter)
                        .offset(y = if (isPanelVisible) (-18).dp else (-12).dp)
                        .size(36.dp)
                        .clickable { isPanelVisible = !isPanelVisible }
                )
            }
        }
    }
}


@Composable
private fun ZoomChip(
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (selected) {
        Color(0xFF4285F4)
    } else {
        if (isDarkTheme) Color.White else Color.Black
    }
    
    val textColor = if (selected) {
        Color.White
    } else {
        if (isDarkTheme) Color.Black else Color.White
    }

    Box(
        modifier = Modifier
            .height(28.dp)
            .width(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
