package com.example.ardrawing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ardrawing.R

@Composable
fun ARFloatingBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // ✅ FIX 1
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        // ===== PILL CONTAINER =====
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
                .background(Color.White, RoundedCornerShape(36.dp))
                .clip(RoundedCornerShape(36.dp))
        ) {
            // ===== UNITARY NAVIGATION ROW =====
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    icon = if (currentRoute == "home") R.drawable.blue_brush else R.drawable.brush,
                    selected = currentRoute == "home",
                    onClick = { onItemClick("home") }
                )

                NavBarItem(
                    icon = if (currentRoute == "lesson_list") R.drawable.teacher_blue else R.drawable.teacher,
                    selected = currentRoute == "lesson_list",
                    onClick = { onItemClick("lesson_list") }
                )

                NavBarItem(
                    icon = if (currentRoute == "favorite") R.drawable.fav_heart else R.drawable.fav_ic,
                    selected = currentRoute == "favorite",
                    onClick = { onItemClick("favorite") }
                )

                NavBarItem(
                    icon = if (currentRoute == "my_creative") R.drawable.profile_blue else R.drawable.profile,
                    selected = currentRoute == "my_creative",
                    onClick = { onItemClick("my_creative") }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(64.dp) 
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top 
    ) {
        // TOP INDICATOR
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                    if (selected) Color(0xFF4DA3FF) else Color.Transparent
                )
        )
        
        // ICON
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

