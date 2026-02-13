package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.ui.components.ProfileHeader
import com.example.ardrawing.ui.components.WaterWaveBackground
import com.example.ardrawing.ui.theme.Poppins
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle

@Composable
fun LessonScreen(
    onLessonClick: (String) -> Unit = {}
) {
    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // 1. Background Animation
        WaterWaveBackground()

        // 2. Foreground Content
        Column(modifier = Modifier.fillMaxSize()) {
            // ProfileHeader
            ProfileHeader(
                avatarRes = R.drawable.home_avtr,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 0.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. Free Lesson Card
                item { FreeLessonCard() }

                // 3. First Lesson Item (Lesson 1 - Full Image)
                item {
                    LessonItemCard(
                        imagePath = "file:///android_asset/lessons/lesson_1/step_17.svg",
                        level = 3,
                        steps = 17,
                        onClick = { onLessonClick("lesson_1") }
                    )
                }

                // 4. Course Banner
                item { CourseBanner() }

                // 5. More Lesson Items
                item {
                    LessonItemCard(
                        imagePath = "file:///android_asset/lessons/lesson_2/Step_13.svg",
                        level = 2,
                        steps = 13,
                        onClick = { onLessonClick("lesson_2") }
                    )
                }

                item {
                    LessonItemCard(
                        imagePath = "file:///android_asset/lessons/lesson_3/Step_8.svg",
                        level = 1,
                        steps = 8,
                        onClick = { onLessonClick("lesson_3") }
                    )
                }
            }
        }
    }
}

// ---------------- COMPONENTS ----------------
// ---------------- COMPONENTS ----------------
// LessonHeader replaced by Shared ProfileHeader

@Composable
private fun FreeLessonCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
    ) {
        // Custom Drawn Green Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path()
                    val startX = size.width * 0.32f
                    val bottomStartX = size.width * 0.22f

                    path.moveTo(startX, 0f)
                    path.lineTo(size.width, 0f)
                    path.lineTo(size.width, size.height)
                    path.lineTo(bottomStartX, size.height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF90D967),
                                Color(0xFF82D45D)
                            )
                        )
                    )
                }
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Area
            Image(
                painter = painterResource(id = R.drawable.lesson_cat),
                contentDescription = null,
                modifier = Modifier
                    .size(68.dp)
            )

            Spacer(modifier = Modifier.width(22.dp))

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Free Lesson",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    style = TextStyle(
                        fontFamily = Poppins,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Timer Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimerBox("07")
                    Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    TimerBox("20")
                    Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    TimerBox("48")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Start Button (Direct child of Row for perfect vertical centering)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorResource(R.color.app_blue))
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontFamily = Poppins,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Composable
private fun TimerBox(time: String) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 24.dp)
            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                fontFamily = Poppins,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@Composable
private fun LessonItemCard(
    imagePath: String,
    level: Int,
    steps: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // LEFT SIDE: Image + Heart
            Box(
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                // Image (Loaded from Assets via Coil)
                val context = androidx.compose.ui.platform.LocalContext.current
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(imagePath)
                        .decoderFactory(coil.decode.SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                )

                // Heart Icon
                Icon(
                    painter = painterResource(R.drawable.my_fav_unfill),
                    contentDescription = null,
                    tint = colorResource(R.color.app_blue),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.7f)
                    .align(Alignment.CenterVertically)
                    .background(Color(0xFFF1F5F9))
            )

            // RIGHT SIDE: Details
            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight()
                    .padding(start = 20.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {

                // Level Section
                Text(
                    text = "Level:",
                    fontSize = 15.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(
                        fontFamily = Poppins,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Level Bars
                Row(horizontalArrangement = Arrangement.spacedBy(4.5.dp)) {
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index < level) colorResource(R.color.app_blue) else Color(0xFFE2EDFF)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Steps Section
                Text(
                    text = "Steps:",
                    fontSize = 15.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(
                        fontFamily = Poppins,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$steps",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.app_blue),
                    style = TextStyle(
                        fontFamily = Poppins,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Composable
private fun CourseBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
    ) {
         // Custom Drawn Blue Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path()
                    val startX = size.width * 0.38f
                    val bottomStartX = size.width * 0.27f

                    path.moveTo(startX, 0f)
                    path.lineTo(size.width, 0f)
                    path.lineTo(size.width, size.height)
                    path.lineTo(bottomStartX, size.height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF5AB4FF),
                                Color(0xFF4DA3FF)
                            )
                        )
                    )
                }
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content (Image)
            Box(
                 modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                 Image(
                    painter = painterResource(id = R.drawable.lesson_girls),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .offset(x = (-4).dp)
                )
            }

            // Center Text
            Column(
                modifier = Modifier
                    .weight(0.53f)
                    .padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFB81F))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "New lesson available",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            fontFamily = Poppins,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "7-day course",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(
                        fontFamily = Poppins,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }

            // Arrow
            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.move_forward),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
