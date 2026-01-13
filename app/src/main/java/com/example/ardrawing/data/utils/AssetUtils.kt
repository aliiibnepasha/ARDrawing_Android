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
                        filename.endsWith(".webp", ignoreCase = true) ||
                        filename.endsWith(".svg", ignoreCase = true)
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
    
    /**
     * Lists all subfolders in the specified assets folder
     * @param context Android context to access AssetManager
     * @param folderPath Path to the parent folder in assets (e.g., "home")
     * @return List of folder names sorted alphabetically
     */
    fun listFolders(context: Context, folderPath: String): List<String> {
        val folders = mutableListOf<String>()
        
        try {
            val files = context.assets.list(folderPath)
            if (files != null) {
                // Filter for folders (directories) - check if it's a folder by trying to list it
                files.forEach { name ->
                    try {
                        val subFiles = context.assets.list("$folderPath/$name")
                        // If we can list it and it contains files, it's a folder
                        if (subFiles != null && subFiles.isNotEmpty()) {
                            folders.add(name)
                        }
                    } catch (e: Exception) {
                        // Not a folder, skip
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        return folders.sorted()
    }

    /**
     * Loads a bitmap from the assets folder
     * @param context Android context
     * @param path Relative path in assets (e.g. "categories/Animals/cat.png")
     * @return Bitmap or null if failed
     */
    fun getBitmapFromAsset(context: Context, path: String): android.graphics.Bitmap? {
        return try {
            val inputStream = context.assets.open(path)
            android.graphics.BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

