package com.example.ardrawing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R

enum class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val label: String
) {
    Home("home", R.drawable.home_nav_ic, "Home"),
    Lesson("lesson_list", R.drawable.lesson_nav_ic, "Lesson"),
    ARText("ar_text", R.drawable.text_nav_ic, "AR Text"),
    My("my_creative", R.drawable.me_nav_ic, "My")
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.values().forEach { item ->
                BottomNavItem(
                    item = item,
                    isSelected = currentRoute == item.route || 
                                 (item == BottomNavItem.Home && currentRoute == null) ||
                                 (item == BottomNavItem.Home && currentRoute == "home"),
                    onClick = { onItemClick(item.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color(0xFFE3F2FD) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(
                    if (isSelected) Color(0xFF2196F3) else Color(0xFF757575)
                ),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF2196F3) else Color(0xFF757575)
        )
    }
}

