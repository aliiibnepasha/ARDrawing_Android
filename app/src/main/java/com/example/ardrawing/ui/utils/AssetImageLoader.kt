package com.example.ardrawing.ui.utils

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Creates an image painter for loading images from assets folder
 * @param assetPath The path to the image in assets folder (e.g., "image1.png")
 */
@Composable
fun rememberAssetImagePainter(assetPath: String) = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
        .data("file:///android_asset/$assetPath")
        .build()
)

