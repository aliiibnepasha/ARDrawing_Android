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

            // 3. First Lesson Item
            item {
                LessonItemCard(
                    imageRes = R.drawable.lesson_boy,
                    level = 3,
                    steps = 8
                )
            }

            // 4. Course Banner
            item { CourseBanner() }

            // 5. More Lesson Items
            item {
                LessonItemCard(
                    imageRes = R.drawable.lesson_boy,
                    level = 2,
                    steps = 8
                )
            }

            item {
                LessonItemCard(
                    imageRes = R.drawable.lesson_boy,
                    level = 6,
                    steps = 12
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
            .height(110.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White) // Base white
    ) {
        // Custom Drawn Green Background (Right Side with Diagonal)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path()
                    val startX = size.width * 0.28f // Top-left of green roughly
                    val bottomStartX = size.width * 0.20f // Bottom-left of green (diagonal slant)
                    
                    path.moveTo(startX, 0f)
                    path.lineTo(size.width, 0f)
                    path.lineTo(size.width, size.height)
                    path.lineTo(bottomStartX, size.height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8FD866), // Light Green
                                Color(0xFF67C240)  // Darker Green
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
            // Fox Image (Left side on White)
            Box(
                modifier = Modifier
                    .weight(0.32f) // Adjust weight to match graphic split
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lesson_fox),
                    contentDescription = null,
                    modifier = Modifier.size(75.dp) // Fox size
                )
            }

            // Info Column (Right side on Green)
            Column(
                modifier = Modifier
                    .weight(0.68f)
                    .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Free Lesson",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Timer Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TimerBox("07")
                        Text(":", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                        TimerBox("20")
                        Text(":", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                        TimerBox("48")
                    }

                    // Start Button
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4) // Blue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier
                            .width(80.dp)
                            .height(34.dp)
                    ) {
                        Text("Start", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
            .size(width = 24.dp, height = 24.dp)
            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LessonItemCard(
    imageRes: Int,
    level: Int, // 1 to 10
    steps: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Taller card as per ref
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            
            // LEFT SIDE: Image + Heart
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                // Image
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 8.dp) // Space for heart
                )

                // Heart Icon (Top Right of THIS box)
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clickable { }
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.8f) // Not full height
                    .align(Alignment.CenterVertically)
                    .background(Color(0xFFEEEEEE))
            )

            // RIGHT SIDE: Details
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                
                // Level Section
                Text(
                    text = "Level:",
                    fontSize = 15.sp,
                    color = Color(0xFF5A5A5A),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Level Bars
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index < level) Color(0xFF4285F4) else Color(0xFFD3E3FD)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Steps Section
                Text(
                    text = "Steps:",
                    fontSize = 15.sp,
                    color = Color(0xFF5A5A5A),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$steps",
                    fontSize = 20.sp,
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
            .height(95.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White) // Base white
    ) {
         // Custom Drawn Blue Background (Right Side with Diagonal)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path()
                    val startX = size.width * 0.35f // Top-left of blue
                    val bottomStartX = size.width * 0.25f // Bottom-left of blue
                    
                    path.moveTo(startX, 0f)
                    path.lineTo(size.width, 0f)
                    path.lineTo(size.width, size.height)
                    path.lineTo(bottomStartX, size.height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF559DFD), // Blue
                                Color(0xFF559DFD)  // Blue
                            )
                        )
                    )
                }
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content (Image on White)
            Box(
                 modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                 Image(
                    painter = painterResource(id = R.drawable.lesson_course_thumb),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(90.dp)
                        .height(60.dp)
                        // .clip(RoundedCornerShape(8.dp)) // If needed
                )
            }
           
            // Center Text (On Blue)
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .padding(start = 24.dp) // Push text right to clear diagonal
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF9AB26)) // Orange Pill
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "New lesson available",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "7-day course",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Arrow (Right edge)
            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
