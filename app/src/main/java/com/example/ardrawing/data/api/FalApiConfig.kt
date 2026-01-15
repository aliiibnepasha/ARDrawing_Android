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
    const val API_KEY = "b6dc623b-fd52-4139-8feb-fa9b147c707e:22e3202a35ce1768484da5184b2da7d8"
    
    // Model endpoints
    const val TEXT_TO_IMAGE = "fal-ai/nano-banana"
    const val IMAGE_TO_IMAGE = "fal-ai/nano-banana/edit"
}
