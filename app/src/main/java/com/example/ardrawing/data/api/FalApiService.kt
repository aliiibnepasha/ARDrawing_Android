package com.example.ardrawing.data.api

import com.example.ardrawing.data.api.model.FalTextToImageRequest
import com.example.ardrawing.data.api.model.FalTextToImageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service interface for FAL.ai API
 */
interface FalApiService {
    
    /**
     * Generate image from text prompt using Nano Banana model
     * 
     * @param modelEndpoint The model endpoint (e.g., "fal-ai/nano-banana")
     * @param apiKey FAL API key for authentication
     * @param request Request body with prompt and options
     * @return Response containing generated image URLs
     */
    @POST("{modelEndpoint}")
    suspend fun generateTextToImage(
        @Path("modelEndpoint") modelEndpoint: String,
        @Header("Authorization") apiKey: String,
        @Body request: FalTextToImageRequest
    ): Response<FalTextToImageResponse>
}
