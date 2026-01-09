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
                .clip(RoundedCornerShape(36.dp))
                .background(colorResource(R.color.white))
        ) {

            // ===== TOP SELECTION INDICATOR ROW =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TopIndicator(selected = currentRoute == "home")
                TopIndicator(selected = currentRoute == "lesson_list")
                TopIndicator(selected = currentRoute == "ar_text")
                TopIndicator(selected = currentRoute == "my_creative")
            }

            // ===== ICON ROW =====
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconItem(
                    icon = R.drawable.home_nav_ic,
                    selected = currentRoute == "home",
                    onClick = { onItemClick("home") }
                )

                IconItem(
                    icon = R.drawable.lesson_nav_ic,
                    selected = currentRoute == "lesson_list",
                    onClick = { onItemClick("lesson_list") }
                )

                IconItem(
                    icon = R.drawable.text_nav_ic,
                    selected = currentRoute == "ar_text",
                    onClick = { onItemClick("ar_text") }
                )

                IconItem(
                    icon = R.drawable.me_nav_ic,
                    selected = currentRoute == "my_creative",
                    onClick = { onItemClick("my_creative") }
                )
            }
        }
    }
}
@Composable
private fun IconItem(
    icon: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = if (selected) Color(0xFF5E8BFF) else Color(0xFF2C2C2C),
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick() }
    )
}
@Composable
private fun TopIndicator(selected: Boolean) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (selected) Color(0xFF5E8BFF) else Color.Transparent
            )
    )
}

