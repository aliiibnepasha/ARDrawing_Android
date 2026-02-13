package com.example.ardrawing.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PersistenceUtils {
    
    /**
     * Copies an image from a Uri (e.g. from gallery) to the app's internal storage
     * to ensure it persists after app is killed.
     * @return The absolute path of the saved file
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            
            // Create target directory if it doesn't exist
            val directory = File(context.filesDir, "my_album")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            // Generate unique filename
            val fileName = "album_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)
            
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
