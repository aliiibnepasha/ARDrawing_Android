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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
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
import android.net.Uri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import com.example.ardrawing.ui.components.OpacityAndZoomControls

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    template: DrawingTemplate? = null,
    lesson: Lesson? = null,
    galleryImageUri: String? = null,
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
                .windowInsetsPadding(WindowInsets.systemBars)
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
                        color = colorResource(R.color.app_blue)
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
                    // Check for overlay bitmap (from text, text_to_image, or gallery)
                    val overlayBitmap = com.example.ardrawing.LaunchActivity.selectedOverlayBitmap
                    if (overlayBitmap != null && template == null && lesson == null && galleryImageUri == null) {
                        // Display overlay bitmap (text or generated image)
                        androidx.compose.foundation.Image(
                            bitmap = overlayBitmap.asImageBitmap(),
                            contentDescription = "Overlay",
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    rotationZ = imageRotation
                                }
                                .alpha(opacity)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (galleryImageUri != null) {
                        // Show gallery image
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(galleryImageUri))
                                .build(),
                            contentDescription = "Gallery Image",
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    rotationZ = imageRotation
                                }
                                .alpha(opacity)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (lesson != null && currentStepIndex < lesson.steps.size) {
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
                        .padding(bottom = if (isPanelVisible) 220.dp else 60.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Left Arrow Button
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(56.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable(enabled = currentStepIndex > 0) {
                                    if (currentStepIndex > 0) currentStepIndex--
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous",
                                tint = if (currentStepIndex > 0) Color.Black else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Central Step Card
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(56.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "step",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${currentStepIndex + 1}/$totalSteps",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right Arrow Button
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(56.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable(enabled = currentStepIndex < totalSteps - 1) {
                                    if (currentStepIndex < totalSteps - 1) currentStepIndex++
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next",
                                tint = if (currentStepIndex < totalSteps - 1) Color.Black else Color.Gray,
                                modifier = Modifier.size(24.dp)
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
                        OpacityAndZoomControls(
                            opacity = opacity,
                            onOpacityChange = { opacity = it },
                            imageScale = imageScale,
                            onScaleChange = { imageScale = it },
                            onGalleryClick = { /* TODO: Gallery */ },
                            isDarkTheme = true
                        )
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
