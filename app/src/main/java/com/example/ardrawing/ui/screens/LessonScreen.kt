package com.example.ardrawing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R

data class LessonItem(
    val title: String,
    val subtitle: String,
    val category: String
)

@Composable
fun LessonScreen() {

    val tabs = listOf("All", "Anime", "Animals", "Nature")
    var selectedTab by remember { mutableStateOf("Animals") }

    // Sample lesson items
    val allLessons = remember {
        listOf(
            LessonItem("Cute Dog", "15 Steps. Animals", "Animals"),
            LessonItem("Beautiful Cow", "12 Steps. Animals", "Animals"),
            LessonItem("Parrot", "9 Steps. Animals", "Animals"),
            LessonItem("Naruto", "20 Steps. Anime", "Anime"),
            LessonItem("Goku", "18 Steps. Anime", "Anime"),
            LessonItem("Pikachu", "14 Steps. Anime", "Anime"),
            LessonItem("Mountain View", "16 Steps. Nature", "Nature"),
            LessonItem("Sunset", "10 Steps. Nature", "Nature"),
            LessonItem("Forest", "13 Steps. Nature", "Nature")
        )
    }

    // Filter lessons based on selected tab
    val filteredLessons = remember(selectedTab) {
        if (selectedTab == "All") {
            allLessons
        } else {
            allLessons.filter { it.category == selectedTab }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            /* ---------------- HEADER ---------------- */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lesson Step By Step",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select The Text For Your AR Journey",
                            fontSize = 13.sp,
                            color = Color(0xFF7A7A7A)
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.me_nav_ic),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(8.dp)
                    )
                }
            }

            /* ---------------- FILTER TABS ---------------- */
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tabs) { tab ->
                        FilterChip(
                            text = tab,
                            selected = tab == selectedTab
                        ) {
                            selectedTab = tab
                        }
                    }
                }
            }

            /* ---------------- FEATURED CARDS (HORIZONTAL LIST) ---------------- */
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredLessons) { lesson ->
                        FeaturedCard(
                            title = lesson.title,
                            subtitle = lesson.subtitle
                        )
                    }
                }
            }

            /* ---------------- TRENDING ---------------- */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending Sketches",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See all",
                        fontSize = 13.sp,
                        color = Color(0xFF5E8BFF),
                        modifier = Modifier.clickable { }
                    )
                }
            }

            items(2) {
                TrendingItem(
                    title = if (it == 0) "Beautiful Cow" else "Parrot",
                    subtitle = "9 Steps. Animals"
                )
            }
        }

    }
}

/* ================= COMPONENTS ================= */

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Color(0xFF5E8BFF) else Color.White
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StartButton() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF5E8BFF))
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Start",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun FeaturedCard(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEFF4FF))
            ) {
                // IMAGE PLACEHOLDER
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                StartButton()
            }
        }
    }
}

@Composable
private fun TrendingItem(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF4FF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        StartButton()
    }
}
