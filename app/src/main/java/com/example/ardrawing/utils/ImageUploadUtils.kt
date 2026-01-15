package com.example.ardrawing.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Utility for handling image uploads and conversions for FAL.ai API
 */
object ImageUploadUtils {
    
    private const val TAG = "ImageUploadUtils"
    
    /**
     * Convert local image URI to base64 data URL format
     * FAL.ai accepts data URLs for local images
     * 
     * @param context Android context
     * @param imageUri URI of the local image
     * @return Base64 data URL string (e.g., "data:image/png;base64,...")
     */
    fun convertUriToDataUrl(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: $imageUri")
                return null
            }
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: $imageUri")
                return null
            }
            
            // Compress bitmap to reduce size (max 2MB for API)
            val outputStream = ByteArrayOutputStream()
            var quality = 90
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            // If still too large, reduce quality
            while (outputStream.toByteArray().size > 2 * 1024 * 1024 && quality > 50) {
                outputStream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            // Return data URL format
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to data URL: ${e.message}", e)
            null
        }
    }
    
    /**
     * Convert Bitmap to base64 data URL
     */
    fun convertBitmapToDataUrl(bitmap: Bitmap): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            var quality = 90
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
            
            // If too large, reduce quality
            while (outputStream.toByteArray().size > 2 * 1024 * 1024 && quality > 50) {
                outputStream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
            }
            
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            "data:image/png;base64,$base64String"
        } catch (e: Exception) {
            Log.e(TAG, "Error converting bitmap to data URL: ${e.message}", e)
            null
        }
    }
}
