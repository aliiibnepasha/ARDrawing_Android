package com.example.ardrawing.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.utils.rememberAssetImagePainter

@Composable
fun PaperTraceScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit
) {

    /* ---------- BACK HANDLER ---------- */
    BackHandler { onBackClick() }

    /* ---------- STATES ---------- */
    var imageScale by remember { mutableStateOf(0.75f) } // 👈 default smaller
    var imageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var opacity by remember { mutableStateOf(0.5f) }

    var isLocked by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var flashOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        /* ================= CENTER IMAGE ================= */
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

        /* ================= TOP BAR (SAFE AREA FIXED) ================= */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars) // 👈 FIX STATUS BAR
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            /* Back */
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.back_arrow),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            /* Right Icons */
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TopCircleIcon(
                    icon = R.drawable.lock,
                    active = isLocked
                ) { isLocked = !isLocked }

                TopCircleIcon(
                    icon = R.drawable.camera_ic,
                    active = flashOn
                ) { flashOn = !flashOn }
            }
        }

        /* ================= BOTTOM PANEL ================= */
        if (!isFullscreen) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {

                /* ----- SLIDER ----- */
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
                            inactiveTrackColor = Color(0xFF2979FF).copy(alpha = 0.3f)
                        )
                    )

                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                /* ----- 4 ICONS ----- */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    BottomCircleIcon(R.drawable.camera_ic)

                    BottomCircleIcon(R.drawable.camera_ic)

                    BottomCircleIcon(R.drawable.camera_ic) {
                        imageScale = 0.75f
                        imageOffset = androidx.compose.ui.geometry.Offset.Zero
                    }

                    BottomCircleIcon(R.drawable.full_screen) {
                        isFullscreen = true
                    }
                }
            }
        }
    }
}

/* ================= REUSABLE UI ================= */

@Composable
private fun TopCircleIcon(
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
private fun BottomCircleIcon(
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
