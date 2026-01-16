package com.example.ardrawing.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for FAL.ai Nano Banana image-to-image editing API
 */
data class FalImageToImageRequest(
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("image_urls")
    val imageUrls: List<String>, // FAL.ai expects image_urls as array
    
    @SerializedName("aspect_ratio")
    val aspectRatio: String = "auto",
    
    @SerializedName("output_format")
    val outputFormat: String = "png"
)
