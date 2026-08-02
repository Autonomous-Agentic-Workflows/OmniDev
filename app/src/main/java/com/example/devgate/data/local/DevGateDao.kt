package com.example.devgate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DevGateDao {
    // Repos & Commits
    @Query("SELECT * FROM git_repos")
    fun getAllRepos(): Flow<List<GitRepoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepo(repo: GitRepoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRepos(repos: List<GitRepoEntity>)

    @Query("SELECT * FROM git_commits WHERE repoId = :repoId ORDER BY timestamp DESC")
    fun getCommitsForRepo(repoId: String): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity)

    // Snippets
    @Query("SELECT * FROM snippets ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<SnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity)

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteSnippet(id: String)

    // CLI History
    @Query("SELECT * FROM cli_history ORDER BY timestamp DESC LIMIT 100")
    fun getCliHistory(): Flow<List<CliHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliHistory(entry: CliHistoryEntity)

    @Query("DELETE FROM cli_history")
    suspend fun clearCliHistory()

    // Jules Tasks & Steps
    @Query("SELECT * FROM jules_tasks ORDER BY createdTime DESC")
    fun getAllJulesTasks(): Flow<List<JulesTaskEntity>>

    @Query("SELECT * FROM jules_steps WHERE taskId = :taskId ORDER BY stepIndex ASC")
    fun getStepsForTask(taskId: String): Flow<List<JulesStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJulesTask(task: JulesTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJulesStep(step: JulesStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJulesSteps(steps: List<JulesStepEntity>)

    @Query("UPDATE jules_tasks SET status = :status, currentStepIndex = :currentStepIndex, resultSummary = :summary WHERE id = :taskId")
    suspend fun updateJulesTaskStatus(taskId: String, status: String, currentStepIndex: Int, summary: String)

    @Query("UPDATE jules_steps SET status = :status, logOutput = :logOutput WHERE id = :stepId")
    suspend fun updateJulesStep(stepId: String, status: String, logOutput: String)
}
