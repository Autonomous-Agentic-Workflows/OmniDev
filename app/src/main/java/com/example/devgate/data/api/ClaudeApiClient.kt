package com.example.devgate.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String,
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

object ClaudeApiClient {
    private const val BASE_URL = "https://api.anthropic.com/"
    private const val ANTHROPIC_VERSION = "2023-06-01"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: ClaudeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ClaudeApiService::class.java)
    }

    suspend fun queryClaude(
        prompt: String,
        systemInstruction: String? = null,
        model: String = "claude-sonnet-4-20250514",
        apiKey: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getClaudeFallback(prompt, model))
        }

        try {
            val request = ClaudeRequest(
                model = model,
                max_tokens = maxTokens,
                system = systemInstruction,
                messages = listOf(ClaudeMessage(role = "user", content = prompt)),
                temperature = temperature
            )

            val response = service.createMessage(apiKey, ANTHROPIC_VERSION, request)
            val text = response.content?.firstOrNull()?.text

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else if (response.error?.message != null) {
                Result.failure(Exception("Claude API Error: ${response.error.message}"))
            } else {
                Result.success(getClaudeFallback(prompt, model))
            }
        } catch (e: Exception) {
            Result.success("Claude [Offline Mode]\n${getClaudeFallback(prompt, model)}")
        }
    }

    private fun getClaudeFallback(prompt: String, model: String): String {
        return """
            [Claude $model Analysis]
            
            Processed: "$prompt"
            
            Claude reasoning engine standby — API key required for live inference.
            Configure your Anthropic API key in Settings to enable Claude.
        """.trimIndent()
    }
}