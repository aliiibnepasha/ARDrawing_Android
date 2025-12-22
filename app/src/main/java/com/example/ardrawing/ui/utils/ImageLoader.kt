package com.example.ardrawing.ui.utils

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File

/**
 * Creates an image painter for loading images from various sources
 */
@Composable
fun rememberImagePainter(imagePath: String) = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
        .data(
            when {
                imagePath.startsWith("file://") -> imagePath
                imagePath.startsWith("/") -> "file://$imagePath" // File path
                else -> "file:///android_asset/$imagePath" // Asset path
            }
        )
        .build()
)

@Composable
fun rememberUriImagePainter(uri: Uri?) = rememberAsyncImagePainter(
    model = uri?.let {
        ImageRequest.Builder(LocalContext.current)
            .data(it)
            .build()
    }
)

