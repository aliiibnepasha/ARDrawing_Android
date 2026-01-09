package com.example.ardrawing.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit
) {

    /* ---------------- BACK HANDLER ---------------- */
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    /* ---------------- STATES ---------------- */
    var imageScale by remember { mutableFloatStateOf(1f) }
    var imageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var imageRotation by remember { mutableFloatStateOf(0f) }
    
    var opacity by remember { mutableFloatStateOf(0.5f) }
    var isLocked by remember { mutableStateOf(false) }

    // Bottom Panel State
    var isPanelVisible by remember { mutableStateOf(true) }

    // Camera setup
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionsState.allPermissionsGranted) {
            cameraPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        /* ================= CAMERA PREVIEW ================= */
        if (cameraPermissionsState.allPermissionsGranted) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    scope.launch {
                        val provider = cameraProviderFuture.get()
                        provider.unbindAll()

                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview
                        )
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        /* ================= SAFE AREA CONTAINER ================= */
        // We use a box with systemBars padding for the UI overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {

            /* ================= TOP BAR ================= */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Image(
                    painter = painterResource(R.drawable.back_arrow_ic),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onBackClick)
                )

                // Title
                Text(
                    text = "Camera",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Done Button
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                }
            }

            /* ================= OVERLAY IMAGE ================= */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isPanelVisible) 180.dp else 60.dp), // Adjust padding based on panel visibility
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            translationX = imageOffset.x
                            translationY = imageOffset.y
                            rotationZ = imageRotation
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
                            .alpha(opacity)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    // Overlay Controls
                    if (!isLocked) {
                        // Top Right: Lock
                        IconButton(
                            onClick = { isLocked = !isLocked },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Bottom Left: Rotate
                        IconButton(
                            onClick = { imageRotation -= 90f },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-12).dp, y = 12.dp)
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rotate",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp).graphicsLayer { rotationY = 180f }
                            )
                        }

                        // Bottom Right: Resize/Reset
                        IconButton(
                            onClick = { 
                                imageScale = 1f 
                                imageOffset = androidx.compose.ui.geometry.Offset.Zero
                                imageRotation = 0f
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 12.dp, y = 12.dp)
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        ) {
                             // Use a resize/expand icon here. Using standard placeholder for now.
                             Icon(
                                painter = painterResource(R.drawable.full_screen), // Assuming this exists or using placeholder
                                contentDescription = "Reset",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                         // Locked State Icon (Top Right)
                         IconButton(
                            onClick = { isLocked = !isLocked },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                                .size(32.dp)
                                .background(Color(0xFF4285F4), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Unlock",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            /* ================= BOTTOM CONTROLS ================= */
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toggle Button (Floating)
                Image(
                    painter = painterResource(if (isPanelVisible) R.drawable.arrow_below else R.drawable.arrow_up),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(36.dp)
                        .clickable(onClick = { isPanelVisible = !isPanelVisible })
                )

                // Control Panel
                if (isPanelVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(top = 16.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Text(
                            text = "Opacity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.camera_ic),
                                contentDescription = null,
                                tint = Color.White,
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
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            
                            Text(
                                text = "${(opacity * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Gallery Icon
                             IconButton(
                                onClick = { /* TODO: Gallery */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                 Icon(
                                    painter = painterResource(R.drawable.camera_ic), // Placeholder
                                    contentDescription = "Gallery",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Zoom Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ZoomChip(
                                text = "0.5x", 
                                onClick = { imageScale = 0.5f }, 
                                isSelected = imageScale == 0.5f,
                                isDarkTheme = true
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ZoomChip(
                                text = "1.0x", 
                                onClick = { imageScale = 1.0f }, 
                                isSelected = imageScale == 1.0f,
                                isDarkTheme = true
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ZoomChip(
                                text = "2.0x", 
                                onClick = { imageScale = 2.0f }, 
                                isSelected = imageScale == 2.0f,
                                isDarkTheme = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomChip(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF4285F4)
    } else {
        Color.White
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        Color.Black
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
