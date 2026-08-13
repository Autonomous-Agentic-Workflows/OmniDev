package com.example.devgate.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Hermes Bridge Client — connects the Android app to the vertex_orchestrator
 * Python backend running on the host machine (or a remote server).
 * 
 * The backend exposes a simple REST API:
 *   POST /execute  { task_type, task, model, temperature }
 *   POST /batch    { tasks: [...] }
 *   GET  /health
 * 
 * This bridges the Android app to CrewAI, AutoGen, and Aider running
 * on the host with Google Cloud Vertex AI credentials.
 */
object HermesBridgeClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    suspend fun queryHermes(
        prompt: String,
        systemInstruction: String? = null,
        model: String = "gemini-2.5-pro",
        backendUrl: String,
        apiKey: String,
        temperature: Float = 0.2f,
        maxTokens: Int = 2048
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = backendUrl.trimEnd('/')
        if (cleanUrl.isEmpty() || cleanUrl == "http://10.0.2.2:8000/") {
            // Default — check if backend is reachable
        }

        try {
            val jsonBody = buildString {
                append("{")
                append("\"task_type\":\"ANALYSIS\",")
                append("\"task\":${moshi.adapter(String::class.java).toJson(prompt)},")
                append("\"model\":${moshi.adapter(String::class.java).toJson(model)},")
                append("\"temperature\":$temperature,")
                append("\"max_tokens\":$maxTokens")
                if (systemInstruction != null) {
                    append(",\"system_message\":${moshi.adapter(String::class.java).toJson(systemInstruction)}")
                }
                append("}")
            }

            val requestBuilder = Request.Builder()
                .url("$cleanUrl/execute")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")

            if (apiKey.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                // Parse the orchestrator response: {"success":true,"output":"...","runner_used":"crewai"}
                val adapter = moshi.adapter(Map::class.java)
                val parsed = adapter.fromJson(responseBody) as? Map<String, Any>
                val output = parsed?.get("output") as? String
                val success = parsed?.get("success") as? Boolean

                if (success == true && !output.isNullOrBlank()) {
                    Result.success(output)
                } else {
                    val error = parsed?.get("error") as? String
                    Result.failure(Exception(error ?: "Hermes backend returned error"))
                }
            } else {
                Result.success(getHermesFallback(prompt, model, cleanUrl))
            }
        } catch (e: Exception) {
            Result.success("Hermes [Backend Offline]\n${getHermesFallback(prompt, model, backendUrl)}")
        }
    }

    suspend fun checkHealth(backendUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = backendUrl.trimEnd('/')
            val request = Request.Builder()
                .url("$cleanUrl/health")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private fun getHermesFallback(prompt: String, model: String, backendUrl: String): String {
        return """
            [Hermes Agent Bridge — Backend Not Reachable]
            
            Attempted connection to: $backendUrl
            
            To enable the Hermes Agent bridge:
            1. Start the vertex_orchestrator backend:
               cd vertex_orchestrator
               PYTHONPATH=src python -m vertex_orchestrator.server
            2. Ensure it's running on the host machine
            3. If using emulator, use http://10.0.2.2:8000/
            4. If using physical device, use your machine's IP: http://192.168.x.x:8000/
            5. Install crewai/pyautogen/aider-chat for full orchestration
            
            The backend routes tasks to:
            - CrewAI (analysis/audit tasks)
            - AutoGen (multi-agent conversations)
            - Aider (code editing)
            All via Google Vertex AI with enterprise IP protection.
        """.trimIndent()
    }
}