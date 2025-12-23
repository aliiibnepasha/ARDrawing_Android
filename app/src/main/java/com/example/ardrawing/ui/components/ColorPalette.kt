package com.example.ardrawing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

/**
 * Predefined color palette for coloring
 */
object ColorPalette {
    val colors = listOf(
        Color(0xFFE8E8E8), // Light Gray
        Color(0xFF9B59B6), // Purple
        Color(0xFF16A085), // Teal
        Color(0xFFF39C12), // Orange
        Color(0xFFE74C3C), // Red
        Color(0xFF34495E), // Dark Gray
        Color(0xFF000000), // Black
    )
}

@Composable
fun ColorPaletteBar(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    onOpenPicker: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ColorWheelButton(
                    onClick = onOpenPicker
                )
            }
            items(ColorPalette.colors) { color ->
                ColorCircle(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.Black,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Empty content - Box is used for layout and styling only
    }
}

@Composable
private fun ColorWheelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
            .background(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ColorLens,
            contentDescription = "Pick Color",
            tint = Color.White
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val controller: ColorPickerController = rememberColorPickerController()
    val pickedColor = remember { mutableStateOf(initialColor) }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pick a color",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .size(280.dp),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { envelope: ColorEnvelope ->
                        pickedColor.value = envelope.color
                    }
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    controller = controller
                )
                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    controller = controller
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(pickedColor.value)
                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Cancel",
                        color = Color.Gray,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                    Text(
                        text = "Apply",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.clickable {
                            onColorSelected(pickedColor.value)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

