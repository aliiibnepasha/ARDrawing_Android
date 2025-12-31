package com.example.ardrawing.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.View
import androidx.activity.compose.LocalActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
    Log.d("ARDrawing", "=== ARDrawingScreen ENTRY ===")
    Log.d("ARDrawing", "Template: ${template.name}, Image: ${template.imageAssetPath}")
    Log.d("ARDrawing", "Anchor bitmap provided: ${anchorBitmap != null}, Size: ${anchorBitmap?.let { "${it.width}x${it.height}" } ?: "null"}")
//    Log.d("ARDrawing", "Debug mode: $debugMode")

    val context = LocalContext.current
    Log.d("ARDrawing", "Context obtained: ${context.javaClass.simpleName}")

    val activity = LocalActivity.current
    Log.d("ARDrawing", "Activity obtained: ${activity?.javaClass?.simpleName ?: "null"}")

    val lifecycleOwner = LocalLifecycleOwner.current
    Log.d("ARDrawing", "Lifecycle owner obtained: ${lifecycleOwner.javaClass.simpleName}")

    val scope = rememberCoroutineScope()
    Log.d("ARDrawing", "Coroutine scope created: ${scope.javaClass.simpleName}")
    
    val cameraPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    Log.d("ARDrawing", "Camera permissions state initialized")

    var arSession: Session? by remember { mutableStateOf(null) }
    Log.d("ARDrawing", "AR session state initialized: null")

    var arError by remember { mutableStateOf<String?>(null) }
    Log.d("ARDrawing", "AR error state initialized: null")

    var isTracking by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Tracking state initialized: false")

    var showAnchorLostDialog by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Anchor lost dialog state initialized: false")

    var isCheckingARCore by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "ARCore checking state initialized: false")

    var installRequested by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Install requested state initialized: false")

    var isSessionDisposed by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Session disposed state initialized: false")

    var planeAnchor: Anchor? by remember { mutableStateOf(null) }
    Log.d("ARDrawing", "Plane anchor state initialized: null")

    var isPlaneDetected by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Plane detected state initialized: false")

    // Image tracking state (when anchorBitmap is provided)
    var isImageTrackingMode by remember { mutableStateOf(anchorBitmap != null) }
    var isImageTracked by remember { mutableStateOf(false) }
    var trackedImageName by remember { mutableStateOf("--") }
    var trackedImageSize by remember { mutableStateOf("--") }
    var trackedImageCoordinates by remember { mutableStateOf("--") }
    var trackedImageStatus by remember { mutableStateOf("Searching...") }
    Log.d("ARDrawing", "Image tracking mode: $isImageTrackingMode")

    var showPlaceAnchorHint by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Place anchor hint state initialized: false")

    var isSessionResumed by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Session resumed state initialized: false")

    var isDisplayGeometrySet by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Display geometry set state initialized: false")

    var arFrame: Frame? by remember { mutableStateOf(null) }
    Log.d("ARDrawing", "AR frame state initialized: null")
    
    // Safe ARCore state flags (from template)
    var sessionReady by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Session ready state initialized: false")

    var textureSet by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Texture set state initialized: false")

    // Session creation retry tracking
    var sessionCreationAttempts by remember { mutableStateOf(0) }
    val maxSessionCreationAttempts = 3
    Log.d("ARDrawing", "Session creation attempts initialized: 0, max: $maxSessionCreationAttempts")

    // Track last frame update time to throttle AR session updates
    var lastFrameUpdateTime by remember { mutableStateOf(0L) }
    Log.d("ARDrawing", "Last frame update time initialized: 0")

    // Track GLSurfaceView creation time for timeout detection
    var glSurfaceViewCreationTime by remember { mutableStateOf(0L) }
    Log.d("ARDrawing", "GL surface view creation time initialized: 0")

    // Performance monitoring
    var frameCount by remember { mutableStateOf(0) }
    var lastFpsUpdateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentFps by remember { mutableStateOf(0f) }
    Log.d("ARDrawing", "Performance monitoring initialized - frameCount: 0, fps: 0.0")

    // Auto-placement timeout tracking
    var autoPlacementStartTime by remember { mutableStateOf(0L) }
    var showManualFallback by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Auto-placement tracking initialized - startTime: 0, showManualFallback: false")

    // ARCore features state
    var instantPlacementEnabled by remember { mutableStateOf(true) }
    var depthEnabled by remember { mutableStateOf(true) }
    var showPointCloud by remember { mutableStateOf(false) }
    var pointCloud by remember { mutableStateOf<List<com.google.ar.core.PointCloud>?>(null) }
    Log.d("ARDrawing", "ARCore features initialized - instantPlacement: $instantPlacementEnabled, depth: $depthEnabled, pointCloud: $showPointCloud")

    // Debug mode - set to true to see detailed logs and performance metrics
    val debugMode = false // Set to true for debugging, false for production


    // Camera provider for preview
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    Log.d("ARDrawing", "Camera provider future initialized")

    // Image overlay state with bounds
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    var opacity by remember { mutableStateOf(0.7f) }
    var isLocked by remember { mutableStateOf(false) }
    Log.d("ARDrawing", "Image overlay state initialized - scale: $imageScale, offset: ($imageOffsetX, $imageOffsetY), opacity: $opacity, locked: $isLocked")

    // Transform bounds to prevent crashes
    val minScale = 0.1f
    val maxScale = 5.0f
    val maxOffset = 2000f // Reasonable max offset to prevent excessive memory usage
    Log.d("ARDrawing", "Transform bounds initialized - minScale: $minScale, maxScale: $maxScale, maxOffset: $maxOffset")
    
    // Helper function to preprocess image for ARCore (ensure minimum quality)
    fun preprocessImageForARCore(bitmap: Bitmap): Bitmap {
        Log.d("ARDrawing", "=== preprocessImageForARCore ENTRY ===")
        Log.d("ARDrawing", "Input bitmap size: ${bitmap.width}x${bitmap.height}")

        val minDimension = 300 // ARCore minimum recommended size
        val maxDimension = 1920 // Reasonable maximum to avoid memory issues
        Log.d("ARDrawing", "Target dimensions - min: $minDimension, max: $maxDimension")

        var processedBitmap = bitmap

        // Check if image is too small
        val minSize = minOf(processedBitmap.width, processedBitmap.height)
        val maxSize = maxOf(processedBitmap.width, processedBitmap.height)
        Log.d("ARDrawing", "Current dimensions - minSize: $minSize, maxSize: $maxSize")

        if (minSize < minDimension) {
            Log.d("ARDrawing", "Image too small ($minSize < $minDimension) - scaling up...")
            val scale = minDimension.toFloat() / minSize.toFloat()
            val newWidth = (processedBitmap.width * scale).toInt().coerceAtMost(maxDimension)
            val newHeight = (processedBitmap.height * scale).toInt().coerceAtMost(maxDimension)

            Log.d("ARDrawing", "Scale factor: $scale, new dimensions: ${newWidth}x${newHeight}")
            processedBitmap = Bitmap.createScaledBitmap(
                processedBitmap,
                newWidth,
                newHeight,
                true
            )
            Log.d("ARDrawing", "✓ Scaled image to ${processedBitmap.width}x${processedBitmap.height}")
        } else if (maxSize > maxDimension) {
            Log.d("ARDrawing", "Image too large ($maxSize > $maxDimension) - scaling down...")
            val scale = maxDimension.toFloat() / maxSize.toFloat()
            val newWidth = (processedBitmap.width * scale).toInt()
            val newHeight = (processedBitmap.height * scale).toInt()

            Log.d("ARDrawing", "Scale factor: $scale, new dimensions: ${newWidth}x${newHeight}")
            processedBitmap = Bitmap.createScaledBitmap(
                processedBitmap,
                newWidth,
                newHeight,
                true
            )
            Log.d("ARDrawing", "✓ Scaled image to ${processedBitmap.width}x${processedBitmap.height}")
        } else {
            Log.d("ARDrawing", "Image dimensions are within acceptable range - no scaling needed")
        }

        Log.d("ARDrawing", "=== preprocessImageForARCore EXIT - final size: ${processedBitmap.width}x${processedBitmap.height} ===")
        return processedBitmap
    }
    
    // Simplified AR session creation - actual session creation happens in GLSurfaceView renderer
    fun createARSession() {
        Log.d("ARDrawing", "=== createARSession ENTRY ===")
        Log.d("ARDrawing", "Current state - arSession: ${arSession != null}, permissions granted: ${cameraPermissionsState.allPermissionsGranted}")

        if (arSession != null || !cameraPermissionsState.allPermissionsGranted) {
            Log.d("ARDrawing", "Early exit - session exists or no permissions: session=${arSession != null}, permissions=${cameraPermissionsState.allPermissionsGranted}")
            return
        }

        Log.d("ARDrawing", "Proceeding with session creation...")
        scope.launch {
            Log.d("ARDrawing", "Coroutine launched for ARCore session creation")
            val oldCheckingState = isCheckingARCore
            isCheckingARCore = true
            Log.d("ARDrawing", "STATE CHANGE: isCheckingARCore: $oldCheckingState -> $isCheckingARCore")

            Log.d("ARDrawing", "=== ARCore Session creation delegated to GLSurfaceView ===")

            try {
                // Reset all flags - actual session creation happens in onSurfaceCreated
                Log.d("ARDrawing", "Resetting ARCore initialization flags...")

                val oldDisposedState = isSessionDisposed
                val oldReadyState = sessionReady
                val oldTextureState = textureSet
                val oldInstallRequestedState = installRequested
                val oldErrorState = arError
                val oldHintState = showPlaceAnchorHint

                isSessionDisposed = false
                sessionReady = false
                textureSet = false
                isCheckingARCore = false
                installRequested = false
                arError = null
                showPlaceAnchorHint = true

                Log.d("ARDrawing", "STATE CHANGES:")
                Log.d("ARDrawing", "  - isSessionDisposed: $oldDisposedState -> $isSessionDisposed")
                Log.d("ARDrawing", "  - sessionReady: $oldReadyState -> $sessionReady")
                Log.d("ARDrawing", "  - textureSet: $oldTextureState -> $textureSet")
                Log.d("ARDrawing", "  - isCheckingARCore: true -> $isCheckingARCore")
                Log.d("ARDrawing", "  - installRequested: $oldInstallRequestedState -> $installRequested")
                Log.d("ARDrawing", "  - arError: $oldErrorState -> $arError")
                Log.d("ARDrawing", "  - showPlaceAnchorHint: $oldHintState -> $showPlaceAnchorHint")

                Log.d("ARDrawing", "=== ARCore Session initialization flags reset ===")
            } catch (e: Exception) {
                Log.e("ARDrawing", "Unexpected exception during ARCore initialization: ${e.message}", e)
                val oldError = arError
                val oldChecking = isCheckingARCore
                arError = "Failed to initialize ARCore: ${e.message}"
                isCheckingARCore = false

                Log.d("ARDrawing", "STATE CHANGES (error):")
                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")
            }
        }
        Log.d("ARDrawing", "=== createARSession EXIT ===")
    }
    
    // Check ARCore availability and request install if needed
    LaunchedEffect(cameraPermissionsState.allPermissionsGranted) {
        Log.d("ARDrawing", "=== ARCore Availability Check LaunchedEffect ENTRY ===")
        Log.d("ARDrawing", "Camera permissions granted: ${cameraPermissionsState.allPermissionsGranted}")
        Log.d("ARDrawing", "Current AR session exists: ${arSession != null}")
        Log.d("ARDrawing", "Current isCheckingARCore: $isCheckingARCore")

        if (cameraPermissionsState.allPermissionsGranted && arSession == null) {
            Log.d("ARDrawing", "Conditions met for ARCore check - proceeding...")

            val oldCheckingState = isCheckingARCore
            isCheckingARCore = true
            Log.d("ARDrawing", "STATE CHANGE: isCheckingARCore: $oldCheckingState -> $isCheckingARCore")

            Log.d("ARDrawing", "Starting ARCore availability check...")

            try {
                Log.d("ARDrawing", "Getting ArCoreApk instance...")
                // Check ARCore availability first
                val arCoreApk = ArCoreApk.getInstance()
                Log.d("ARDrawing", "✓ ArCoreApk instance obtained: ${arCoreApk.javaClass.simpleName}")

                Log.d("ARDrawing", "Calling checkAvailability()...")
                val availability = arCoreApk.checkAvailability(context)
                Log.d("ARDrawing", "✓ ARCore availability check completed")
                Log.d("ARDrawing", "ARCore availability result: $availability")
                Log.d("ARDrawing", "  - isSupported: ${availability.isSupported}")
                Log.d("ARDrawing", "  - isTransient: ${availability.isTransient}")
                Log.d("ARDrawing", "  - toString: ${availability.toString()}")

                when {
                    availability.isTransient -> {
                        Log.d("ARDrawing", "ARCore availability is transient - status still being determined")
                        Log.d("ARDrawing", "Waiting 500ms before retry...")
                        // ARCore is checking availability - wait a bit
                        kotlinx.coroutines.delay(500)
                        Log.d("ARDrawing", "Retrying availability check after delay...")
                        // Retry after delay
                        return@LaunchedEffect
                    }
                    availability.isSupported -> {
                        Log.d("ARDrawing", "✓ ARCore is supported on this device - checking installation status...")
                        // ARCore is supported, check if installation is needed
                        Log.d("ARDrawing", "Calling requestInstall() with user prompt...")
                        val installStatus = try {
                            arCoreApk.requestInstall(activity, true)
                        } catch (e: Exception) {
                            Log.e("ARDrawing", "Exception during requestInstall(): ${e.message}", e)
                            throw e
                        }

                        Log.d("ARDrawing", "✓ requestInstall() completed")
                        Log.d("ARDrawing", "ARCore install status: $installStatus")

                        when (installStatus) {
                            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                                Log.d("ARDrawing", "ARCore installation requested - Play Store dialog shown")
                                Log.d("ARDrawing", "Setting installRequested flag and stopping session creation")

                                val oldInstallRequested = installRequested
                                val oldError = arError
                                val oldChecking = isCheckingARCore

                                // Installation was requested - STOP HERE and wait for activity resume
                                installRequested = true
                                arError = "ARCore installation requested. Please install ARCore from Play Store and return to this screen."
                                isCheckingARCore = false

                                Log.d("ARDrawing", "STATE CHANGES:")
                                Log.d("ARDrawing", "  - installRequested: $oldInstallRequested -> $installRequested")
                                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")

                                // DO NOT create session here - wait for onResume
                                Log.d("ARDrawing", "Exiting LaunchedEffect - waiting for user to install ARCore")
                                return@LaunchedEffect
                            }
                            ArCoreApk.InstallStatus.INSTALLED -> {
                                Log.d("ARDrawing", "✓ ARCore is already installed - proceeding with Session creation")

                                val oldChecking = isCheckingARCore
                                isCheckingARCore = false
                                Log.d("ARDrawing", "STATE CHANGE: isCheckingARCore: $oldChecking -> $isCheckingARCore")

                                Log.d("ARDrawing", "Calling createARSession()...")
                                createARSession()
                                Log.d("ARDrawing", "createARSession() call completed")
                            }
                            else -> {
                                Log.w("ARDrawing", "⚠ Unexpected install status: $installStatus")

                                val oldError = arError
                                val oldChecking = isCheckingARCore
                                arError = "ARCore installation status: $installStatus. Please check device compatibility."
                                isCheckingARCore = false

                                Log.d("ARDrawing", "STATE CHANGES:")
                                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")

                                Log.d("ARDrawing", "Exiting due to unexpected install status")
                                return@LaunchedEffect
                            }
                        }
                    }
                    else -> {
                        Log.e("ARDrawing", "✗ ARCore is NOT supported on this device")

                        val oldError = arError
                        val oldChecking = isCheckingARCore
                        arError = "ARCore is not supported on this device"
                        isCheckingARCore = false

                        Log.d("ARDrawing", "STATE CHANGES:")
                        Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                        Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")

                        Log.d("ARDrawing", "Exiting due to unsupported device")
                        return@LaunchedEffect
                    }
                }
            } catch (e: UnavailableArcoreNotInstalledException) {
                Log.e("ARDrawing", "UnavailableArcoreNotInstalledException: ${e.message}", e)

                val oldError = arError
                val oldChecking = isCheckingARCore
                arError = "ARCore is not installed. Please install ARCore from Play Store."
                isCheckingARCore = false

                Log.d("ARDrawing", "STATE CHANGES:")
                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")
            } catch (e: UnavailableDeviceNotCompatibleException) {
                Log.e("ARDrawing", "UnavailableDeviceNotCompatibleException: ${e.message}", e)

                val oldError = arError
                val oldChecking = isCheckingARCore
                arError = "Device is not compatible with ARCore"
                isCheckingARCore = false

                Log.d("ARDrawing", "STATE CHANGES:")
                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")
            } catch (e: UnavailableSdkTooOldException) {
                Log.e("ARDrawing", "UnavailableSdkTooOldException: ${e.message}", e)

                val oldError = arError
                val oldChecking = isCheckingARCore
                arError = "ARCore SDK is too old. Please update the app."
                isCheckingARCore = false

                Log.d("ARDrawing", "STATE CHANGES:")
                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")
            } catch (e: UnavailableUserDeclinedInstallationException) {
                Log.e("ARDrawing", "UnavailableUserDeclinedInstallationException: ${e.message}", e)

                val oldError = arError
                val oldChecking = isCheckingARCore
                arError = "ARCore installation was declined. Please install ARCore to use this feature."
                isCheckingARCore = false

                Log.d("ARDrawing", "STATE CHANGES:")
                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")
            } catch (e: Exception) {
                Log.e("ARDrawing", "Unexpected exception during ARCore availability check: ${e.message}", e)
                Log.e("ARDrawing", "Exception type: ${e.javaClass.name}")
                e.printStackTrace()

                val oldError = arError
                val oldChecking = isCheckingARCore
                arError = "Failed to check ARCore availability: ${e.message}"
                isCheckingARCore = false

                Log.d("ARDrawing", "STATE CHANGES:")
                Log.d("ARDrawing", "  - arError: $oldError -> $arError")
                Log.d("ARDrawing", "  - isCheckingARCore: $oldChecking -> $isCheckingARCore")
            }
        } else {
            Log.d("ARDrawing", "Skipping ARCore availability check:")
            Log.d("ARDrawing", "  - Permissions granted: ${cameraPermissionsState.allPermissionsGranted}")
            Log.d("ARDrawing", "  - AR session exists: ${arSession != null}")

            if (!cameraPermissionsState.allPermissionsGranted) {
                Log.d("ARDrawing", "Camera permissions not granted - requesting...")
                cameraPermissionsState.launchMultiplePermissionRequest()
                Log.d("ARDrawing", "Permission request launched")
            } else {
                Log.d("ARDrawing", "All conditions met for ARCore check but skipping (probably already processed)")
            }
        }

        Log.d("ARDrawing", "=== ARCore Availability Check LaunchedEffect EXIT ===")
    }
    
    // Lifecycle observer: Check ARCore again when activity resumes (after install)
    DisposableEffect(lifecycleOwner, installRequested) {
        Log.d("ARDrawing", "=== Setting up Lifecycle Observer ===")
        Log.d("ARDrawing", "installRequested: $installRequested")

        val observer = LifecycleEventObserver { _, event ->
            Log.d("ARDrawing", "Lifecycle event received: $event")
            Log.d("ARDrawing", "Current state - installRequested: $installRequested, arSession: ${arSession != null}")

            if (event == Lifecycle.Event.ON_RESUME && installRequested && arSession == null) {
                Log.d("ARDrawing", "✓ ON_RESUME triggered after install request - checking ARCore again...")
                scope.launch {
                    Log.d("ARDrawing", "Coroutine launched for resume ARCore check")
                    try {
                        Log.d("ARDrawing", "Waiting 1000ms for Play Store installation to complete...")
                        // Wait a bit for Play Store installation to complete (if user installed it)
                        kotlinx.coroutines.delay(1000)
                        Log.d("ARDrawing", "✓ Wait completed, checking ARCore availability...")

                        val arCoreApk = ArCoreApk.getInstance()
                        Log.d("ARDrawing", "✓ ArCoreApk instance obtained")

                        val availability = arCoreApk.checkAvailability(context)
                        Log.d("ARDrawing", "ARCore availability after resume: $availability")
                        Log.d("ARDrawing", "  - isSupported: ${availability.isSupported}")

                        if (!availability.isSupported) {
                            Log.w("ARDrawing", "✗ ARCore is not supported after resume - device compatibility issue")
                            return@launch
                        }

                        Log.d("ARDrawing", "✓ ARCore supported, attempting to verify installation...")
                        // The most reliable way to check if ARCore is installed is to try creating a session
                        // requestInstall() can return INSTALL_REQUESTED even if installation is complete
                        // but Play Store hasn't updated the status yet
                        Log.d("ARDrawing", "Attempting to verify ARCore installation by creating test session...")
                        try {
                            // Try creating a test session - this is the definitive check
                            val testSession = Session(context)
                            testSession.close() // Close immediately, we just wanted to verify it works
                            Log.d("ARDrawing", "✓ Test session created and closed successfully - ARCore is installed and ready")
                            Log.d("ARDrawing", "Creating actual session...")
                            // ARCore is definitely installed, create the real session
                            createARSession()
                            Log.d("ARDrawing", "✓ createARSession() called after successful verification")
                        } catch (e: UnavailableArcoreNotInstalledException) {
                            Log.d("ARDrawing", "✗ ARCore is still not installed - test session creation failed")
                            Log.d("ARDrawing", "Checking install status to determine next action...")

                            // Check install status to see if we should show Play Store again
                            val installStatus = arCoreApk.requestInstall(activity, false)
                            Log.d("ARDrawing", "ARCore install status (no prompt): $installStatus")

                            if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                                Log.d("ARDrawing", "Installation not completed - user may need to finish installation")
                                val oldError = arError
                                arError = "ARCore installation is not complete. Please complete the installation from Play Store and return to this screen."
                                Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
                            } else {
                                Log.d("ARDrawing", "Trying to request install again with user prompt...")
                                // Try requesting install again (user might have cancelled)
                                val retryStatus = arCoreApk.requestInstall(activity, true)
                                Log.d("ARDrawing", "Re-requested ARCore installation status: $retryStatus")

                                if (retryStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                                    Log.d("ARDrawing", "✓ Re-requested ARCore installation")
                                    val oldError = arError
                                    arError = "ARCore installation required. Please install from Play Store."
                                    Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
                                } else {
                                    Log.w("ARDrawing", "Unexpected retry status: $retryStatus")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ARDrawing", "Error verifying ARCore installation: ${e.message}", e)
                            Log.d("ARDrawing", "Attempting to create session normally despite verification error...")
                            // If it's a different error, try creating session normally anyway
                            createARSession()
                        }
                    } catch (e: Exception) {
                        Log.e("ARDrawing", "Error checking ARCore on resume: ${e.message}", e)
                        Log.e("ARDrawing", "Resume ARCore check failed completely")
                    }
                }
            } else {
                Log.d("ARDrawing", "ON_RESUME conditions not met:")
                Log.d("ARDrawing", "  - Event is ON_RESUME: ${event == Lifecycle.Event.ON_RESUME}")
                Log.d("ARDrawing", "  - installRequested: $installRequested")
                Log.d("ARDrawing", "  - arSession is null: ${arSession == null}")
            }
        }

        Log.d("ARDrawing", "Adding lifecycle observer...")
        lifecycleOwner.lifecycle.addObserver(observer)
        Log.d("ARDrawing", "✓ Lifecycle observer added")
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Monitor GLSurfaceView timeout - show error if surface never created
    LaunchedEffect(glSurfaceViewCreationTime) {
        Log.d("ARDrawing", "=== GLSurfaceView Timeout Monitor LaunchedEffect ENTRY ===")
        Log.d("ARDrawing", "glSurfaceViewCreationTime: $glSurfaceViewCreationTime")
        Log.d("ARDrawing", "sessionReady: $sessionReady")
        Log.d("ARDrawing", "arError: $arError")

        if (glSurfaceViewCreationTime > 0 && !sessionReady) {
            Log.d("ARDrawing", "GLSurfaceView created but session not ready - starting 10s timeout...")
            val timeoutStart = System.currentTimeMillis()
            kotlinx.coroutines.delay(10000) // 10 second timeout
            val timeoutEnd = System.currentTimeMillis()
            val actualDelay = timeoutEnd - timeoutStart

            Log.d("ARDrawing", "Timeout check after ${actualDelay}ms delay")
            Log.d("ARDrawing", "sessionReady: $sessionReady, arError: $arError")

            if (!sessionReady && arError == null) {
                Log.e("ARDrawing", "✗ GLSurfaceView surface creation timeout - onSurfaceCreated never called")
                Log.e("ARDrawing", "Surface was created at: $glSurfaceViewCreationTime")
                Log.e("ARDrawing", "Current time: $timeoutEnd")
                Log.e("ARDrawing", "Time since surface creation: ${timeoutEnd - glSurfaceViewCreationTime}ms")

                val oldError = arError
                arError = "AR initialization failed: OpenGL surface could not be created. Please try restarting the app."
                Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
            } else {
                Log.d("ARDrawing", "Timeout check passed - session is ready or error already set")
            }
        } else {
            Log.d("ARDrawing", "Skipping timeout monitor - surface not created yet or session already ready")
        }

        Log.d("ARDrawing", "=== GLSurfaceView Timeout Monitor LaunchedEffect EXIT ===")
    }

    // Monitor AR tracking state - process frames from GL thread
    LaunchedEffect(arFrame) {
        Log.v("ARDrawing", "=== Frame Processing LaunchedEffect ENTRY ===")
        Log.v("ARDrawing", "arFrame available: ${arFrame != null}")

        val frame = arFrame ?: run {
            Log.v("ARDrawing", "No AR frame available, exiting")
            return@LaunchedEffect
        }

        Log.v("ARDrawing", "Processing frame: ${frame.javaClass.simpleName}")

        // Safety checks - ensure everything is ready
        Log.v("ARDrawing", "Safety checks - sessionReady: $sessionReady, textureSet: $textureSet, isSessionDisposed: $isSessionDisposed, imageTrackingMode: $isImageTrackingMode")

        if (isSessionDisposed || !sessionReady || !textureSet) {
            Log.v("ARDrawing", "Skipping frame processing - session not ready or disposed")
            Log.v("ARDrawing", "  - sessionReady: $sessionReady")
            Log.v("ARDrawing", "  - textureSet: $textureSet")
            Log.v("ARDrawing", "  - isSessionDisposed: $isSessionDisposed")
            return@LaunchedEffect
        }

        // Additional check: ensure we have a valid AR session
        val session = arSession ?: run {
            Log.w("ARDrawing", "AR session is null during frame processing - critical error")
            return@LaunchedEffect
        }

        Log.v("ARDrawing", "✓ All safety checks passed, processing frame...")

        try {
            // Handle image tracking mode
            if (isImageTrackingMode) {
                Log.v("ARDrawing", "Processing image tracking mode")
           //     processImageTracking(frame)
                return@LaunchedEffect
            }

            // Check anchor tracking state first (plane detection mode)
            val anchor = planeAnchor
            Log.v("ARDrawing", "Checking anchor state - anchor exists: ${anchor != null}")

            if (anchor != null) {
                val trackingState = anchor.trackingState
                Log.v("ARDrawing", "Anchor tracking state: $trackingState")

                when (trackingState) {
                    TrackingState.TRACKING -> {
                        Log.v("ARDrawing", "Anchor is TRACKING")
                        if (!isTracking) {
                            Log.d("ARDrawing", "✓ Anchor location tracked - TRACKING started")
                            Log.d("ARDrawing", "Updating UI state for tracking...")

                            val oldTracking = isTracking
                            val oldDialog = showAnchorLostDialog
                            val oldHint = showPlaceAnchorHint

                            isTracking = true
                            showAnchorLostDialog = false
                            showPlaceAnchorHint = false

                            Log.d("ARDrawing", "STATE CHANGES:")
                            Log.d("ARDrawing", "  - isTracking: $oldTracking -> $isTracking")
                            Log.d("ARDrawing", "  - showAnchorLostDialog: $oldDialog -> $showAnchorLostDialog")
                            Log.d("ARDrawing", "  - showPlaceAnchorHint: $oldHint -> $showPlaceAnchorHint")
                        } else {
                            Log.v("ARDrawing", "Anchor already tracking, no state change needed")
                        }
                    }
                    TrackingState.STOPPED, TrackingState.PAUSED -> {
                        Log.v("ARDrawing", "Anchor tracking STOPPED or PAUSED")
                        if (isTracking) {
                            Log.d("ARDrawing", "✗ Anchor location lost - TRACKING stopped")
                            Log.d("ARDrawing", "Showing anchor lost dialog...")

                            val oldTracking = isTracking
                            val oldDialog = showAnchorLostDialog

                            isTracking = false
                            showAnchorLostDialog = true

                            Log.d("ARDrawing", "STATE CHANGES:")
                            Log.d("ARDrawing", "  - isTracking: $oldTracking -> $isTracking")
                            Log.d("ARDrawing", "  - showAnchorLostDialog: $oldDialog -> $showAnchorLostDialog")
                        } else {
                            Log.v("ARDrawing", "Anchor already not tracking, no state change needed")
                        }
                    }
                    else -> {
                        Log.v("ARDrawing", "Anchor in transitional state: $trackingState")
                    }
                }
            } else {
                Log.v("ARDrawing", "No anchor set - checking for auto-placement conditions")
                // No anchor yet - try to auto-place if we have a captured image
                Log.v("ARDrawing", "Checking auto-placement conditions - anchorBitmap exists: ${anchorBitmap != null}")

                if (anchorBitmap != null) {
                    Log.v("ARDrawing", "Anchor bitmap available - attempting auto-placement")
                    val currentTime = System.currentTimeMillis()
                    Log.v("ARDrawing", "Current time: $currentTime")

                    // Initialize start time on first attempt
                    if (autoPlacementStartTime == 0L) {
                        val oldStartTime = autoPlacementStartTime
                        autoPlacementStartTime = currentTime
                        Log.d("ARDrawing", "STATE CHANGE: autoPlacementStartTime: $oldStartTime -> $autoPlacementStartTime (initialized)")
                    }

                    // Check for timeout (30 seconds)
                    val timeElapsed = currentTime - autoPlacementStartTime
                    Log.v("ARDrawing", "Time elapsed since auto-placement start: ${timeElapsed}ms")

                    if (timeElapsed > 30000 && !showManualFallback) {
                        Log.d("ARDrawing", "⚠ Auto-placement timeout reached (30s), showing manual fallback")
                        val oldManualFallback = showManualFallback
                        showManualFallback = true
                        Log.d("ARDrawing", "STATE CHANGE: showManualFallback: $oldManualFallback -> $showManualFallback")
                    }

                    Log.v("ARDrawing", "showManualFallback: $showManualFallback")

                    if (!showManualFallback) {
                        Log.v("ARDrawing", "Attempting to get updated trackables...")
                        val planes = frame.getUpdatedTrackables(Plane::class.java)
                        Log.v("ARDrawing", "Found ${planes.size} planes total")

                        // Performance optimization: Only log in debug mode and limit frequency
                        if (debugMode && (System.currentTimeMillis() % 2000 < 100)) { // Log every ~2 seconds
                            Log.d("ARDrawing", "Plane detection: found ${planes.size} planes")
                            planes.forEachIndexed { index, plane ->
                                Log.d("ARDrawing", "  Plane $index: type=${plane.type}, tracking=${plane.trackingState}")
                            }
                        }

                        // Prioritize horizontal upward-facing planes for better stability
                        val horizontalPlanes = planes.filter {
                            it.trackingState == TrackingState.TRACKING &&
                            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                        }

                        Log.v("ARDrawing", "Horizontal upward planes: ${horizontalPlanes.size}")

                        val validPlanes = if (horizontalPlanes.isNotEmpty()) {
                            // Use horizontal planes first (tables, floors)
                            Log.v("ARDrawing", "Using horizontal upward-facing planes")
                            horizontalPlanes
                        } else {
                            // Fallback to any horizontal plane
                            Log.v("ARDrawing", "No horizontal upward planes, checking other types...")
                            planes.filter {
                                it.trackingState == TrackingState.TRACKING &&
                                (it.type == Plane.Type.HORIZONTAL_DOWNWARD_FACING ||
                                 it.type == Plane.Type.VERTICAL)
                            }
                        }

                        Log.v("ARDrawing", "Valid planes for anchoring: ${validPlanes.size}")

                        // Update plane detection state for UI
                        val oldPlaneDetected = isPlaneDetected
                        isPlaneDetected = validPlanes.isNotEmpty()
                        if (oldPlaneDetected != isPlaneDetected) {
                            Log.d("ARDrawing", "STATE CHANGE: isPlaneDetected: $oldPlaneDetected -> $isPlaneDetected")
                        }

                        if (validPlanes.isNotEmpty()) {
                            Log.d("ARDrawing", "✓ Suitable planes found - attempting auto-placement...")
                            // Automatically place anchor on first detected plane
                            val selectedPlane = validPlanes.first()
                            Log.d("ARDrawing", "Selected plane: type=${selectedPlane.type}, tracking=${selectedPlane.trackingState}")

                            try {
                                val pose = selectedPlane.centerPose
                                Log.d("ARDrawing", "Plane center pose: x=${pose.tx()}, y=${pose.ty()}, z=${pose.tz()}")

                                val newAnchor = selectedPlane.createAnchor(pose)
                                Log.d("ARDrawing", "✓ Anchor created successfully")

                                val oldAnchor = planeAnchor
                                val oldTracking = isTracking
                                val oldHint = showPlaceAnchorHint
                                val oldStartTime = autoPlacementStartTime
                                val oldManualFallback = showManualFallback

                                planeAnchor = newAnchor
                                isTracking = true
                                showPlaceAnchorHint = false
                                // Reset timeout tracking
                                autoPlacementStartTime = 0L
                                showManualFallback = false

                                Log.d("ARDrawing", "STATE CHANGES:")
                                Log.d("ARDrawing", "  - planeAnchor: ${oldAnchor != null} -> ${planeAnchor != null}")
                                Log.d("ARDrawing", "  - isTracking: $oldTracking -> $isTracking")
                                Log.d("ARDrawing", "  - showPlaceAnchorHint: $oldHint -> $showPlaceAnchorHint")
                                Log.d("ARDrawing", "  - autoPlacementStartTime: $oldStartTime -> $autoPlacementStartTime (reset)")
                                Log.d("ARDrawing", "  - showManualFallback: $oldManualFallback -> $showManualFallback")

                                Log.d("ARDrawing", "✓ Auto-placed anchor on ${selectedPlane.type} plane at: ${pose.tx()}, ${pose.ty()}, ${pose.tz()}")
                            } catch (e: Exception) {
                                Log.e("ARDrawing", "Error auto-placing anchor: ${e.message}", e)
                                Log.d("ARDrawing", "Setting manual fallback due to anchor creation failure")
                                // If anchor creation fails, don't retry automatically - show manual option
                                val oldManualFallback = showManualFallback
                                showManualFallback = true
                                Log.d("ARDrawing", "STATE CHANGE: showManualFallback: $oldManualFallback -> $showManualFallback")
                            }
                        } else if (debugMode && (System.currentTimeMillis() % 5000 < 100)) {
                            // Log less frequently to avoid spam
                            Log.d("ARDrawing", "No suitable planes found - keep scanning")
                        }
                    } else {
                        Log.v("ARDrawing", "Manual fallback active - skipping auto-placement")
                    }
                } else {
                    Log.v("ARDrawing", "No anchor bitmap - manual placement mode")
                    // No captured image - use manual placement
                    val planes = frame.getUpdatedTrackables(Plane::class.java)
                    val hasValidPlane = planes.any {
                        it.trackingState == TrackingState.TRACKING &&
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }
                    isPlaneDetected = hasValidPlane
                    if (hasValidPlane && !showPlaceAnchorHint) {
                        Log.d("ARDrawing", "Plane detected - showing manual placement option")
                        showPlaceAnchorHint = true
                    }
                }
            }

            Log.v("ARDrawing", "Frame processing completed successfully")
        } catch (e: Exception) {
            Log.e("ARDrawing", "Error processing AR frame: ${e.message}", e)
            Log.e("ARDrawing", "Exception type: ${e.javaClass.name}")
            e.printStackTrace()
            Log.e("ARDrawing", "Frame processing failed - AR session may be in invalid state")
        }

        Log.v("ARDrawing", "=== Frame Processing LaunchedEffect EXIT ===")
    }
    
    // Function to place anchor on detected plane or use Instant Placement
    fun placeAnchorOnPlane() {
        Log.d("ARDrawing", "=== placeAnchorOnPlane ENTRY ===")
        Log.d("ARDrawing", "Current state - sessionReady: $sessionReady, arSession: ${arSession != null}, isSessionDisposed: $isSessionDisposed")

        if (!sessionReady) {
            Log.w("ARDrawing", "Cannot place anchor - AR session not ready")
            return
        }

        val session = arSession ?: run {
            Log.w("ARDrawing", "Cannot place anchor - AR session is null")
            return
        }

        if (isSessionDisposed) {
            Log.w("ARDrawing", "Cannot place anchor - session is being disposed")
            return
        }

        Log.d("ARDrawing", "All safety checks passed - launching anchor placement coroutine")
        scope.launch {
            Log.d("ARDrawing", "Anchor placement coroutine started")
            try {
                Log.d("ARDrawing", "Getting latest AR frame...")
                // Get latest frame safely
                val frame = arFrame ?: run {
                    Log.w("ARDrawing", "No AR frame available for anchor placement")
                    return@launch
                }

                Log.d("ARDrawing", "✓ AR frame obtained, checking for planes...")
                val planes = frame.getUpdatedTrackables(Plane::class.java)
                    .filter {
                        it.trackingState == TrackingState.TRACKING &&
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }

                Log.d("ARDrawing", "Found ${planes.size} horizontal upward-facing planes")

                if (planes.isNotEmpty()) {
                    Log.d("ARDrawing", "Using traditional plane-based placement")
                    // Use traditional plane-based placement
                    val plane = planes[0]
                    Log.d("ARDrawing", "Selected plane: type=${plane.type}, tracking=${plane.trackingState}")

                    val pose = plane.centerPose
                    Log.d("ARDrawing", "Plane center pose: x=${pose.tx()}, y=${pose.ty()}, z=${pose.tz()}")

                    Log.d("ARDrawing", "Creating anchor on plane...")
                    val anchor = plane.createAnchor(pose)
                    Log.d("ARDrawing", "✓ Anchor created successfully")

                    val oldAnchor = planeAnchor
                    val oldTracking = isTracking
                    val oldHint = showPlaceAnchorHint

                    planeAnchor = anchor
                    isTracking = true
                    showPlaceAnchorHint = false

                    Log.d("ARDrawing", "STATE CHANGES:")
                    Log.d("ARDrawing", "  - planeAnchor: ${oldAnchor != null} -> ${planeAnchor != null}")
                    Log.d("ARDrawing", "  - isTracking: $oldTracking -> $isTracking")
                    Log.d("ARDrawing", "  - showPlaceAnchorHint: $oldHint -> $showPlaceAnchorHint")

                    Log.d("ARDrawing", "✓ Anchor placed on plane at: ${pose.tx()}, ${pose.ty()}, ${pose.tz()}")
                } else if (instantPlacementEnabled) {
                    Log.d("ARDrawing", "No planes detected - using Instant Placement")
                    Log.d("ARDrawing", "instantPlacementEnabled: $instantPlacementEnabled")

                    // Use Instant Placement - place anchor at camera center
                    Log.d("ARDrawing", "Getting camera pose...")
                    val camera = frame.camera
                    val cameraPose = camera.pose
                    Log.d("ARDrawing", "Camera pose: x=${cameraPose.tx()}, y=${cameraPose.ty()}, z=${cameraPose.tz()}")

                    // Place anchor 1 meter in front of camera
                    val anchorPose = cameraPose.compose(com.google.ar.core.Pose.makeTranslation(0f, 0f, -1f))
                    Log.d("ARDrawing", "Calculated anchor pose: x=${anchorPose.tx()}, y=${anchorPose.ty()}, z=${anchorPose.tz()}")

                    Log.d("ARDrawing", "Creating Instant Placement anchor...")
                    val anchor = session.createAnchor(anchorPose)
                    Log.d("ARDrawing", "✓ Instant Placement anchor created")

                    val oldAnchor = planeAnchor
                    val oldTracking = isTracking
                    val oldHint = showPlaceAnchorHint

                    planeAnchor = anchor
                    isTracking = true
                    showPlaceAnchorHint = false

                    Log.d("ARDrawing", "STATE CHANGES:")
                    Log.d("ARDrawing", "  - planeAnchor: ${oldAnchor != null} -> ${planeAnchor != null}")
                    Log.d("ARDrawing", "  - isTracking: $oldTracking -> $isTracking")
                    Log.d("ARDrawing", "  - showPlaceAnchorHint: $oldHint -> $showPlaceAnchorHint")

                    Log.d("ARDrawing", "✓ Instant Placement anchor created at: ${anchorPose.tx()}, ${anchorPose.ty()}, ${anchorPose.tz()}")
                    Log.d("ARDrawing", "Note: Anchor pose will be refined as surface geometry is detected")
                } else {
                    Log.w("ARDrawing", "No valid plane found to place anchor and Instant Placement is disabled")
                    Log.w("ARDrawing", "Planes found: ${planes.size}, instantPlacementEnabled: $instantPlacementEnabled")

                    val oldError = arError
                    arError = "No surface detected. Please point camera at a flat surface like a desk or table."
                    Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
                }

                Log.d("ARDrawing", "Anchor placement completed successfully")
            } catch (e: Exception) {
                Log.e("ARDrawing", "Error placing anchor: ${e.message}", e)
                Log.e("ARDrawing", "Exception type: ${e.javaClass.name}")
                e.printStackTrace()

                val oldError = arError
                arError = "Failed to place anchor: ${e.message}"
                Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
            }

            Log.d("ARDrawing", "=== placeAnchorOnPlane EXIT ===")
        }
    }
    
    // Process image tracking for image tracking mode
    fun processImageTracking(frame: Frame) {
        Log.v("ARDrawing", "Processing image tracking...")

        try {
            val augmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
            Log.v("ARDrawing", "Found ${augmentedImages.size} augmented images")

            var foundTrackedImage = false

            for (image in augmentedImages) {
                when (image.trackingState) {
                    TrackingState.TRACKING -> {
                        Log.d("ARDrawing", "✓ Image TRACKED: ${image.name}")
                        foundTrackedImage = true

                        val oldTracked = isImageTracked
                        val oldName = trackedImageName
                        val oldSize = trackedImageSize
                        val oldCoords = trackedImageCoordinates
                        val oldStatus = trackedImageStatus

                        isImageTracked = true
                        trackedImageName = image.name
                        trackedImageSize = String.format("%.2f x %.2f meters", image.extentX, image.extentZ)
                        trackedImageCoordinates = String.format("(%.2f, %.2f, %.2f)",
                            image.centerPose.tx(), image.centerPose.ty(), image.centerPose.tz())
                        trackedImageStatus = "TRACKING ✅"

                        if (oldTracked != isImageTracked || oldName != trackedImageName ||
                            oldSize != trackedImageSize || oldCoords != trackedImageCoordinates ||
                            oldStatus != trackedImageStatus) {
                            Log.d("ARDrawing", "IMAGE TRACKING STATE CHANGES:")
                            Log.d("ARDrawing", "  - isImageTracked: $oldTracked -> $isImageTracked")
                            Log.d("ARDrawing", "  - trackedImageName: $oldName -> $trackedImageName")
                            Log.d("ARDrawing", "  - trackedImageSize: $oldSize -> $trackedImageSize")
                            Log.d("ARDrawing", "  - trackedImageCoordinates: $oldCoords -> $trackedImageCoordinates")
                            Log.d("ARDrawing", "  - trackedImageStatus: $oldStatus -> $trackedImageStatus")
                        }
                    }
                    TrackingState.PAUSED -> {
                        Log.v("ARDrawing", "Image tracking PAUSED: ${image.name}")
                        val oldStatus = trackedImageStatus
                        trackedImageStatus = "PAUSED ⏸️"
                        if (oldStatus != trackedImageStatus) {
                            Log.d("ARDrawing", "IMAGE TRACKING STATE CHANGE: trackedImageStatus: $oldStatus -> $trackedImageStatus")
                        }
                    }
                    TrackingState.STOPPED -> {
                        Log.v("ARDrawing", "Image tracking STOPPED: ${image.name}")
                        val oldTracked = isImageTracked
                        val oldStatus = trackedImageStatus
                        isImageTracked = false
                        trackedImageStatus = "STOPPED ❌"
                        if (oldTracked != isImageTracked || oldStatus != trackedImageStatus) {
                            Log.d("ARDrawing", "IMAGE TRACKING STATE CHANGES:")
                            Log.d("ARDrawing", "  - isImageTracked: $oldTracked -> $isImageTracked")
                            Log.d("ARDrawing", "  - trackedImageStatus: $oldStatus -> $trackedImageStatus")
                        }
                    }
                }
            }

            // If no images are being tracked, reset state
            if (!foundTrackedImage && isImageTracked) {
                Log.d("ARDrawing", "No images currently tracked - resetting state")
                val oldTracked = isImageTracked
                val oldStatus = trackedImageStatus
                isImageTracked = false
                trackedImageStatus = "Searching..."
                Log.d("ARDrawing", "IMAGE TRACKING STATE CHANGES:")
                Log.d("ARDrawing", "  - isImageTracked: $oldTracked -> $isImageTracked")
                Log.d("ARDrawing", "  - trackedImageStatus: $oldStatus -> $trackedImageStatus")
            }

        } catch (e: Exception) {
            Log.e("ARDrawing", "Error processing image tracking: ${e.message}", e)
        }
    }

    // Clean up AR Session when composable is disposed (not when session changes)
    DisposableEffect(Unit) {
        Log.d("ARDrawing", "=== Setting up cleanup DisposableEffect ===")

        onDispose {
            Log.d("ARDrawing", "=== COMPOSABLE DISPOSAL STARTED ===")
            Log.d("ARDrawing", "Cleanup state - arSession: ${arSession != null}, planeAnchor: ${planeAnchor != null}, pointCloud: ${pointCloud != null}")

            // Mark session as disposed to prevent further operations
            val oldDisposedState = isSessionDisposed
            isSessionDisposed = true
            Log.d("ARDrawing", "STATE CHANGE: isSessionDisposed: $oldDisposedState -> $isSessionDisposed")

            try {
                Log.d("ARDrawing", "Starting anchor cleanup...")
                // Clean up anchor first
                planeAnchor?.let { anchor ->
                    Log.d("ARDrawing", "Detaching anchor...")
                    try {
                        anchor.detach()
                        Log.d("ARDrawing", "✓ Anchor detached successfully")
                    } catch (e: Exception) {
                        Log.e("ARDrawing", "✗ Error detaching anchor: ${e.message}", e)
                        Log.e("ARDrawing", "Anchor detach failed - may cause memory leaks")
                    }
                }

                val oldAnchor = planeAnchor
                planeAnchor = null
                Log.d("ARDrawing", "STATE CHANGE: planeAnchor: ${oldAnchor != null} -> ${planeAnchor != null}")

                Log.d("ARDrawing", "Starting AR session cleanup...")
                // Clean up AR session
                arSession?.let { session ->
                    Log.d("ARDrawing", "Pausing AR session...")
                    try {
                        session.pause()
                        Log.d("ARDrawing", "✓ AR Session paused successfully")
                    } catch (e: Exception) {
                        Log.e("ARDrawing", "✗ Error pausing AR session: ${e.message}", e)
                    }

                    Log.d("ARDrawing", "Closing AR session...")
                    try {
                        session.close()
                        Log.d("ARDrawing", "✓ AR Session closed successfully")
                    } catch (e: Exception) {
                        Log.e("ARDrawing", "✗ Error closing AR session: ${e.message}", e)
                        Log.e("ARDrawing", "Session close failed - may cause resource leaks")
                    }
                }

                val oldSession = arSession
                arSession = null
                Log.d("ARDrawing", "STATE CHANGE: arSession: ${oldSession != null} -> ${arSession != null}")

                Log.d("ARDrawing", "Clearing frame reference...")
                // Clear frame reference to prevent memory leaks
                arFrame = null
                Log.d("ARDrawing", "✓ AR frame reference cleared")

                Log.d("ARDrawing", "Releasing point cloud resources...")
                // Release point cloud resources
                pointCloud?.forEach { cloud ->
                    try {
                        cloud.release()
                        Log.d("ARDrawing", "✓ Point cloud released")
                    } catch (e: Exception) {
                        Log.e("ARDrawing", "Error releasing point cloud: ${e.message}", e)
                    }
                }
                pointCloud = null
                Log.d("ARDrawing", "✓ Point cloud list cleared")

                // Log final memory state
                val runtime = Runtime.getRuntime()
                val usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                Log.d("ARDrawing", "Cleanup completed - Memory usage: ${usedMemoryMB}MB")

                Log.d("ARDrawing", "=== COMPOSABLE DISPOSAL COMPLETED ===")
            } catch (e: Exception) {
                Log.e("ARDrawing", "✗ Unexpected error during AR session cleanup: ${e.message}", e)
                Log.e("ARDrawing", "Cleanup failed - resources may not be properly released")
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isImageTrackingMode) "AR Image Tracking" else "Draw with AR",
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

            Log.d("ARDrawing", "=== UI State Evaluation ===")
            Log.d("ARDrawing", "Permissions granted: ${cameraPermissionsState.allPermissionsGranted}")
            Log.d("ARDrawing", "Checking ARCore: $isCheckingARCore")
            Log.d("ARDrawing", "Session ready: $sessionReady")
            Log.d("ARDrawing", "AR error: $arError")

            when {
                !cameraPermissionsState.allPermissionsGranted -> {
                    Log.d("ARDrawing", "UI STATE: Showing permission request view")
                    PermissionRequestView(
                        onRequestPermission = {
                            cameraPermissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
                isCheckingARCore -> {
                    Log.d("ARDrawing", "UI STATE: Showing ARCore availability check loading")
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
                !sessionReady && !isCheckingARCore && arError == null -> {
                    Log.d("ARDrawing", "UI STATE: Showing AR session initialization loading")
                    // Show AR initialization loading
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
                            text = "Initializing AR Session...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This may take a few seconds",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                arError != null -> {
                    Log.d("ARDrawing", "UI STATE: Showing error view - error: $arError")
                    ErrorView(
                        message = arError ?: "Unknown error",
                        onRetry = {
                            // Reset all AR state for retry
                            arError = null
                            isCheckingARCore = false
                            sessionReady = false
                            textureSet = false
                            sessionCreationAttempts = 0
                            arSession = null
                            arFrame = null
                            planeAnchor = null
                            isTracking = false
                        },
                        onRetakePhoto = {
                            arError = null
                            isCheckingARCore = false
                            onRetakeAnchor()
                        }
                    )
                }
                else -> {
                    Log.d("ARDrawing", "UI STATE: Showing main AR view")
                    // Camera Preview + ARCore GL Context
                    if (cameraPermissionsState.allPermissionsGranted) {
                        Log.d("ARDrawing", "Conditions met for AR rendering - showing camera and AR overlay")
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Camera Preview (CameraX) - Shows the camera feed
                            AndroidView(
                                factory = { ctx ->
                                    Log.d("ARDrawing", "Creating CameraX PreviewView...")
                                    val previewView = PreviewView(ctx)
                                    Log.d("ARDrawing", "✓ PreviewView created")

                                    Log.d("ARDrawing", "Launching CameraX setup coroutine...")
                                    scope.launch {
                                        Log.d("ARDrawing", "CameraX setup coroutine started")

                                        try {
                                            Log.d("ARDrawing", "Getting camera provider...")
                                            val cameraProvider = cameraProviderFuture.get()
                                            Log.d("ARDrawing", "✓ Camera provider obtained: ${cameraProvider.javaClass.simpleName}")

                                            Log.d("ARDrawing", "Creating preview use case...")
                                            val previewUseCase = Preview.Builder()
                                                .build()
                                            Log.d("ARDrawing", "✓ Preview use case created")

                                            Log.d("ARDrawing", "Setting surface provider...")
                                            previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                                            Log.d("ARDrawing", "✓ Surface provider set")

                                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                            Log.d("ARDrawing", "Camera selector: $cameraSelector")

                                            Log.d("ARDrawing", "Unbinding all previous camera uses...")
                                            cameraProvider.unbindAll()
                                            Log.d("ARDrawing", "✓ All camera uses unbound")

                                            Log.d("ARDrawing", "Binding camera to lifecycle...")
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                previewUseCase
                                            )
                                            Log.d("ARDrawing", "✓ CameraX preview bound successfully to lifecycle")

                                            Log.d("ARDrawing", "CameraX setup completed successfully")
                                        } catch (e: Exception) {
                                            Log.e("ARDrawing", "Error binding camera: ${e.message}", e)
                                            Log.e("ARDrawing", "Exception type: ${e.javaClass.name}")
                                            e.printStackTrace()

                                            val oldError = arError
                                            arError = "Camera setup failed: ${e.message}"
                                            Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
                                        }

                                        Log.d("ARDrawing", "CameraX setup coroutine completed")
                                    }

                                    Log.d("ARDrawing", "Returning PreviewView to AndroidView")
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Hidden GLSurfaceView for ARCore GL context (invisible)
                            AndroidView(
                                factory = { ctx ->

                                    val glView = GLSurfaceView(ctx).apply {

                                        setEGLContextClientVersion(2)
                                        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                                        preserveEGLContextOnPause = true

                                        // FIX #1 — Surface must be FULLY VISIBLE for ARCore to start
                                        alpha = 1f
                                        visibility = View.VISIBLE

                                        // FIX #2 — Bring surface above Compose
                                        setZOrderOnTop(true)

                                        setRenderer(object : GLSurfaceView.Renderer {

                                            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                                                Log.e("ARDrawing", "🔥 GLSurfaceView.onSurfaceCreated ENTRY")
                                                Log.d("ARDrawing", "GL10: ${gl?.javaClass?.simpleName ?: "null"}")
                                                Log.d("ARDrawing", "EGLConfig: ${config?.javaClass?.simpleName ?: "null"}")
                                                Log.d("ARDrawing", "Current AR session state: ${arSession != null}")
                                                Log.d("ARDrawing", "Session ready flag: $sessionReady")

                                                // Record surface creation time for timeout detection
                                                val oldCreationTime = glSurfaceViewCreationTime
                                                glSurfaceViewCreationTime = System.currentTimeMillis()
                                                Log.d("ARDrawing", "GL surface creation time: $oldCreationTime -> $glSurfaceViewCreationTime")

                                                try {
                                                    if (arSession == null) {
                                                        Log.d("ARDrawing", "Creating new ARCore session...")
                                                        arSession = Session(ctx)
                                                        Log.d("ARDrawing", "✓ ARCore Session created: ${arSession?.javaClass?.simpleName}")

                Log.d("ARDrawing", "Configuring session...")
                val cfg = Config(arSession).apply {
                    if (isImageTrackingMode && anchorBitmap != null) {
                        // Image tracking mode - disable plane finding, enable image tracking
                        planeFindingMode = Config.PlaneFindingMode.DISABLED
                        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

                        // Create image database for tracking
                        val imageDatabase = AugmentedImageDatabase(arSession)
                        val processedBitmap = preprocessImageForARCore(anchorBitmap)
                        imageDatabase.addImage("TrackedImage", processedBitmap)
                        augmentedImageDatabase = imageDatabase

                        Log.d("ARDrawing", "Image tracking configured - planeFindingMode: $planeFindingMode, updateMode: $updateMode")
                    } else {
                        // Plane detection mode (original behavior)
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                        Log.d("ARDrawing", "Plane detection configured - planeFindingMode: $planeFindingMode, updateMode: $updateMode")
                    }
                }

                                                        arSession!!.configure(cfg)
                                                        Log.d("ARDrawing", "✓ Session configured")

                                                        Log.d("ARDrawing", "Resuming session...")
                                                        arSession!!.resume()
                                                        Log.d("ARDrawing", "✓ Session resumed")

                                                        val oldReadyState = sessionReady
                                                        sessionReady = true
                                                        Log.d("ARDrawing", "STATE CHANGE: sessionReady: $oldReadyState -> $sessionReady")

                                                        Log.e("ARDrawing", "✓ ARCore session fully initialized and ready")
                                                    } else {
                                                        Log.d("ARDrawing", "AR session already exists, skipping creation")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("ARDrawing", "AR ERROR in onSurfaceCreated: ${e.message}", e)
                                                    Log.e("ARDrawing", "Exception type: ${e.javaClass.name}")
                                                    e.printStackTrace()

                                                    val oldError = arError
                                                    arError = "Failed to create AR session: ${e.message}"
                                                    Log.d("ARDrawing", "STATE CHANGE: arError: $oldError -> $arError")
                                                }

                                                Log.d("ARDrawing", "🔥 GLSurfaceView.onSurfaceCreated EXIT")
                                            }

                                            override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
                                                Log.d("ARDrawing", "📐 GLSurfaceView.onSurfaceChanged ENTRY - size: ${w}x${h}")
                                                Log.d("ARDrawing", "GL10: ${gl?.javaClass?.simpleName ?: "null"}")
                                                Log.d("ARDrawing", "Display geometry - width: $w, height: $h")

                                                val oldGeometryState = isDisplayGeometrySet

                                                try {
                                                    arSession?.setDisplayGeometry(0, w, h)
                                                    Log.d("ARDrawing", "✓ Display geometry set for AR session")
                                                    isDisplayGeometrySet = true
                                                } catch (e: Exception) {
                                                    Log.e("ARDrawing", "Error setting display geometry: ${e.message}", e)
                                                    isDisplayGeometrySet = false
                                                }

                                                Log.d("ARDrawing", "STATE CHANGE: isDisplayGeometrySet: $oldGeometryState -> $isDisplayGeometrySet")
                                                Log.d("ARDrawing", "📐 GLSurfaceView.onSurfaceChanged EXIT")
                                            }

                                            override fun onDrawFrame(gl: GL10?) {
                                                val frameStartTime = System.currentTimeMillis()
                                                Log.v("ARDrawing", "🎬 GLSurfaceView.onDrawFrame ENTRY - frame time: $frameStartTime")

                                                val session = arSession ?: run {
                                                    Log.v("ARDrawing", "No AR session available, skipping frame")
                                                    return
                                                }

                                                Log.v("ARDrawing", "Session available: ${session.javaClass.simpleName}")

                                                // Handle texture setup
                                                if (!textureSet) {
                                                    Log.d("ARDrawing", "Setting up camera texture...")
                                                    try {
                                                        val t = IntArray(1)
                                                        GLES20.glGenTextures(1, t, 0)
                                                        val textureId = t[0]
                                                        Log.d("ARDrawing", "Generated texture ID: $textureId")

                                                        session.setCameraTextureName(textureId)
                                                        Log.d("ARDrawing", "✓ Camera texture set for session")

                                                        val oldTextureState = textureSet
                                                        textureSet = true
                                                        Log.d("ARDrawing", "STATE CHANGE: textureSet: $oldTextureState -> $textureSet")
                                                    } catch (e: Exception) {
                                                        Log.e("ARDrawing", "Error setting up camera texture: ${e.message}", e)
                                                        return
                                                    }
                                                } else {
                                                    Log.v("ARDrawing", "Camera texture already set")
                                                }

                                                // Update AR frame
                                                try {
                                                    val frameUpdateStart = System.currentTimeMillis()
                                                    val oldFrame = arFrame
                                                    arFrame = session.update()
                                                    val frameUpdateTime = System.currentTimeMillis() - frameUpdateStart

                                                    Log.v("ARDrawing", "✓ AR frame updated - old: ${oldFrame != null}, new: ${arFrame != null}, update time: ${frameUpdateTime}ms")

                                                    // Update performance metrics
                                                    frameCount++
                                                    val currentTime = System.currentTimeMillis()
                                                    val timeSinceLastFpsUpdate = currentTime - lastFpsUpdateTime

                                                    if (timeSinceLastFpsUpdate >= 1000) { // Update FPS every second
                                                        currentFps = frameCount.toFloat() / (timeSinceLastFpsUpdate / 1000f)

                                                        // Log memory usage (approximate)
                                                        val runtime = Runtime.getRuntime()
                                                        val usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                                                        val maxMemoryMB = runtime.maxMemory() / (1024 * 1024)

                                                        Log.d("ARDrawing", "PERFORMANCE: FPS=$currentFps, Frames=$frameCount, Time=${timeSinceLastFpsUpdate}ms, Memory=${usedMemoryMB}MB/${maxMemoryMB}MB")
                                                        frameCount = 0
                                                        lastFpsUpdateTime = currentTime
                                                    }

                                                } catch (e: Exception) {
                                                    Log.e("ARDrawing", "Error updating AR frame: ${e.message}", e)
                                                    Log.e("ARDrawing", "Frame update failed - session may be in invalid state")

                                                    // Log error with performance context
                                                    val runtime = Runtime.getRuntime()
                                                    val usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                                                    Log.e("ARDrawing", "Error context - Memory usage: ${usedMemoryMB}MB, Frame count: $frameCount")
                                                }

                                                val frameDuration = System.currentTimeMillis() - frameStartTime
                                                Log.v("ARDrawing", "🎬 GLSurfaceView.onDrawFrame EXIT - duration: ${frameDuration}ms")
                                            }
                                        })

                                        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                                    }

                                    glView
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    // FIX #3 — DO NOT hide GLSurface. Hide it in other way later.
                                    .alpha(1f)
                            )

                        }
                        
                        // AR Image Overlay - Only show when tracking
                        if ((isTracking && !isImageTrackingMode) || (isImageTracked && isImageTrackingMode)) {
                            Log.d("ARDrawing", "UI STATE: Showing AR image overlay - isTracking: $isTracking, isImageTracked: $isImageTracked, imageTrackingMode: $isImageTrackingMode")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (anchorBitmap != null) {
                                    // Show captured anchor image
                                    Image(
                                        bitmap = anchorBitmap.asImageBitmap(),
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
                                                        try {
                                                            // Apply bounds checking to prevent crashes
                                                            val newScale = (imageScale * zoom).coerceIn(minScale, maxScale)
                                                            val newOffsetX = (imageOffsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                                            val newOffsetY = (imageOffsetY + pan.y).coerceIn(-maxOffset, maxOffset)

                                                            imageScale = newScale
                                                            imageOffsetX = newOffsetX
                                                            imageOffsetY = newOffsetY

                                                            Log.v("ARDrawing", "Image transform: scale=$newScale, offsetX=$newOffsetX, offsetY=$newOffsetY")
                                                        } catch (e: Exception) {
                                                            Log.e("ARDrawing", "Error during image transform: ${e.message}", e)
                                                            // Reset to safe values on error
                                                            imageScale = imageScale.coerceIn(minScale, maxScale)
                                                            imageOffsetX = imageOffsetX.coerceIn(-maxOffset, maxOffset)
                                                            imageOffsetY = imageOffsetY.coerceIn(-maxOffset, maxOffset)
                                                        }
                                                    }
                                                }
                                            },
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // Show template image
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
                                                        try {
                                                            // Apply bounds checking to prevent crashes
                                                            val newScale = (imageScale * zoom).coerceIn(minScale, maxScale)
                                                            val newOffsetX = (imageOffsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                                            val newOffsetY = (imageOffsetY + pan.y).coerceIn(-maxOffset, maxOffset)

                                                            imageScale = newScale
                                                            imageOffsetX = newOffsetX
                                                            imageOffsetY = newOffsetY

                                                            Log.v("ARDrawing", "Image transform: scale=$newScale, offsetX=$newOffsetX, offsetY=$newOffsetY")
                                                        } catch (e: Exception) {
                                                            Log.e("ARDrawing", "Error during image transform: ${e.message}", e)
                                                            // Reset to safe values on error
                                                            imageScale = imageScale.coerceIn(minScale, maxScale)
                                                            imageOffsetX = imageOffsetX.coerceIn(-maxOffset, maxOffset)
                                                            imageOffsetY = imageOffsetY.coerceIn(-maxOffset, maxOffset)
                                                        }
                                                    }
                                                }
                                            },
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Show image tracking status
                                if (isImageTrackingMode && isImageTracked) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(16.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🎯 IMAGE MATCHED!",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Name: $trackedImageName",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Status: $trackedImageStatus",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Show success indicator when anchor is auto-placed (plane mode)
                                if (!isImageTrackingMode && anchorBitmap != null && planeAnchor != null) {
                                    Text(
                                        text = "✓ Anchor placed at captured location",
                                        color = Color.Green,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(16.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )
                                }

                                // Show Instant Placement indicator
                                if (instantPlacementEnabled && planeAnchor != null && anchorBitmap == null) {
                                    Text(
                                        text = "⚡ Instant Placement - Anchor will refine as surfaces are detected",
                                        color = Color.Yellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 80.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )
                                }

                                // Point Cloud visualization (simple indicator for now)
                                if (showPointCloud && pointCloud != null) {
                                    Text(
                                        text = "📊 Point Cloud: ${pointCloud?.firstOrNull()?.getIds()?.remaining() ?: 0} points",
                                        color = Color.Cyan,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                                            .padding(4.dp)
                                    )
                                }

                                // FPS display (debug mode only)
                                if (debugMode) {
                                    Text(
                                        text = "FPS: ${currentFps.toInt()}",
                                        color = Color.Yellow,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Instructions overlay - Show when not tracking
                    if ((!isTracking && !showAnchorLostDialog && !isImageTrackingMode) ||
                        (!isImageTracked && isImageTrackingMode && !showAnchorLostDialog)) {
                        Log.d("ARDrawing", "UI STATE: Showing instructions overlay - isTracking: $isTracking, isImageTracked: $isImageTracked, showAnchorLostDialog: $showAnchorLostDialog, imageTrackingMode: $isImageTrackingMode")
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                    if (isImageTrackingMode) {
                                        // Image tracking mode instructions
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            if (isImageTracked) {
                                                // Image is being tracked - show success
                                                Text(
                                                    text = "🎯 Image Detected!",
                                                    color = Color(0xFF4CAF50),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Image is being tracked successfully",
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            } else {
                                                // Looking for image
                                                CircularProgressIndicator(
                                                    color = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "Looking for tracked image...",
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Point camera at the image you want to track\nMove slowly to help detection",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        }
                                    } else if (planeAnchor == null) {
                                        if (anchorBitmap != null && !showManualFallback) {
                                            // Show auto-placement progress when we have a captured image
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = if (isPlaneDetected) {
                                                        "Surface detected! Placing anchor..."
                                                    } else {
                                                        "Initializing AR and scanning surfaces..."
                                                    },
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = if (isPlaneDetected) {
                                                        "Anchor will be placed automatically"
                                                    } else {
                                                        "Move camera slowly around the area\nLook for flat surfaces like tables or floors\n\nInstant Placement: ${if (instantPlacementEnabled) "ON" else "OFF"}\nDepth: ${if (depthEnabled) "ON" else "OFF"}"
                                                    },
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        } else if (anchorBitmap != null && showManualFallback) {
                                            // Timeout reached - show manual placement option
                                            Text(
                                                text = "Surface detection taking longer than expected",
                                                color = Color.Yellow,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tap the button below to place anchor manually\n\nNote: Instant Placement is ${if (instantPlacementEnabled) "enabled" else "disabled"}",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        } else {
                                            // Fallback to manual placement
                                            Text(
                                                text = if (isPlaneDetected) {
                                                    "Surface detected!"
                                                } else {
                                                    "Point camera at a flat surface"
                                                },
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (isPlaneDetected) {
                                                    "Tap the button below to place anchor"
                                                } else {
                                                    "Move device slowly to detect surface"
                                                },
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Waiting for anchor...",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Place Anchor Button - Show for manual placement
                            if (planeAnchor == null && ((isPlaneDetected && anchorBitmap == null) || (showManualFallback && anchorBitmap != null))) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { placeAnchorOnPlane() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "Place Anchor Here",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    // Bottom Controls - Only show when tracking in plane mode (not image tracking)
                    if (isTracking && !isImageTrackingMode) {
                        Log.d("ARDrawing", "UI STATE: Showing bottom controls - isTracking: $isTracking, imageTrackingMode: $isImageTrackingMode")
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

                            // AR Settings Button
                            var showARSettings by remember { mutableStateOf(false) }
                            Button(
                                onClick = { showARSettings = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9C27B0)
                                )
                            ) {
                                Text("AR Settings", color = Color.White)
                            }

                            // AR Settings Dialog
                            if (showARSettings) {
                                AlertDialog(
                                    onDismissRequest = { showARSettings = false },
                                    title = { Text("AR Settings", color = Color.White) },
                                    text = {
                                        Column {
                                            // Instant Placement Toggle
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "Instant Placement",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "Place objects immediately without waiting for surface detection",
                                                        color = Color.Gray,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Switch(
                                                    checked = instantPlacementEnabled,
                                                    onCheckedChange = { instantPlacementEnabled = it },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color(0xFF4CAF50),
                                                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Depth Toggle
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "Depth",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "Enable occlusion - virtual objects hide behind real objects",
                                                        color = Color.Gray,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Switch(
                                                    checked = depthEnabled,
                                                    onCheckedChange = {
                                                        depthEnabled = it
                                                        // Note: Depth setting will take effect on next session restart
                                                        arError = "Depth setting will apply after restarting AR session"
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color(0xFF4CAF50),
                                                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Point Cloud Toggle
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "Point Cloud",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "Show depth points from environment (for debugging)",
                                                        color = Color.Gray,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Switch(
                                                    checked = showPointCloud,
                                                    onCheckedChange = { showPointCloud = it },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color(0xFF4CAF50),
                                                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = { showARSettings = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            )
                                        ) {
                                            Text("Done", color = Color.White)
                                        }
                                    },
                                    containerColor = Color(0xFF2C2C2C)
                                )
                            }
                        }
                    }
                }
            }
            
            // Anchor Lost Dialog - Show when tracking is lost
            if (showAnchorLostDialog) {
                Log.d("ARDrawing", "UI STATE: Showing anchor lost dialog")
                AnchorLostDialog(
                    onDismiss = { 
                        showAnchorLostDialog = false
                        // Reset anchor and allow re-placing
                        planeAnchor = null
                        isTracking = false
                    },
                    onRetakePhoto = {
                        showAnchorLostDialog = false
                        // Reset anchor and allow re-placing
                        planeAnchor = null
                        isTracking = false
                        showPlaceAnchorHint = true
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
                    text = "Anchor Location Lost",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "You moved away from the anchor location. Please return to the same place or place a new anchor on a flat surface.",
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
                        text = "Place New Anchor",
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
fun ErrorView(message: String, onRetry: () -> Unit, onRetakePhoto: (() -> Unit)? = null) {
    val context = LocalContext.current
    val isIncompatible = message.contains("incompatible", ignoreCase = true) ||
                         message.contains("not available on this device", ignoreCase = true)
    val isImageQualityError = message.contains("quality", ignoreCase = true) ||
                              message.contains("insufficient", ignoreCase = true) ||
                              message.contains("anchor image", ignoreCase = true)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                isIncompatible -> "Device Not Compatible"
                isImageQualityError -> "Image Quality Issue"
                else -> "AR Error"
            },
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
                "• Latest Google Play Services\n" +
                "• Gyroscope, accelerometer sensors\n" +
                "• Sufficient RAM (2GB+)\n\n" +
                "Compatible devices: Pixel, Samsung Galaxy S20+, OnePlus, etc.\n\n" +
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
        
        if (isImageQualityError && onRetakePhoto != null) {
            // Show retake photo button for image quality errors
            Button(
                onClick = onRetakePhoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("Retake Photo", color = Color.White)
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
