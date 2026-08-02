package com.example.devgate.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class GitHubRepoDto(
    @field:Json(name = "name") val name: String,
    @field:Json(name = "full_name") val fullName: String,
    @field:Json(name = "default_branch") val defaultBranch: String?,
    @field:Json(name = "clone_url") val cloneUrl: String?,
    @field:Json(name = "stargazers_count") val starsCount: Int?,
    @field:Json(name = "open_issues_count") val openIssuesCount: Int?
)

data class GitHubCommitDto(
    @field:Json(name = "sha") val sha: String,
    @field:Json(name = "commit") val commitData: CommitDetailsDto
)

data class CommitDetailsDto(
    @field:Json(name = "message") val message: String,
    @field:Json(name = "author") val author: CommitAuthorDto?
)

data class CommitAuthorDto(
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "date") val date: String?
)

data class GitHubBranchDto(
    @field:Json(name = "name") val name: String
)

interface GitHubApiService {
    @GET("repos/{owner}/{repo}")
    suspend fun getRepoDetails(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRepoDto>

    @GET("repos/{owner}/{repo}/commits")
    suspend fun getRepoCommits(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("sha") branch: String? = null,
        @Query("per_page") perPage: Int = 15
    ): Response<List<GitHubCommitDto>>

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getRepoBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<List<GitHubBranchDto>>
}

object GitHubApiClient {
    private const val BASE_URL = "https://api.github.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "DevGate-Android-App")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubApiService::class.java)
    }
}
