package com.example.devgate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "git_repos")
data class GitRepoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val activeBranch: String,
    val uncommittedChangesCount: Int,
    val totalCommits: Int,
    val remoteUrl: String,
    val isClean: Boolean
)

@Entity(tableName = "git_commits")
data class GitCommitEntity(
    @PrimaryKey val hash: String,
    val repoId: String,
    val message: String,
    val author: String,
    val timestamp: Long,
    val branch: String,
    val filesChangedCount: Int
)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey val id: String,
    val title: String,
    val language: String,
    val code: String,
    val description: String,
    val tagsRaw: String, // Comma separated
    val timestamp: Long
)

@Entity(tableName = "cli_history")
data class CliHistoryEntity(
    @PrimaryKey val id: String,
    val command: String,
    val response: String,
    val timestamp: Long,
    val modelUsed: String,
    val isSuccess: Boolean,
    val executionTimeMs: Long
)

@Entity(tableName = "jules_tasks")
data class JulesTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val createdTime: Long,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val resultSummary: String
)

@Entity(tableName = "jules_steps")
data class JulesStepEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val stepIndex: Int,
    val name: String,
    val detail: String,
    val status: String,
    val logOutput: String,
    val timestamp: Long
)
