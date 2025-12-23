package com.example.ardrawing.ui.screens

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Camera permission state
    val cameraPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    // Image overlay state
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    var opacity by remember { mutableStateOf(1f) }

    // Flashlight state
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isVerticallyFlipped by remember { mutableStateOf(false) }
    var isHorizontallyFlipped by remember { mutableStateOf(false) }

    // Camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var camera: androidx.camera.core.Camera? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionsState.allPermissionsGranted) {
            cameraPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            if (!isFullscreen && cameraPermissionsState.allPermissionsGranted) {
                AppTopBar(
                    title = "",
                    showBackButton = true,
                    onBackClick = onBackClick
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionsState.allPermissionsGranted) {
                // Camera Preview
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        scope.launch {
                            val cameraProvider = cameraProviderFuture.get()
                            val previewUseCase = Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val cameraInstance = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    previewUseCase
                                )
                                preview = previewUseCase
                                camera = cameraInstance
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        previewView
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.2f)),
                contentAlignment = Alignment.TopCenter
                ) {
                    // Overlay Image
                    Image(
                        painter = rememberAssetImagePainter(template.imageAssetPath),
                        contentDescription = "Overlay",
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
                                        thumbColor = Color(0xFF4CAF50),
                                        activeTrackColor = Color(0xFF4CAF50),
                                        inactiveTrackColor = Color(0xFF4CAF50).copy(alpha = 0.3f)
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
                                        tint = Color.Black,
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

                                // Fullscreen Icon
                                IconButton(
                                    onClick = { isFullscreen = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.full_screen),
                                        contentDescription = "Fullscreen",
                                        tint = Color.Black,
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
            } else {
                // Permission Denied State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera Permission Required",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please allow camera permission to use AR Drawing feature",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            cameraPermissionsState.launchMultiplePermissionRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Grant Permission", color = Color.White)
                    }
                }
            }
        }
    }
}

