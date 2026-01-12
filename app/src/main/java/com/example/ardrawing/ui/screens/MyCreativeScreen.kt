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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ardrawing.R
import com.example.ardrawing.data.local.entity.SavedDrawing
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel

@Composable
fun MyCreativeScreen(
    viewModel: MyCreativeViewModel = viewModel(),
    onBackClick: () -> Unit,
    onDrawingClick: (SavedDrawing) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Section
            ProfileHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Row
            StatsRow()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // My Album Section
            MyAlbumSection(
                onSeeAllClick = { /* TODO */ },
                onUploadClick = { /* TODO */ },
                onDrawingClick = {}
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // More Section
            MoreSection(
                onPrivacyPolicyClick = { /* TODO */ },
                onManageSubscriptionClick = { /* TODO */ }
            )
            
            // Add extra spacing at the bottom to avoid nav bar overlap
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProfileHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_avtr),
            contentDescription = "Profile Avatar",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "Welcome to",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Augmented Reality",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            cornerColor = Color(0xFF8E44AD),
            icon = Icons.Default.Edit,
            title = "Drawn",
            value = "02"
        )

        StatCard(
            modifier = Modifier.weight(1f),
            cornerColor = Color(0xFFF5B041),
            icon = Icons.Default.AccessTime,
            title = "Time",
            value = "22s"
        )

        StatCard(
            modifier = Modifier.weight(1f),
            cornerColor = Color(0xFF7DCEA0),
            icon = Icons.Default.School,
            title = "Lessons",
            value = "02"
        )
    }
}


@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    cornerColor: Color,
    icon: ImageVector,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .height(170.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
            .clip(RoundedCornerShape(22.dp))
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
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 18.dp, start = 18.dp)
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827) // Gray-900
            )
        }
    }
}



@Composable
fun MyAlbumSection(
    onSeeAllClick: () -> Unit,
    onUploadClick: () -> Unit,
    onDrawingClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Album",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "See All",
                fontSize = 14.sp,
                color = Color(0xFF4285F4),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
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
                        color = Color(0xFF4285F4),
                        strokeWidth = 4.dp, // Thicker border
                        cornerRadius = 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFE0E9FC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Upload",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                     Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Upload a photo\nof your drawing",
                        fontSize = 13.sp,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            
            // Example Drawing Card (Placeholder)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable { onDrawingClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                 Box(
                   modifier = Modifier.fillMaxSize(),
                   contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit, // Placeholder
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
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
    onPrivacyPolicyClick: () -> Unit,
    onManageSubscriptionClick: () -> Unit
) {
    Column {
        Text(
            text = "More",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        MoreItem(
            text = "Privacy Policy",
            onClick = onPrivacyPolicyClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        MoreItem(
            text = "Manage subscription",
            onClick = onManageSubscriptionClick
        )
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
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() }
            .padding(16.dp),
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
            tint = Color.Gray
        )
    }
}
