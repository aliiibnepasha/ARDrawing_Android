package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.ui.components.WaterWaveBackground

@Composable
fun CustomTextScreen(
    onBackClick: () -> Unit,
    onDrawClick: () -> Unit,
    onDoneClick: (String, FontItem) -> Unit
) {
    var selectedFontIndex by remember { mutableStateOf(0) }
    var customText by remember { mutableStateOf("XYXZSS") }
    // Mock fonts using system font families for now
    val fonts = listOf(
        FontItem("Augmented\nReality", FontFamily.Serif, FontWeight.Bold, FontStyle.Normal),
        FontItem("AUGMENTED\nREALITY", FontFamily.SansSerif, FontWeight.Black, FontStyle.Italic),
        FontItem("Augmented\nReality", FontFamily.Cursive, FontWeight.Normal, FontStyle.Normal),
        FontItem("Augmented\nReality", FontFamily.Monospace, FontWeight.Bold, FontStyle.Normal),
        FontItem("Augmented\nReality", FontFamily.Default, FontWeight.ExtraBold, FontStyle.Italic),
        FontItem("Augmented\nReality", FontFamily.Serif, FontWeight.Light, FontStyle.Italic)
    )

    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()
        
        // 2. Foreground Content
    Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
        topBar = {
            CustomTextTopBar(
                onBackClick = onBackClick,
                onDoneClick = { 
                    onDoneClick(customText, fonts[selectedFontIndex])
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onDrawClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.app_blue)
                )
            ) {
                Text(
                    text = "Draw",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Dashed Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FBFF))
                    .dashedBorder(colorResource(R.color.app_blue), 20.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = fonts[selectedFontIndex].weight,
                    fontFamily = fonts[selectedFontIndex].family,
                    fontStyle = fonts[selectedFontIndex].style,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = false,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Select Font",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Font Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(fonts.size) { index ->
                    val item = fonts[index]
                    val isSelected = selectedFontIndex == index
                    
                    Box(
                        modifier = Modifier
                            .height(80.dp) // Aspect ratio roughly
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White else Color(0xFFF9F9F9))
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) colorResource(R.color.app_blue) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedFontIndex = index }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.previewText,
                            fontSize = 12.sp,
                            fontWeight = item.weight,
                            fontFamily = item.family,
                            fontStyle = item.style,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            lineHeight = 16.sp
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTextTopBar(
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Image(
            painter = painterResource(R.drawable.back_arrow_ic),
            contentDescription = "Back",
            modifier = Modifier
                .size(32.dp)
                .clickable { onBackClick() }
            )

        // Centered Title
        Text(
            text = "Custom Text",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Done Button
        Text(
            text = "Done",
            color = colorResource(R.color.app_blue),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.clickable { 
                onDoneClick()
            }
        )
    }
}

// Helper for dashed border
fun Modifier.dashedBorder(color: Color, cornerRadius: androidx.compose.ui.unit.Dp) = drawBehind {
    val stroke = Stroke(
        width = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
    )
}

data class FontItem(
    val previewText: String,
    val family: FontFamily,
    val weight: FontWeight,
    val style: FontStyle
)
