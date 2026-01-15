package com.example.ardrawing.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object FavoriteImageUtils {
    /**
     * Save bitmap to internal storage for favorites
     */
    fun saveFavoriteImage(bitmap: Bitmap, context: Context, prompt: String): String? {
        return try {
            val timestamp = System.currentTimeMillis()
            val sanitizedPrompt = prompt.take(50).replace(Regex("[^a-zA-Z0-9]"), "_")
            val filename = "favorite_${sanitizedPrompt}_$timestamp.jpg"
            
            val storageDir = File(context.filesDir, "favorites")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val imageFile = File(storageDir, filename)
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            imageFile.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("FavoriteImageUtils", "Error saving favorite image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Load bitmap from file path
     */
    fun loadFavoriteImage(imagePath: String): Bitmap? {
        return try {
            android.graphics.BitmapFactory.decodeFile(imagePath)
        } catch (e: Exception) {
            android.util.Log.e("FavoriteImageUtils", "Error loading favorite image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Delete favorite image file
     */
    fun deleteFavoriteImage(imagePath: String): Boolean {
        return try {
            File(imagePath).delete()
        } catch (e: Exception) {
            android.util.Log.e("FavoriteImageUtils", "Error deleting favorite image: ${e.message}", e)
            false
        }
    }
}
