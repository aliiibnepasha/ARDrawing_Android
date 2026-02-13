package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ardrawing.R
import com.example.ardrawing.data.local.entity.SavedDrawing
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel
import com.example.ardrawing.ui.components.WaterWaveBackground
import com.example.ardrawing.ui.components.ProfileHeader
import com.example.ardrawing.utils.GalleryUtils
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import java.io.File

@Composable
fun MyCreativeScreen(
    viewModel: MyCreativeViewModel = viewModel(),
    onBackClick: () -> Unit,
    onDrawingClick: (SavedDrawing) -> Unit = {},
    onSeeAllAlbumClick: () -> Unit = {},
    onSelectLanguageClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onManageSubscriptionClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Refresh stats when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshStats(context)
    }

    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // ... (background animation)

        // 2. Foreground Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ProfileHeader
            ProfileHeader(
                avatarRes = R.drawable.home_avtr,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {

                // Stats Row
                StatsRow(
                    drawnCount = uiState.drawnCount,
                    latestTime = uiState.latestDrawnTime,
                    lessonsCount = uiState.lessonsCount
                )

                Spacer(modifier = Modifier.height(24.dp))

                // My Album Section
                val galleryLauncher = GalleryUtils.rememberGalleryLauncher { uri ->
                    if (uri != null) {
                        viewModel.addUploadedImage(context, uri)
                    }
                }

                MyAlbumSection(
                    uploadedImages = uiState.uploadedImages,
                    onSeeAllClick = onSeeAllAlbumClick,
                    onUploadClick = { GalleryUtils.openGallery(galleryLauncher) },
                    onImageClick = { /* Will be handled in See All screen */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // More Section
                MoreSection(
                    onSelectLanguageClick = onSelectLanguageClick,
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    onManageSubscriptionClick = onManageSubscriptionClick
                )

                // Add extra spacing at the bottom to avoid nav bar overlap
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }


}
@Composable
fun StatsRow(
    drawnCount: Int,
    latestTime: String,
    lessonsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            cornerColor = Color(0xFF8E44AD),
            icon = R.drawable.blue_brush,
            title = "Drawn",
            value = String.format("%02d", drawnCount)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            cornerColor = Color(0xFFF5B041),
            icon = R.drawable.clock,
            title = "Time",
            value = latestTime
        )

        StatCard(
            modifier = Modifier.weight(1f),
            cornerColor = Color(0xFF7DCEA0),
            icon = R.drawable.teacher_blue,
            title = "Lessons",
            value = String.format("%02d", lessonsCount)
        )
    }
}


@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    cornerColor: Color,
    icon: Int,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        // 🔺 Straight Diagonal Triangle (Top Left)
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.8f, 0f) // Point on Top Edge
                lineTo(0f, size.height * 0.65f) // Point on Left Edge
                close()
            }
            drawPath(path, cornerColor)
        }

        // 🔹 Icon - Positioned in the Top-Left Triangle
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp, start = 12.dp)
                .size(28.dp)
        )

        // 🔹 Text Content - Aligned straight and clear at the bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF), // Gray-400
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827) // Gray-900
            )
        }
    }
}



@Composable
fun MyAlbumSection(
    uploadedImages: List<String>,
    onSeeAllClick: () -> Unit,
    onUploadClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Album",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            if (uploadedImages.isNotEmpty()) {
                Text(
                    text = "See All",
                    fontSize = 14.sp,
                    color = colorResource(R.color.app_blue),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Card with Dashed Border
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0F4FF))
                    .clickable { onUploadClick() }
                    .drawDashedBorder(
                        color = colorResource(R.color.app_blue),
                        strokeWidth = 4.dp,
                        cornerRadius = 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                        Icon(
                            painter = painterResource(R.drawable.add_illustration),
                            contentDescription = "Upload",
                            tint = colorResource(R.color.app_blue),
                            modifier = Modifier.size(30.dp)
                        )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Upload a photo\nof your drawing",
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            
            // Show uploaded images or placeholder
            if (uploadedImages.isNotEmpty()) {
                // Show ONLY the latest uploaded image (at index 0)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { onImageClick(uploadedImages[0]) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (uploadedImages[0].startsWith("/")) File(uploadedImages[0]) else Uri.parse(uploadedImages[0]))
                            .build(),
                        contentDescription = "Uploaded Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Placeholder when no images
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

// Extension function for dashed border
fun Modifier.drawDashedBorder(
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    dashLength: androidx.compose.ui.unit.Dp = 5.dp,
    gapLength: androidx.compose.ui.unit.Dp = 4.dp
) = drawBehind {
    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
        width = strokeWidth.toPx(),
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
    )
}

@Composable
fun MoreSection(
    onSelectLanguageClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onManageSubscriptionClick: () -> Unit
) {
    Column {
        Text(
            text = "More",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Single Container for all items
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                MoreItem(
                    text = "Select Language",
                    onClick = onSelectLanguageClick
                )
                
                MoreItem(
                    text = "Manage subscription",
                    onClick = onManageSubscriptionClick
                )
                
                MoreItem(
                    text = "Privacy Policy",
                    onClick = onPrivacyPolicyClick
                )

                Spacer(modifier = Modifier.height(6.dp))

            }
        }
    }
}

@Composable
fun MoreItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF4DA3FF)
        )
    }
}
