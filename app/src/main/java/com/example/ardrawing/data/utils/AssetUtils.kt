package com.example.ardrawing.data.utils

import android.content.Context
import java.io.IOException

object AssetUtils {
    /**
     * Lists all image files in the specified assets folder
     * @param context Android context to access AssetManager
     * @param folderPath Path to the folder in assets (e.g., "bg_remove")
     * @return List of image filenames sorted alphabetically
     */
    fun listImageFiles(context: Context, folderPath: String): List<String> {
        val imageFiles = mutableListOf<String>()
        
        try {
            val files = context.assets.list(folderPath)
            if (files != null) {
                // Filter for image files and sort
                imageFiles.addAll(
                    files.filter { filename ->
                        filename.endsWith(".png", ignoreCase = true) ||
                        filename.endsWith(".jpg", ignoreCase = true) ||
                        filename.endsWith(".jpeg", ignoreCase = true) ||
                        filename.endsWith(".webp", ignoreCase = true)
                    }.sorted()
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        return imageFiles
    }
    
    /**
     * Gets the full asset path for an image in a folder
     * @param folderPath Folder path in assets (e.g., "bg_remove")
     * @param filename Image filename (e.g., "img1.png")
     * @return Full asset path (e.g., "bg_remove/img1.png")
     */
    fun getAssetPath(folderPath: String, filename: String): String {
        return "$folderPath/$filename"
    }
}

