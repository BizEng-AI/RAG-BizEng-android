package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.TrackingApi
import com.example.myapplication.data.remote.dto.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackingRepositoryTest {
    private lateinit var trackingRepository: TrackingRepository
    private lateinit var trackingApi: TrackingApi

    @Before
    fun setup() {
        trackingApi = mockk()
        trackingRepository = TrackingRepository(trackingApi)
    }

    @After
    fun tearDown() { clearAllMocks() }

    @Test
    fun `startExercise should call API and return attempt`() = runTest {
        val exerciseId = "conv_1"
        val exerciseType = "chat"
        val dto = ExerciseAttemptDto(
            id = "attempt_123",
            exerciseId = exerciseId,
            exerciseType = exerciseType,
            status = "started",
            score = null,
            durationSec = null,
            startedAt = "2025-11-11T10:00:00Z",
            finishedAt = null
        )
        coEvery { trackingApi.startAttempt(ExerciseAttemptReq(exerciseId, exerciseType)) } returns dto

        val result = trackingRepository.startExercise(exerciseId, exerciseType)
        assertTrue(result.isSuccess)
        assertEquals("attempt_123", result.getOrNull()!!.id)
        coVerify { trackingApi.startAttempt(ExerciseAttemptReq(exerciseId, exerciseType)) }
    }

    @Test
    fun `updateExercise should send update and return updated dto`() = runTest {
        val attemptId = "attempt_123"
        val updateDto = ExerciseAttemptDto(
            id = attemptId,
            exerciseId = "conv_1",
            exerciseType = "chat",
            status = "completed",
            score = 0.9f,
            durationSec = 120,
            startedAt = "2025-11-11T10:00:00Z",
            finishedAt = "2025-11-11T10:02:00Z"
        )
        coEvery { trackingApi.updateAttempt(attemptId, ExerciseAttemptUpdate("completed", 0.9f, 120)) } returns updateDto

        val result = trackingRepository.updateExercise(attemptId, status = "completed", score = 0.9f, durationSec = 120)
        assertTrue(result.isSuccess)
        assertEquals("completed", result.getOrNull()!!.status)
        coVerify { trackingApi.updateAttempt(attemptId, ExerciseAttemptUpdate("completed", 0.9f, 120)) }
    }

    @Test
    fun `logActivity should call API and return event`() = runTest {
        val exerciseId = "conv_1"
        val eventType = "opened"
        val dto = ActivityEventDto(
            id = 1L,
            eventType = eventType,
            ts = "2025-11-11T10:00:00Z"
        )
        coEvery { trackingApi.logEvent(ActivityEventReq(exerciseId, eventType, null)) } returns dto

        val result = trackingRepository.logActivity(exerciseId, eventType)
        assertTrue(result.isSuccess)
        coVerify { trackingApi.logEvent(ActivityEventReq(exerciseId, eventType, null)) }
    }

    @Test
    fun `getMyProgress should return summary`() = runTest {
        val progress = ProgressSummaryDto(
            totals = TotalsDto(10, 6, 0.85f, 120),
            byType = mapOf("chat" to TypeStatsDto(5, 0.9f)),
            recentAttempts = emptyList()
        )
        coEvery { trackingApi.getMyProgress(null, null) } returns progress

        val result = trackingRepository.getMyProgress()
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.totals.attempts)
        coVerify { trackingApi.getMyProgress(null, null) }
    }
}
