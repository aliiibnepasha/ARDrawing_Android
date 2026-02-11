package com.example.ardrawing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R

@Composable
fun OpacityAndZoomControls(
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    imageScale: Float,
    onScaleChange: (Float) -> Unit,
    onGalleryClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val iconTint = if (isDarkTheme) Color.White else Color.Black
    val sliderThumbColor = colorResource(R.color.app_blue)
    val sliderActiveTrackColor = colorResource(R.color.app_blue) // Always blue per request?
    
    // Slider inactive color
    val sliderInactiveColor = if (isDarkTheme) Color.White else Color.Black.copy(alpha = 0.1f) 
    // Wait, typical material slider inactive is track color.
    // User image shows blue active, white inactive for Camera (Dark bg).
    // For Paper (Light bg), image shows blue active, black/grey inactive.
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Opacity",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // Slider Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.mask),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Slider(
                value = opacity,
                onValueChange = onOpacityChange,
                modifier = Modifier
                    .weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = sliderThumbColor,
                    activeTrackColor = sliderActiveTrackColor,
                    inactiveTrackColor = sliderInactiveColor
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "${(opacity * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.width(36.dp) // Fixed width for alignment
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Gallery Icon
             IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(24.dp)
            ) {
                 Icon(
                    painter = painterResource(R.drawable.galllery),
                    contentDescription = "Gallery",
                    tint = iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // Zoom Segmented Control
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ZoomSegmentedControl(
                currentScale = imageScale,
                onScaleChange = onScaleChange,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun ZoomSegmentedControl(
    currentScale: Float,
    onScaleChange: (Float) -> Unit,
    isDarkTheme: Boolean
) {
    // Styles based on User Request
    // Camera (isDarkTheme = true): Container White, Selected Blue
    // Paper (isDarkTheme = false): Container #0F172A, Selected White

    val containerColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val selectedItemColor = if (isDarkTheme) colorResource(R.color.app_blue) else Color.White
    val unselectedTextColor = if (isDarkTheme) Color.Black else Color.White
    val selectedTextColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)

    val scales = listOf(0.5f, 1.0f, 2.0f)
    
    // Container
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(100)) // Pill shape
            .background(containerColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
         scales.forEach { scale ->
             val isSelected = scale == currentScale
             val text = "${scale}x"
             
             Box(
                 modifier = Modifier
                     .height(28.dp)
                     .width(48.dp)
                     .clip(RoundedCornerShape(100))
                     .background(if (isSelected) selectedItemColor else Color.Transparent)
                     .clickable { onScaleChange(scale) },
                 contentAlignment = Alignment.Center
             ) {
                 Text(
                     text = text,
                     fontSize = 12.sp,
                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                     color = if (isSelected) selectedTextColor else unselectedTextColor
                 )
             }
         }
    }
}
