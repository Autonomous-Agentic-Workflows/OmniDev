package com.example.devgate.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = false)
data class Content(
    val parts: List<Part>,
    val role: String? = "user"
)

@JsonClass(generateAdapter = false)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = false)
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 2048
)

@JsonClass(generateAdapter = false)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val error: ApiError? = null
)

@JsonClass(generateAdapter = false)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = false)
data class ApiError(
    val code: Int? = null,
    val message: String? = null
)
