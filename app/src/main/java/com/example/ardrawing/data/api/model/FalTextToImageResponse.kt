package com.example.ardrawing.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for FAL.ai Nano Banana text-to-image API
 */
data class FalTextToImageResponse(
    @SerializedName("images")
    val images: List<FalImage>?,
    
    @SerializedName("description")
    val description: String?
)

/**
 * Image data from FAL API response
 */
data class FalImage(
    @SerializedName("file_name")
    val fileName: String?,
    
    @SerializedName("content_type")
    val contentType: String?,
    
    @SerializedName("url")
    val url: String?
)
