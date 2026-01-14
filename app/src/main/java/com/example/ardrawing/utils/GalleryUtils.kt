package com.example.ardrawing.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

object GalleryUtils {
    @Composable
    fun rememberGalleryLauncher(
        onImageSelected: (Uri?) -> Unit
    ): androidx.activity.result.ActivityResultLauncher<String> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onImageSelected(uri)
        }
    }
    
    fun openGallery(
        launcher: androidx.activity.result.ActivityResultLauncher<String>,
        mimeType: String = "image/*"
    ) {
        launcher.launch(mimeType)
    }
}
