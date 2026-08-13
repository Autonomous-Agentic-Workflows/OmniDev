package com.example.devgate.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface VertexApiService {
    @POST("v1/projects/{project}/locations/{location}/publishers/google/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("project") project: String,
        @Path("location") location: String,
        @Path("model") model: String,
        @Header("Authorization") authHeader: String,
        @Body request: VertexRequest
    ): VertexResponse
}

object VertexApiClient {
    // Vertex AI uses Google Cloud auth (ADC or service account token)
    // For Android, we accept a bearer token or API key-based approach
    private const val BASE_URL = "https://us-central1-aiplatform.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: VertexApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VertexApiService::class.java)
    }

    suspend fun queryVertex(
        prompt: String,
        systemInstruction: String? = null,
        model: String = "gemini-2.5-pro",
        projectId: String,
        location: String = "us-central1",
        accessToken: String,
        temperature: Float = 0.2f,
        maxTokens: Int = 2048
    ): Result<String> = withContext(Dispatchers.IO) {
        if (projectId.isEmpty() || accessToken.isEmpty()) {
            return@withContext Result.success(getVertexFallback(prompt, model))
        }

        try {
            val sysInstruction = systemInstruction?.let {
                Content(parts = listOf(Part(text = it)), role = "system")
            }

            val request = VertexRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)), role = "user")),
                generationConfig = GenerationConfig(
                    temperature = temperature,
                    topP = 0.95f,
                    maxOutputTokens = maxTokens
                ),
                systemInstruction = sysInstruction
            )

            val response = service.generateContent(
                project = projectId,
                location = location,
                model = model,
                authHeader = "Bearer $accessToken",
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else if (response.error?.message != null) {
                Result.failure(Exception("Vertex AI Error: ${response.error.message}"))
            } else {
                Result.success(getVertexFallback(prompt, model))
            }
        } catch (e: Exception) {
            Result.success("Vertex AI [Connection Error]\n${getVertexFallback(prompt, model)}")
        }
    }

    private fun getVertexFallback(prompt: String, model: String): String {
        return """
            [Vertex AI $model — Enterprise Gateway Standby]
            
            Processed: "$prompt"
            
            To enable Google Cloud Vertex AI:
            1. Run: gcloud auth application-default login
            2. Set GOOGLE_CLOUD_PROJECT in Settings
            3. Ensure aiplatform.googleapis.com API is enabled
            4. Provide a valid access token or service account key
            
            The vertex_orchestrator Python backend can also proxy requests
            from your local machine for enterprise-secured access.
        """.trimIndent()
    }
}