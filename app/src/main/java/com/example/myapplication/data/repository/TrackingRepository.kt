package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.TrackingApi
import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.data.local.db.ExerciseAttemptDao
import com.example.myapplication.data.local.db.ExerciseAttemptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackingRepository @Inject constructor(
    private val trackingApi: TrackingApi,
    private val attemptDao: ExerciseAttemptDao? = null
) {
    // Track start timestamps for duration auto-calculation
    private val attemptStarts = mutableMapOf<String, Long>()

    /**
     * Start tracking an exercise attempt
     * Call this when user begins an exercise
     */
    suspend fun startExercise(
        exerciseId: String,
        exerciseType: String  // "chat", "roleplay", "pronunciation", "rag"
    ): Result<ExerciseAttemptDto> = runCatching {
        trackingApi.startAttempt(
            ExerciseAttemptReq(exerciseId, exerciseType)
        ).also { dto ->
            attemptStarts[dto.id] = System.currentTimeMillis()
        }
    }

    /**
     * Update an exercise attempt
     * Call this when user completes or exits an exercise
     */
    suspend fun updateExercise(
        attemptId: String,
        status: String? = null,  // "completed", "abandoned"
        score: Float? = null,
        durationSec: Int? = null
    ): Result<ExerciseAttemptDto> = runCatching {
        val computed = durationSec ?: attemptStarts[attemptId]?.let { startMs ->
            val diffSec = ((System.currentTimeMillis() - startMs) / 1000).toInt().coerceAtLeast(1)
            diffSec
        }
        trackingApi.updateAttempt(
            attemptId,
            ExerciseAttemptUpdate(status, score, computed)
        ).also { attemptStarts.remove(attemptId) }
    }

    /**
     * Log an activity event (lightweight tracking)
     * Call this when user opens a feature or takes an action
     */
    suspend fun logActivity(
        exerciseId: String,
        eventType: String,  // "opened", "started", "completed", "abandoned"
        payload: Map<String, String>? = null
    ): Result<ActivityEventDto> = runCatching {
        trackingApi.logEvent(
            ActivityEventReq(exerciseId, eventType, payload)
        )
    }

    /**
     * Get user's progress summary
     */
    suspend fun getMyProgress(
        from: String? = null,
        to: String? = null,
        days: Int? = null
    ): Result<ProgressSummaryDto> = runCatching {
        when {
            days != null -> trackingApi.getMyProgress(days)
            else -> trackingApi.getMyProgress(from, to)
        }
    }

    /**
     * Get user's summary
     */
    suspend fun getMySummary(days: Int = 30): Result<SummaryDto> = runCatching {
        trackingApi.getMySummary(days)
    }

    /**
     * Get user's attempts
     */
    suspend fun getMyAttempts(limit: Int = 50, offset: Int = 0, days: Int = 30): Result<List<AttemptDto>> = runCatching {
        trackingApi.getMyAttempts(limit, offset, days)
    }

    fun getRecentAttempts(days: Int = 30, limit: Int = 50): Flow<List<AttemptDto>> = flow {
        if (attemptDao == null) {
            // No local cache available yet: emit network result directly
            val remote = runCatching { trackingApi.getMyAttempts(limit = limit, offset = 0, days = days) }.getOrElse { emptyList() }
            emit(remote)
            return@flow
        }
        // 1) Emit cached
        emitAll(
            attemptDao.getRecent(limit).map { rows ->
                rows.map {
                    AttemptDto(
                        id = it.remoteId ?: it.localId.toInt(),
                        userId = it.userId ?: 0,
                        exerciseType = it.exerciseType,
                        exerciseId = it.exerciseId,
                        startedAt = it.startedAt,
                        finishedAt = it.finishedAt,
                        durationSeconds = it.durationSeconds,
                        score = it.score,
                        passed = null,
                        extraMetadata = null
                    )
                }
            }
        )
        // 2) Refresh from network
        runCatching {
            val remote = trackingApi.getMyAttempts(limit = limit, offset = 0, days = days)
            val entities = remote.map {
                ExerciseAttemptEntity(
                    remoteId = it.id,
                    userId = it.userId,
                    exerciseType = it.exerciseType,
                    exerciseId = it.exerciseId,
                    startedAt = it.startedAt,
                    finishedAt = it.finishedAt,
                    durationSeconds = it.durationSeconds,
                    score = it.score
                )
            }
            attemptDao.upsertAll(entities)
        }
    }

    suspend fun abandonExercise(
        attemptId: String,
        exerciseId: String,
        exerciseType: String,
        score: Float? = null
    ): Result<ExerciseAttemptDto> = runCatching {
        val dto = trackingApi.updateAttempt(
            attemptId,
            ExerciseAttemptUpdate(status = "abandoned", score = score, durationSec = attemptStarts[attemptId]?.let { ((System.currentTimeMillis() - it) / 1000).toInt().coerceAtLeast(1) })
        )
        attemptStarts.remove(attemptId)
        // Fire lightweight analytics event
        kotlin.runCatching {
            trackingApi.logEvent(
                ActivityEventReq(
                    exerciseId = exerciseId,
                    eventType = "abandoned",
                    payload = mapOf(
                        "attempt_id" to attemptId,
                        "exercise_type" to exerciseType,
                        "duration_sec" to (dto.durationSec ?: 0).toString()
                    )
                )
            )
        }
        dto
    }
}
