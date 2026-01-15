package com.example.ardrawing.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for FAL.ai Nano Banana text-to-image API
 */
data class FalTextToImageRequest(
    @SerializedName("prompt")
    val prompt: String,
    
    @SerializedName("aspect_ratio")
    val aspectRatio: String = "1:1",
    
    @SerializedName("resolution")
    val resolution: String = "1K",
    
    @SerializedName("num_images")
    val numImages: Int = 1,
    
    @SerializedName("output_format")
    val outputFormat: String = "png"
)
