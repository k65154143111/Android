package com.example.subtitleapp.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- Define Data Classes ONCE ---
// (Move these to separate files like 'VideoRequest.kt' and 'SubtitleResponse.kt' if preferred)
data class VideoRequest(
    val url: String
)

data class SubtitleResponse(
    val success: Boolean,
    val srtContent: String // Ensure this name matches the JSON key from the API
)
// --- End of Data Class definitions ---


interface ApiService {

    @POST("generate-subtitles") // Make sure this endpoint is correct
    suspend fun generateSubtitles(
        @Body request: VideoRequest
    ): Response<SubtitleResponse> // Ensure T matches the expected JSON structure

    companion object {
        fun create(baseUrl: String): ApiService {
            // Basic validation for base URL
            val validBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"

            val retrofit = Retrofit.Builder()
                .baseUrl(validBaseUrl) // Ensure base URL ends with '/'
                .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}
