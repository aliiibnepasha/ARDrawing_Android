package com.example.ardrawing.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var imageScale by remember { mutableStateOf(0.75f) }
    var imageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var opacity by remember { mutableStateOf(0.5f) }

    var isLocked by remember { mutableStateOf(false) }
    var flashOn by remember { mutableStateOf(false) }

    var isPanelExpanded by remember { mutableStateOf(false) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionsState.allPermissionsGranted) {
            cameraPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(containerColor = Color.Black) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

            /* ================= OVERLAY IMAGE ================= */
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAssetImagePainter(template.imageAssetPath),
                    contentDescription = "Sketch",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(opacity)
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
                        },
                    contentScale = ContentScale.FillBounds
                )
            }

            /* ================= TOP LEFT BACK ================= */
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.back_arrow),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            /* ================= TOP RIGHT ICONS ================= */
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircleIcon(R.drawable.lock, isLocked) {
                    isLocked = !isLocked
                }
                CircleIcon(R.drawable.camera_ic, flashOn) {
                    flashOn = !flashOn
                }
            }

            /* ================= COLLAPSIBLE BOTTOM PANEL ================= */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    /* ---------- ARROW HANDLE ---------- */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { isPanelExpanded = !isPanelExpanded }) {
                            Icon(
                                painter = painterResource(
                                    if (isPanelExpanded)
                                        R.drawable.arrow_left   // replace later
                                    else
                                        R.drawable.arrow_left     // replace later
                                ),
                                contentDescription = "Toggle Panel",
                                tint = Color.Black
                            )
                        }
                    }

                    /* ---------- EXPANDED CONTENT ---------- */
                    AnimatedVisibility(visible = isPanelExpanded) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {

                            Text(
                                text = "Opacity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.camera_ic),
                                    contentDescription = null,
                                    tint = Color.Gray
                                )

                                Slider(
                                    value = opacity,
                                    onValueChange = { opacity = it },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF2979FF),
                                        activeTrackColor = Color(0xFF2979FF),
                                        inactiveTrackColor = Color(0xFFE0E0E0)
                                    )
                                )

                                Text(
                                    text = "${(opacity * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    /* ---------- TOOL ICON ROW (ALWAYS VISIBLE) ---------- */
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BottomIcon(R.drawable.camera_ic)
                        BottomIcon(R.drawable.camera_ic)
                        BottomIcon(R.drawable.camera_ic)
                        BottomIcon(R.drawable.camera_ic)
                    }
                }
            }
        }
    }
}

/* ================= REUSABLE ICONS ================= */

@Composable
private fun CircleIcon(
    icon: Int,
    active: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(
                if (active) Color(0xFFE3F2FD) else Color(0xFFF1F5F9),
                CircleShape
            )
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (active) Color(0xFF2979FF) else Color.Black,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun BottomIcon(
    icon: Int,
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .background(Color(0xFFF1F5F9), CircleShape)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color(0xFF2979FF),
            modifier = Modifier.size(20.dp)
        )
    }
}
