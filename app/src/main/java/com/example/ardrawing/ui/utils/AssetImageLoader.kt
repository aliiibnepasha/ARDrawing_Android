package com.example.ardrawing.ui.utils

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File

/**
 * Creates an image painter for loading images from assets folder or file paths
 * @param imagePath The path to the image - can be asset path (e.g., "bg_remove/image1.png") or file path
 */
@Composable
fun rememberAssetImagePainter(imagePath: String) = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
        .data(
            when {
                imagePath.startsWith("/") && File(imagePath).exists() -> "file://$imagePath"
                imagePath.startsWith("file://") -> imagePath
                else -> "file:///android_asset/$imagePath" // Asset path
            }
        )
        .build()
)

