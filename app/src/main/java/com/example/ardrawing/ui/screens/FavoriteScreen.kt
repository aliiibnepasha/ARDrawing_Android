package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.ui.components.WaterWaveBackground

@Composable
fun FavoriteScreen() {
    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()
        
        // 2. Foreground Content
        Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
            topBar = {
                FavoriteHeader()
            }
        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sad Face Icon
                Icon(
                    imageVector = Icons.Outlined.SentimentDissatisfied,
                    contentDescription = null,
                    tint = Color(0xFFA0C1F8), // Light blue tint
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Favorites Added",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6F7E95) // Grey text
                )
            }
        }
        }
    }
}

@Composable
fun FavoriteHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_avtr),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
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
