package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit = {},
    onRateClick: () -> Unit = {},
    onTryProClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onMoreAppsClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Profile",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Go Premium Section
            item {
                PremiumCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }

            // First Group: Language, Rate, Try Pro
            item {
                SettingsItem(
                    icon = null, // Icon will be added later
                    title = "Language",
                    subtitle = "Default",
                    onClick = onLanguageClick,
                    showArrow = false
                )
            }
            
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }
            
            item {
                SettingsItem(
                    icon = null,
                    title = "Rate",
                    onClick = onRateClick,
                    showArrow = false
                )
            }
            
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }
            
            item {
                SettingsItem(
                    icon = null,
                    title = "Try full pro version",
                    onClick = onTryProClick,
                    showArrow = true
                )
            }

            // Spacing between groups
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Second Group: Help, Share, Feedback, More apps
            item {
                SettingsItem(
                    icon = null,
                    title = "Help",
                    onClick = onHelpClick,
                    showArrow = false
                )
            }
            
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }
            
            item {
                SettingsItem(
                    icon = null,
                    title = "Share",
                    onClick = onShareClick,
                    showArrow = false
                )
            }
            
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }
            
            item {
                SettingsItem(
                    icon = null,
                    title = "Feedback",
                    onClick = onFeedbackClick,
                    showArrow = false
                )
            }
            
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }
            
            item {
                SettingsItem(
                    icon = null,
                    title = "More apps",
                    onClick = onMoreAppsClick,
                    showArrow = false
                )
            }
        }
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.card_bg)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Go Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Get exclusive tools for smooth and accurate drawing",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            
            Spacer(modifier = Modifier.width(6.dp))
            
            // Placeholder for 3D icon (will be added later)
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.premium_ic),
                    contentDescription = "icon",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector?,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showArrow: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.card_bg)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder (will be added later)
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // Placeholder box for icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            if (showArrow) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

