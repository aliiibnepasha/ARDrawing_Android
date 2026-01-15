package com.example.ardrawing.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.ardrawing.data.api.FalApiClient
import com.example.ardrawing.data.api.FalApiConfig
import com.example.ardrawing.data.api.model.FalImageToImageRequest
import com.example.ardrawing.data.api.model.FalTextToImageRequest
import com.example.ardrawing.utils.ImageUploadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Repository for text-to-image generation and image-to-image editing using FAL.ai API
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
                Log.e(TAG, "FAL API key not configured")
                return@withContext Result.failure(
                    IllegalArgumentException("Failed to generate image")
                )
            }
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "🚀 Starting Text-to-Image Generation")
            Log.d(TAG, "========================================")
            Log.d(TAG, "📝 Prompt: $prompt")
            Log.d(TAG, "📐 Aspect Ratio: $aspectRatio")
            Log.d(TAG, "🔗 Base URL: ${FalApiConfig.BASE_URL}")
            Log.d(TAG, "🎯 Model Endpoint: ${FalApiConfig.TEXT_TO_IMAGE}")
            Log.d(TAG, "🔑 API Key: ${FalApiConfig.API_KEY.take(10)}...")
            Log.d(TAG, "🌐 Full URL: ${FalApiConfig.BASE_URL}${FalApiConfig.TEXT_TO_IMAGE}")
            
            // Create request
            val request = FalTextToImageRequest(
                prompt = prompt,
                aspectRatio = aspectRatio,
                resolution = "1K",
                numImages = 1,
                outputFormat = "png"
            )
            
            Log.d(TAG, "📦 Request Body: prompt=$prompt, aspectRatio=$aspectRatio, resolution=1K, numImages=1, outputFormat=png")
            Log.d(TAG, "🔐 Authorization Header: Key ${FalApiConfig.API_KEY.take(10)}...")
            
            // Make API call with Key prefix (FAL.ai uses "Key" not "Bearer")
            val response = FalApiClient.apiService.generateTextToImage(
                modelEndpoint = FalApiConfig.TEXT_TO_IMAGE,
                apiKey = "Key ${FalApiConfig.API_KEY}",
                request = request
            )
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API Response Received")
            Log.d(TAG, "========================================")
            Log.d(TAG, "✅ Status Code: ${response.code()}")
            Log.d(TAG, "✅ Is Successful: ${response.isSuccessful}")
            Log.d(TAG, "✅ Headers: ${response.headers()}")
            
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
                
                Log.d(TAG, "🖼️ Image URL received: $imageUrl")
                
                // Download image from URL
                val bitmap = downloadImageFromUrl(imageUrl)
                if (bitmap != null) {
                    Log.d(TAG, "✅ Image downloaded successfully (${bitmap.width}x${bitmap.height})")
                    Log.d(TAG, "========================================")
                    Result.success(bitmap)
                } else {
                    Log.e(TAG, "❌ Failed to download image from URL")
                    Log.d(TAG, "========================================")
                    Result.failure(IllegalStateException("Failed to generate image"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ API Call Failed")
                Log.e(TAG, "========================================")
                Log.e(TAG, "Status Code: ${response.code()}")
                Log.e(TAG, "Error Message: ${response.message()}")
                Log.e(TAG, "Error Body: $errorBody")
                Log.e(TAG, "Request URL: ${FalApiConfig.BASE_URL}${FalApiConfig.TEXT_TO_IMAGE}")
                Log.e(TAG, "========================================")
                // Return user-friendly error message (technical details only in logs)
                Result.failure(
                    Exception("Failed to generate image")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ Exception in generateImage")
            Log.e(TAG, "========================================")
            Log.e(TAG, "Exception Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception Message: ${e.message}")
            Log.e(TAG, "Stack Trace:", e)
            Log.e(TAG, "========================================")
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
    
    /**
     * Convert photo to sketch using FAL.ai image-to-image editing
     * 
     * @param context Android context for accessing content resolver
     * @param imageUri URI of the local image to convert
     * @return Result containing the generated sketch bitmap or error
     */
    suspend fun convertPhotoToSketch(
        context: Context,
        imageUri: Uri
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            // Validate API key
            if (FalApiConfig.API_KEY == "YOUR_FAL_API_KEY_HERE") {
                Log.e(TAG, "FAL API key not configured")
                return@withContext Result.failure(
                    IllegalArgumentException("Failed to generate sketch")
                )
            }
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "🎨 Starting Photo-to-Sketch Conversion")
            Log.d(TAG, "========================================")
            Log.d(TAG, "📷 Image URI: $imageUri")
            
            // Convert local image URI to data URL
            val imageDataUrl = ImageUploadUtils.convertUriToDataUrl(context, imageUri)
            if (imageDataUrl == null) {
                Log.e(TAG, "❌ Failed to convert image URI to data URL")
                return@withContext Result.failure(
                    IllegalStateException("Failed to process selected image")
                )
            }
            
            Log.d(TAG, "✅ Image converted to data URL")
            Log.d(TAG, "📏 Data URL length: ${imageDataUrl.length} characters")
            Log.d(TAG, "🔗 Base URL: ${FalApiConfig.BASE_URL}")
            Log.d(TAG, "🎯 Edit Endpoint: ${FalApiConfig.IMAGE_TO_IMAGE}")
            Log.d(TAG, "🔑 API Key: ${FalApiConfig.API_KEY.take(10)}...")
            Log.d(TAG, "🌐 Full URL: ${FalApiConfig.BASE_URL}${FalApiConfig.IMAGE_TO_IMAGE}")
            
            // Create request with the prompt for sketch conversion
            // FAL.ai expects image_urls as an array
            val prompt = "Convert image to clean pencil sketch outline, transparent background, black line art, no shading"
            val request = FalImageToImageRequest(
                prompt = prompt,
                imageUrls = listOf(imageDataUrl), // Wrap in list as API expects array
                aspectRatio = "auto",
                outputFormat = "png"
            )
            
            Log.d(TAG, "📦 Request Body:")
            Log.d(TAG, "   - Prompt: $prompt")
            Log.d(TAG, "   - Image URLs: [data:image/jpeg;base64,[${imageDataUrl.length} chars]]")
            Log.d(TAG, "   - Aspect Ratio: auto")
            Log.d(TAG, "   - Output Format: png")
            Log.d(TAG, "🔐 Authorization Header: Key ${FalApiConfig.API_KEY.take(10)}...")
            
            // Make API call with Key prefix (FAL.ai uses "Key" not "Bearer")
            val response = FalApiClient.apiService.editImage(
                modelEndpoint = FalApiConfig.IMAGE_TO_IMAGE,
                apiKey = "Key ${FalApiConfig.API_KEY}",
                request = request
            )
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API Response Received")
            Log.d(TAG, "========================================")
            Log.d(TAG, "✅ Status Code: ${response.code()}")
            Log.d(TAG, "✅ Is Successful: ${response.isSuccessful}")
            Log.d(TAG, "✅ Response Message: ${response.message()}")
            Log.d(TAG, "✅ Headers: ${response.headers()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                // Get image URL from response
                val imageUrl = apiResponse.images?.firstOrNull()?.url
                if (imageUrl == null) {
                    Log.e(TAG, "No image URL in response")
                    return@withContext Result.failure(
                        IllegalStateException("Failed to generate sketch")
                    )
                }
                
                Log.d(TAG, "🖼️ Sketch image URL received: $imageUrl")
                
                // Download image from URL
                val bitmap = downloadImageFromUrl(imageUrl)
                if (bitmap != null) {
                    Log.d(TAG, "✅ Sketch image downloaded successfully (${bitmap.width}x${bitmap.height})")
                    Log.d(TAG, "========================================")
                    Result.success(bitmap)
                } else {
                    Log.e(TAG, "❌ Failed to download sketch image from URL")
                    Log.d(TAG, "========================================")
                    Result.failure(IllegalStateException("Failed to generate sketch"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ API Call Failed")
                Log.e(TAG, "========================================")
                Log.e(TAG, "Status Code: ${response.code()}")
                Log.e(TAG, "Error Message: ${response.message()}")
                Log.e(TAG, "Error Body: $errorBody")
                Log.e(TAG, "Request URL: ${FalApiConfig.BASE_URL}${FalApiConfig.IMAGE_TO_IMAGE}")
                Log.e(TAG, "Request Method: POST")
                Log.e(TAG, "Authorization Header: Key ${FalApiConfig.API_KEY.take(10)}...")
                Log.e(TAG, "========================================")
                Result.failure(
                    Exception("API call failed: ${response.code()} - $errorBody")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ Exception in convertPhotoToSketch")
            Log.e(TAG, "========================================")
            Log.e(TAG, "Exception Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception Message: ${e.message}")
            Log.e(TAG, "Stack Trace:", e)
            Log.e(TAG, "========================================")
            Result.failure(e)
        }
    }
}
