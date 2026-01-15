package com.example.ardrawing.data.api

/**
 * Configuration for FAL.ai API
 * 
 * TODO: Add your FAL API key here or load from secure storage
 * You can get your API key from: https://fal.ai/dashboard/keys
 */
object FalApiConfig {
    // Base URL for FAL.ai API
    const val BASE_URL = "https://fal.run/"
    
    // API Key - Replace with your actual key
    // For production, consider using BuildConfig or secure storage
    const val API_KEY = "YOUR_FAL_API_KEY_HERE"
    
    // Model endpoint
    const val MODEL_ENDPOINT = "fal-ai/nano-banana"
}
