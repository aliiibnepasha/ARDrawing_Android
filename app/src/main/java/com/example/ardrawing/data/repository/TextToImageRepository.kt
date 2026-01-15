package com.example.ardrawing.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.ardrawing.data.api.FalApiClient
import com.example.ardrawing.data.api.FalApiConfig
import com.example.ardrawing.data.api.model.FalTextToImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Repository for text-to-image generation using FAL.ai API
 * Handles API calls and image downloading
 */
class TextToImageRepository {
    
    companion object {
        private const val TAG = "TextToImageRepository"
    }
    
    /**
     * Generate image from text prompt
     * 
     * @param prompt Text description of the image to generate
     * @param aspectRatio Aspect ratio (e.g., "1:1", "16:9")
     * @return Result containing the generated bitmap or error
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1"
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            // Validate API key
            if (FalApiConfig.API_KEY == "YOUR_FAL_API_KEY_HERE") {
                return@withContext Result.failure(
                    IllegalArgumentException("FAL API key not configured. Please add your API key in FalApiConfig.kt")
                )
            }
            
            Log.d(TAG, "Generating image for prompt: $prompt")
            
            // Create request
            val request = FalTextToImageRequest(
                prompt = prompt,
                aspectRatio = aspectRatio,
                resolution = "1K",
                numImages = 1,
                outputFormat = "png"
            )
            
            // Make API call
            val response = FalApiClient.apiService.generateTextToImage(
                modelEndpoint = FalApiConfig.MODEL_ENDPOINT,
                apiKey = "Key ${FalApiConfig.API_KEY}",
                request = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                // Get image URL from response
                val imageUrl = apiResponse.images?.firstOrNull()?.url
                if (imageUrl == null) {
                    Log.e(TAG, "No image URL in response")
                    return@withContext Result.failure(
                        IllegalStateException("No image URL in API response")
                    )
                }
                
                Log.d(TAG, "Image URL received: $imageUrl")
                
                // Download image from URL
                val bitmap = downloadImageFromUrl(imageUrl)
                if (bitmap != null) {
                    Log.d(TAG, "Image downloaded successfully")
                    Result.success(bitmap)
                } else {
                    Log.e(TAG, "Failed to download image from URL")
                    Result.failure(IllegalStateException("Failed to download image"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API call failed: ${response.code()} - $errorBody")
                Result.failure(
                    Exception("API call failed: ${response.code()} - $errorBody")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download image from URL and convert to Bitmap
     */
    private suspend fun downloadImageFromUrl(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpsURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.doInput = true
            connection.connect()
            
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()
            
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image from URL: $urlString", e)
            null
        }
    }
}
