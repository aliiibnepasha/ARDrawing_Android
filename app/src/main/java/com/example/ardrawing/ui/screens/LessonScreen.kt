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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R

@Composable
fun LessonScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Header
            item { LessonHeader() }

            // 2. Free Lesson Card
            item { FreeLessonCard() }

            // 3. First Lesson Item (Lesson 1 - Full Image)
            item {
                LessonItemCard(
                    imagePath = "file:///android_asset/lessons/lesson_1/step_17.svg",
                    level = 3,
                    steps = 17
                )
            }

            // 4. Course Banner
            item { CourseBanner() }

            // 5. More Lesson Items
            item {
                LessonItemCard(
                    imagePath = "file:///android_asset/lessons/lesson_2/Step_13.svg",
                    level = 2,
                    steps = 13
                )
            }

            item {
                LessonItemCard(
                    imagePath = "file:///android_asset/lessons/lesson_3/Step_8.svg",
                    level = 1,
                    steps = 8
                )
            }
        }
    }
}

// ---------------- COMPONENTS ----------------

@Composable
private fun LessonHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Image(
            painter = painterResource(id = R.drawable.home_avtr),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Column {
            Text(
                text = "Welcome to",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Augmented Reality",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun FreeLessonCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // Adjusted height for balance
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
    ) {
        // Custom Drawn Green Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path()
                    // Shifted right to give Fox more space
                    val startX = size.width * 0.35f 
                    val bottomStartX = size.width * 0.24f
                    
                    path.moveTo(startX, 0f)
                    path.lineTo(size.width, 0f)
                    path.lineTo(size.width, size.height)
                    path.lineTo(bottomStartX, size.height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8FD866),
                                Color(0xFF67C240)
                            )
                        )
                    )
                }
        )

        // Content
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fox Image Area (Wider)
            Box(
                modifier = Modifier
                    .weight(0.36f) // Increased weight
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lesson_cat),
                    contentDescription = null,
                    modifier = Modifier
                        .size(85.dp) // Much larger fox
                        .offset(x = 4.dp) // Nudge right slightly
                )
            }

            // Info Column
            Column(
                modifier = Modifier
                    .weight(0.64f)
                    .padding(start = 4.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Free Lesson",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp, // Larger title
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Timer Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimerBox("07")
                        Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TimerBox("20")
                        Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TimerBox("48")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Start Button
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f) // Fill remaining space
                            .height(38.dp)
                    ) {
                        Text(
                            "Start", 
                            color = Color.White, 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerBox(time: String) {
    Box(
        modifier = Modifier
            .size(width = 26.dp, height = 26.dp)
            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LessonItemCard(
    imagePath: String,
    level: Int,
    steps: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp) // Reduced height from 180dp
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            
            // LEFT SIDE: Image + Heart
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .padding(10.dp)
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
                        .padding(top = 12.dp, bottom = 4.dp)
                )

                // Heart Icon
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp) // Smaller icon
                        .clickable { }
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.7f)
                    .align(Alignment.CenterVertically)
                    .background(Color(0xFFF0F0F0)) // Subtle gray
            )

            // RIGHT SIDE: Details
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight()
                    .padding(start = 20.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                
                // Level Section
                Text(
                    text = "Level:",
                    fontSize = 14.sp,
                    color = Color(0xFF5A5A5A),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Level Bars
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .width(4.dp) // Thinner bars
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index < level) Color(0xFF4285F4) else Color(0xFFD3E3FD)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Steps Section
                Text(
                    text = "Steps:",
                    fontSize = 14.sp,
                    color = Color(0xFF5A5A5A),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4)
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
            .height(82.dp) // Reduced height from 95dp
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
         // Custom Drawn Blue Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path()
                    val startX = size.width * 0.35f
                    val bottomStartX = size.width * 0.25f
                    
                    path.moveTo(startX, 0f)
                    path.lineTo(size.width, 0f)
                    path.lineTo(size.width, size.height)
                    path.lineTo(bottomStartX, size.height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF559DFD),
                                Color(0xFF559DFD)
                            )
                        )
                    )
                }
        )

        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content (Image)
            Box(
                 modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                 Image(
                    painter = painterResource(id = R.drawable.lesson_girls),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .width(80.dp)
                        .height(55.dp)
                )
            }
           
            // Center Text
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .padding(start = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF9AB26))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "New lesson available",
                        color = Color.White,
                        fontSize = 10.sp, // Reduced font size to ensure one line
                        fontWeight = FontWeight.Bold,
                        maxLines = 1 // Force single line
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "7-day course",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
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
                        .size(28.dp) // Smaller arrow circle
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
