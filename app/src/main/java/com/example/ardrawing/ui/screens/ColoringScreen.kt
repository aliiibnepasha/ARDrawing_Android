package com.example.ardrawing.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.components.ColoringCanvas
import com.example.ardrawing.ui.components.ColorPaletteBar
import com.example.ardrawing.ui.components.ColorPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoringScreen(
    template: DrawingTemplate,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedColor by remember { mutableStateOf(Color(0xFF9B59B6)) } // Default to purple
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    
    // Load bitmap from assets
    LaunchedEffect(template) {
        val bitmap = withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.assets.open(template.imageAssetPath)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                null
            }
        }
        originalBitmap = bitmap
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Coloring",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ColorPaletteBar(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                onOpenPicker = { showColorPicker = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (originalBitmap == null) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                originalBitmap?.let { bitmap ->
                    ColoringCanvas(
                        originalBitmap = bitmap,
                        selectedColor = selectedColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = {
                selectedColor = it
                showColorPicker = false
            }
        )
    }
}

