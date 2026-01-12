package com.example.ardrawing.ui.screens

import android.Manifest
import android.R.attr.tint
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    template: DrawingTemplate? = null,
    lesson: Lesson? = null,
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

    // Lesson Step State
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val totalSteps = lesson?.steps?.size ?: 0

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
                    .padding(bottom = if (isPanelVisible) 200.dp else 80.dp), // Increased padding for steps
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            translationX = imageOffset.x
                            translationY = imageOffset.y
                            // rotationZ removed from here
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
                    if (lesson != null && currentStepIndex < lesson.steps.size) {
                         val step = lesson.steps[currentStepIndex]
                         AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/${step.imageAssetPath}")
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = "Step ${step.stepNumber}",
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    rotationZ = imageRotation
                                }
                                .alpha(opacity)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (template != null) {
                        Image(
                            painter = rememberAssetImagePainter(template.imageAssetPath),
                            contentDescription = "Sketch",
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    rotationZ = imageRotation
                                }
                                .alpha(opacity)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Overlay Controls
                    if (!isLocked) {
                        // Top Right: Lock
                        Image(
                            painter = painterResource(R.drawable.lock),
                            contentDescription = "Lock",
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
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { imageRotation -= 90f }
                            )
                            
                             // Rotate Right (flip2)
                            Image(
                                painter = painterResource(R.drawable.flip2),
                                contentDescription = "Rotate Right",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { imageRotation += 90f }
                            )
                        }

                        // Bottom Right: Resize/Reset (full_screen)
                        Image(
                            painter = painterResource(R.drawable.full_screen),
                            contentDescription = "Reset",
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
                         // Locked State Icon (Top Right) (lock)
                         Image(
                            painter = painterResource(R.drawable.lock),
                            contentDescription = "Unlock",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                                .size(32.dp)
                                .clickable { isLocked = !isLocked }
                        )
                    }
                }
            }
            
            /* ================= STEP NAVIGATION (LESSONS ONLY) ================= */
            if (lesson != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isPanelVisible) 180.dp else 40.dp) // Auto-adjusts
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentStepIndex > 0) currentStepIndex-- },
                            enabled = currentStepIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous",
                                tint = if (currentStepIndex > 0) Color.Black else Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text(
                                "step",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${currentStepIndex + 1}/$totalSteps",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        IconButton(
                            onClick = { if (currentStepIndex < totalSteps - 1) currentStepIndex++ },
                            enabled = currentStepIndex < totalSteps - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next",
                                tint = if (currentStepIndex < totalSteps - 1) Color.Black else Color.Gray
                            )
                        }
                    }
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
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Text(
                            text = "Opacity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.mask),
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
                                    painter = painterResource(R.drawable.galllery),
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
                                selected = imageScale == 0.5f,
                                isDarkTheme = true
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ZoomChip(
                                text = "1.0x", 
                                onClick = { imageScale = 1.0f }, 
                                selected = imageScale == 1.0f,
                                isDarkTheme = true
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ZoomChip(
                                text = "2.0x", 
                                onClick = { imageScale = 2.0f }, 
                                selected = imageScale == 2.0f,
                                isDarkTheme = true
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
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (selected) {
        Color(0xFF4285F4)
    } else {
        Color.White
    }
    
    val textColor = if (selected) {
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
