package com.example.ardrawing.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.ardrawing.R
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.utils.ARCorePreferences
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawingModeSelectionScreen(
    template: DrawingTemplate?,
    lesson: Lesson?,
    onBackClick: () -> Unit,
    onDrawSketchClick: () -> Unit,
    onTraceImageClick: () -> Unit,
    onStartAR: () -> Unit = {}
) {
    val context = LocalContext.current
    val titleName = template?.name ?: lesson?.name ?: "Your Image"
    
    // Check if ARCore is supported on this device
    val isARCoreSupported = remember {
        // Default to false if not checked yet (safe assumption)
        ARCorePreferences.isARCoreSupported(context) ?: false
    }
    
    // Filter modes based on ARCore support - hide AR mode if not supported
    val allModes = listOf(
        DrawingMode(
            id = "ar",
            title = "Draw with AR",
            description = "Use a tripod, a glass, or a stack of books to hold your phone in place while you draw"
        ),
        DrawingMode(
            id = "camera",
            title = "Draw with camera",
            description = "Use a tripod, a glass, or a stack of books to hold your phone in place while you draw"
        ),
        DrawingMode(
            id = "screen",
            title = "Draw with screen",
            description = "Put a paper over your phone's screen and follow the lines to trace your image with ease"
        )
    )

    // Filter out AR mode if device doesn't support ARCore
    val modes = remember(isARCoreSupported) {
        if (isARCoreSupported) {
            allModes
        } else {
            allModes.filter { it.id != "ar" }
        }
    }
    
    // Set initial page - if AR was removed, start at camera (index 0 or 1 depending on removal)
    val initialPage = remember(isARCoreSupported) {
        if (isARCoreSupported) {
            1 // Camera mode (middle)
        } else {
            0 // Camera mode (first after AR removal)
        }
    }
    
    val pagerState = rememberPagerState(initialPage = initialPage) { modes.size }
    val scope = rememberCoroutineScope()
    var selectedModeId by remember { mutableStateOf("camera") }

    // Sync selection with pager scroll
    LaunchedEffect(pagerState.currentPage) {
        selectedModeId = modes[pagerState.currentPage].id
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars) // Fix overlap
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.back_arrow_ic),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onBackClick)
                    )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    when (selectedModeId) {
                        "ar" -> onStartAR()
                        "camera" -> onDrawSketchClick()
                        "screen" -> onTraceImageClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4)
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select the drawing mode",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1C),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 80.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                val mode = modes[page]
                val isSelected = mode.id == selectedModeId
                
                // Parallax/Scale effect
                val pageOffset = (
                    (pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                ).absoluteValue

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // Scale the center item up, others down
                            val scale = lerp(
                                start = 0.85f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    DrawingModeCard(
                        mode = mode,
                        isSelected = isSelected,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingModeCard(
    mode: DrawingMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF4285F4) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Image Container with Radio/Check
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Background Pattern (Checkered)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF0F0F0)) // Placeholder gray
                ) {
                    // Start of placeholder grid pattern
                   // In a real app, this would be the transparent png background
                   // For now, simple gray is fine as per plan
                }

                // Selection Indicator
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                        .background(
                            color = if (isSelected) Color(0xFF4285F4) else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) Color.Transparent else Color(0xFF4285F4),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    text = mode.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                if (isSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mode.description,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

data class DrawingMode(
    val id: String,
    val title: String,
    val description: String
)
