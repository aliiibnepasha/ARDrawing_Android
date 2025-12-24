package com.example.ardrawing.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ARDrawingScreen(
    template: DrawingTemplate,
    anchorBitmap: Bitmap?,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRetakeAnchor: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val cameraPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    
    var arSession: Session? by remember { mutableStateOf(null) }
    var arError by remember { mutableStateOf<String?>(null) }
    var isTracking by remember { mutableStateOf(false) }
    var showAnchorLostDialog by remember { mutableStateOf(false) }
    var isCheckingARCore by remember { mutableStateOf(false) }
    
    // Image overlay state
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    var opacity by remember { mutableStateOf(0.7f) }
    var isLocked by remember { mutableStateOf(false) }
    
    // Camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var camera: androidx.camera.core.Camera? by remember { mutableStateOf(null) }
    
    // Initialize ARCore Session and Augmented Image Database
    LaunchedEffect(anchorBitmap, cameraPermissionsState.allPermissionsGranted) {
        Log.d("ARDrawing", "=== ARCore Initialization Started ===")
        Log.d("ARDrawing", "Camera permissions granted: ${cameraPermissionsState.allPermissionsGranted}")
        Log.d("ARDrawing", "Anchor bitmap: ${if (anchorBitmap != null) "Present (${anchorBitmap.width}x${anchorBitmap.height})" else "Null"}")
        
        if (cameraPermissionsState.allPermissionsGranted && anchorBitmap != null) {
            isCheckingARCore = true
            Log.d("ARDrawing", "Starting ARCore availability check...")
            
            try {
                // Check ARCore availability first
                val arCoreApk = ArCoreApk.getInstance()
                Log.d("ARDrawing", "ARCoreApk instance obtained")
                
                val availability = arCoreApk.checkAvailability(context)
                Log.d("ARDrawing", "ARCore availability check result: $availability")
                Log.d("ARDrawing", "  - isSupported: ${availability.isSupported}")
                Log.d("ARDrawing", "  - isTransient: ${availability.isTransient}")
                Log.d("ARDrawing", "  - toString: ${availability.toString()}")
                
                when {
                    availability.isTransient -> {
                        Log.d("ARDrawing", "ARCore availability is transient - waiting...")
                        // ARCore is checking availability - wait a bit
                        kotlinx.coroutines.delay(500)
                        // Retry after delay
                        return@LaunchedEffect
                    }
                    availability.isSupported -> {
                        Log.d("ARDrawing", "ARCore is supported - checking installation status...")
                        // ARCore is supported, check if installation is needed
                        val installStatus = try {
                            arCoreApk.requestInstall(activity, true)
                        } catch (e: Exception) {
                            Log.e("ARDrawing", "Error requesting ARCore install: ${e.message}", e)
                            throw e
                        }
                        
                        Log.d("ARDrawing", "ARCore install status: $installStatus")
                        
                        when (installStatus) {
                            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                                Log.d("ARDrawing", "ARCore installation requested")
                                // Installation was requested - user needs to install from Play Store
                                // If device is incompatible, Play Store will show error
                                arError = "ARCore installation requested. If your device is incompatible, AR features won't be available. Please check Play Store compatibility."
                                isCheckingARCore = false
                                return@LaunchedEffect
                            }
                            ArCoreApk.InstallStatus.INSTALLED -> {
                                Log.d("ARDrawing", "ARCore is installed - proceeding with Session creation")
                                // ARCore is installed, proceed with Session creation
                            }
                            else -> {
                                Log.w("ARDrawing", "Unexpected install status: $installStatus")
                                arError = "ARCore installation status: $installStatus. Please check device compatibility."
                                isCheckingARCore = false
                                return@LaunchedEffect
                            }
                        }
                    }
                    else -> {
                        Log.e("ARDrawing", "ARCore is NOT supported on this device")
                        arError = "ARCore is not supported on this device"
                        isCheckingARCore = false
                        return@LaunchedEffect
                    }
                }
                
                // Create ARCore Session
                Log.d("ARDrawing", "Creating ARCore Session...")
                val session = try {
                    Session(context)
                } catch (e: Exception) {
                    Log.e("ARDrawing", "Error creating ARCore Session: ${e.message}", e)
                    throw e
                }
                Log.d("ARDrawing", "ARCore Session created successfully")
                
                val config = Config(session)
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                Log.d("ARDrawing", "ARCore Config created: planeFindingMode=DISABLED, updateMode=LATEST_CAMERA_IMAGE")
                
                // Create Augmented Image Database from captured bitmap
                try {
                    Log.d("ARDrawing", "Creating Augmented Image Database...")
                    val augmentedImageDatabase = AugmentedImageDatabase(session)
                    Log.d("ARDrawing", "Adding anchor image to database (${anchorBitmap.width}x${anchorBitmap.height})...")
                    augmentedImageDatabase.addImage("anchor_image", anchorBitmap)
                    config.augmentedImageDatabase = augmentedImageDatabase
                    Log.d("ARDrawing", "Augmented Image Database created and configured successfully")
                } catch (e: Exception) {
                    Log.e("ARDrawing", "Failed to create AR database: ${e.message}", e)
                    arError = "Failed to create AR database: ${e.message}"
                    isCheckingARCore = false
                    return@LaunchedEffect
                }
                
                Log.d("ARDrawing", "Configuring ARCore Session...")
                session.configure(config)
                Log.d("ARDrawing", "Resuming ARCore Session...")
                session.resume()
                arSession = session
                isCheckingARCore = false
                Log.d("ARDrawing", "=== ARCore Session initialized successfully ===")
            } catch (e: UnavailableArcoreNotInstalledException) {
                Log.e("ARDrawing", "ARCore not installed exception: ${e.message}", e)
                arError = "ARCore is not installed. Please install ARCore from Play Store."
                isCheckingARCore = false
            } catch (e: UnavailableDeviceNotCompatibleException) {
                Log.e("ARDrawing", "Device not compatible exception: ${e.message}", e)
                arError = "Device is not compatible with ARCore"
                isCheckingARCore = false
            } catch (e: UnavailableSdkTooOldException) {
                Log.e("ARDrawing", "SDK too old exception: ${e.message}", e)
                arError = "ARCore SDK is too old. Please update the app."
                isCheckingARCore = false
            } catch (e: UnavailableUserDeclinedInstallationException) {
                Log.e("ARDrawing", "User declined installation exception: ${e.message}", e)
                arError = "ARCore installation was declined. Please install ARCore to use this feature."
                isCheckingARCore = false
            } catch (e: Exception) {
                Log.e("ARDrawing", "Unexpected exception during ARCore initialization: ${e.message}", e)
                Log.e("ARDrawing", "Exception type: ${e.javaClass.name}")
                e.printStackTrace()
                arError = "Failed to initialize ARCore: ${e.message}"
                isCheckingARCore = false
            }
        } else {
            if (!cameraPermissionsState.allPermissionsGranted) {
                Log.d("ARDrawing", "Camera permissions not granted - requesting...")
                cameraPermissionsState.launchMultiplePermissionRequest()
            } else if (anchorBitmap == null) {
                Log.w("ARDrawing", "Anchor bitmap is null - cannot initialize AR")
            }
        }
    }
    
    // Monitor AR tracking state
    LaunchedEffect(arSession) {
        arSession?.let { session ->
            Log.d("ARDrawing", "Starting AR tracking monitor loop...")
            while (true) {
                try {
                    val frame = session.update()
                    val augmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
                    
                    Log.v("ARDrawing", "Frame updated - found ${augmentedImages.size} augmented images")
                    
                    var foundTracking = false
                    for (augmentedImage in augmentedImages) {
                        val trackingState = augmentedImage.trackingState
                        Log.v("ARDrawing", "AugmentedImage tracking state: $trackingState")
                        
                        when (trackingState) {
                            TrackingState.TRACKING -> {
                                foundTracking = true
                                if (!isTracking) {
                                    Log.d("ARDrawing", "✓ Anchor object detected - TRACKING started")
                                    isTracking = true
                                    showAnchorLostDialog = false
                                }
                            }
                            TrackingState.STOPPED, TrackingState.PAUSED -> {
                                if (isTracking) {
                                    Log.d("ARDrawing", "✗ Anchor object lost - TRACKING stopped")
                                    isTracking = false
                                    showAnchorLostDialog = true
                                }
                            }
                            else -> {
                                Log.v("ARDrawing", "AugmentedImage in state: $trackingState")
                            }
                        }
                    }
                    
                    // If no images are tracking and we were tracking before, show dialog
                    if (!foundTracking && isTracking) {
                        Log.d("ARDrawing", "No tracking images found - stopping tracking")
                        isTracking = false
                        showAnchorLostDialog = true
                    }
                    
                    kotlinx.coroutines.delay(100) // Check every 100ms
                } catch (e: Exception) {
                    Log.e("ARDrawing", "Error in AR tracking loop: ${e.message}", e)
                    e.printStackTrace()
                    // If session update fails, stop tracking
                    if (isTracking) {
                        Log.d("ARDrawing", "Session update failed - stopping tracking")
                        isTracking = false
                        showAnchorLostDialog = true
                    }
                }
            }
        } ?: run {
            Log.w("ARDrawing", "AR Session is null - tracking monitor not started")
        }
    }
    
    DisposableEffect(arSession) {
        onDispose {
            arSession?.pause()
            arSession?.close()
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Draw with AR",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            when {
                anchorBitmap == null -> {
                    // No anchor captured yet
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Anchor Object",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please capture an anchor object first",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                !cameraPermissionsState.allPermissionsGranted -> {
                    PermissionRequestView(
                        onRequestPermission = {
                            cameraPermissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
                isCheckingARCore -> {
                    // Show loading while checking ARCore
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Checking ARCore availability...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                arError != null -> {
                    ErrorView(
                        message = arError ?: "Unknown error",
                        onRetry = {
                            arError = null
                            isCheckingARCore = false
                        }
                    )
                }
                else -> {
                    // Camera Preview
                    if (cameraPermissionsState.allPermissionsGranted) {
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
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // AR Image Overlay - Only show when tracking
                        if (isTracking) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAssetImagePainter(template.imageAssetPath),
                                    contentDescription = "AR Overlay",
                                    modifier = Modifier
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
                            }
                        }
                    }
                    
                    // Instructions overlay - Show when not tracking
                    if (!isTracking && !showAnchorLostDialog) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Point camera at anchor object",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Move device slowly to detect",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    // Bottom Controls - Only show when tracking
                    if (isTracking) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Opacity Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            // Lock Button
                            Button(
                                onClick = { isLocked = !isLocked },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLocked) Color(0xFF2196F3) else Color.Gray
                                )
                            ) {
                                Text(
                                    text = if (isLocked) "Unlock" else "Lock",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Anchor Lost Dialog - Show when tracking is lost
            if (showAnchorLostDialog) {
                AnchorLostDialog(
                    onDismiss = { showAnchorLostDialog = false },
                    onRetakePhoto = {
                        showAnchorLostDialog = false
                        onRetakeAnchor()
                    }
                )
            }
        }
    }
}

@Composable
fun AnchorLostDialog(
    onDismiss: () -> Unit,
    onRetakePhoto: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF2C2C2C),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Anchor Object Not Detected",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "We couldn't detect your anchor. Try pointing your camera at it again or take a new photo with a more unique object.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = onRetakePhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Retake photo",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text("Grant Permission", color = Color.White)
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    val context = LocalContext.current
    val isIncompatible = message.contains("incompatible", ignoreCase = true) ||
                         message.contains("not available on this device", ignoreCase = true)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isIncompatible) "Device Not Compatible" else "AR Error",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isIncompatible) {
                "Your device is not compatible with ARCore.\n\n" +
                "AR features require:\n" +
                "• Android 7.0+ (API 24+)\n" +
                "• ARCore-compatible hardware\n" +
                "• Latest Google Play Services\n\n" +
                "You can still use:\n" +
                "✓ Camera Sketch\n" +
                "✓ Paper Trace\n" +
                "✓ Coloring Features"
            } else {
                message
            },
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!isIncompatible && (message.contains("not installed", ignoreCase = true) || 
            message.contains("installation", ignoreCase = true))) {
            // Show install button for ARCore installation errors (only if compatible)
            Button(
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("market://details?id=com.google.ar.core")
                            setPackage("com.android.vending")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // If Play Store is not available, open browser
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.ar.core")
                        }
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("Try Install ARCore", color = Color.White)
            }
        }
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text(if (isIncompatible) "Go Back" else "Retry", color = Color.White)
        }
    }
}
