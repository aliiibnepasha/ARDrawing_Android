package com.example.ardrawing.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.components.AppTopBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ARAnchorCaptureScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit,
    onAnchorCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val cameraPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                capturedBitmap = bitmap
                showPreview = true
            }
        }
    }
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionsState.allPermissionsGranted) {
            cameraPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Capture Anchor Object",
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
                !cameraPermissionsState.allPermissionsGranted -> {
                    PermissionRequestView(
                        onRequestPermission = {
                            cameraPermissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
                showPreview && capturedBitmap != null -> {
                    // Preview captured image
                    ImagePreviewView(
                        bitmap = capturedBitmap!!,
                        onRetake = {
                            showPreview = false
                            capturedBitmap = null
                        },
                        onUse = {
                            onAnchorCaptured(capturedBitmap!!)
                        }
                    )
                }
                else -> {
                    // Camera View
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            scope.launch {
                                val cameraProvider = cameraProviderFuture.get()
                                
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                
                                val imageCaptureBuilder = ImageCapture.Builder()
                                imageCapture = imageCaptureBuilder.build()
                                
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                
                                try {
                                    cameraProvider.unbindAll()
                                    camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Instructions
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Capture Anchor Object",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Take a photo of a unique object with good contrast",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Capture Button
                    FloatingActionButton(
                        onClick = {
                            imageCapture?.let { capture ->
                                val file = java.io.File(context.cacheDir, "anchor_${System.currentTimeMillis()}.jpg")
                                val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                                
                                capture.takePicture(
                                    outputFileOptions,
                                    context.mainExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                            capturedBitmap = bitmap
                                            showPreview = true
                                        }
                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(32.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture",
                            tint = Color.White
                        )
                    }
                    
                    // Gallery Button
                    FloatingActionButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(32.dp),
                        containerColor = Color(0xFF2196F3)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Gallery",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePreviewView(
    bitmap: Bitmap,
    onRetake: () -> Unit,
    onUse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured Image",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onRetake,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("Retake", color = Color.White)
            }
            
            Button(
                onClick = onUse,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Use This", color = Color.White)
            }
        }
    }
}


