package com.example.ardrawing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                .background(Color(0xFFF3F4EF))
        ) {

            // ===== TOP SELECTION INDICATOR ROW =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TopIndicator(selected = currentRoute == "home")
                TopIndicator(selected = currentRoute == "lesson_list")
                Spacer(modifier = Modifier.width(48.dp)) // FAB space
                TopIndicator(selected = currentRoute == "ar_text")
                TopIndicator(selected = currentRoute == "my_creative")
            }

            // ===== ICON ROW =====
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconItem(
                    icon = R.drawable.home_nav_ic,
                    onClick = { onItemClick("home") }
                )

                IconItem(
                    icon = R.drawable.lesson_nav_ic,
                    onClick = { onItemClick("lesson_list") }
                )

                Spacer(modifier = Modifier.width(48.dp))

                IconItem(
                    icon = R.drawable.text_nav_ic,
                    onClick = { onItemClick("ar_text") }
                )

                IconItem(
                    icon = R.drawable.me_nav_ic,
                    onClick = { onItemClick("my_creative") }
                )
            }
        }

        // ===== CENTER FAB =====
        Box(
            modifier = Modifier
                .offset(y = (-30).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.magic_nav_ic),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
@Composable
private fun IconItem(
    icon: Int,
    onClick: () -> Unit
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = Color(0xFF2C2C2C),
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

