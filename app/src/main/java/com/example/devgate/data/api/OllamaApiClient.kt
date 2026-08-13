package com.example.devgate.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OllamaApiService {
    @POST("api/generate")
    suspend fun generate(
        @Body request: OllamaRequest
    ): OllamaResponse
}

object OllamaApiClient {
    // Default local Ollama server — can be overridden in settings
    private var baseUrl = "http://10.0.2.2:11434/"
    private const val DEFAULT_MODEL = "llama3"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var service: OllamaApiService = buildService()

    private fun buildService(): OllamaApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OllamaApiService::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        service = buildService()
    }

    fun getCurrentBaseUrl(): String = baseUrl

    suspend fun queryOllama(
        prompt: String,
        systemInstruction: String? = null,
        model: String = DEFAULT_MODEL,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = OllamaRequest(
                model = model,
                prompt = prompt,
                system = systemInstruction,
                stream = false,
                options = OllamaOptions(temperature = temperature, num_predict = maxTokens)
            )

            val response = service.generate(request)
            val text = response.response

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else if (!response.error.isNullOrBlank()) {
                Result.failure(Exception("Ollama Error: ${response.error}"))
            } else {
                Result.success(getOllamaFallback(prompt, model))
            }
        } catch (e: Exception) {
            Result.success("Ollama [Connection Failed — Is Ollama running?]\n${getOllamaFallback(prompt, model)}")
        }
    }

    private fun getOllamaFallback(prompt: String, model: String): String {
        return """
            [Ollama $model — Local Inference Standby]
            
            Could not connect to Ollama server at $baseUrl
            
            To enable local LLM inference:
            1. Install Ollama: https://ollama.com
            2. Run: ollama pull $model
            3. Start server: ollama serve
            4. If on emulator, ensure Ollama is on host machine (10.0.2.2:11434)
            5. If on device, set the correct host IP in Settings
        """.trimIndent()
    }
}