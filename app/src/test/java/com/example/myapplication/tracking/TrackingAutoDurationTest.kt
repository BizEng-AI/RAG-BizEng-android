package com.example.myapplication.tracking

import com.example.myapplication.data.remote.TrackingApi
import com.example.myapplication.data.remote.dto.ExerciseAttemptDto
import com.example.myapplication.data.remote.dto.ExerciseAttemptReq
import com.example.myapplication.data.remote.dto.ExerciseAttemptUpdate
import com.example.myapplication.data.repository.TrackingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingAutoDurationTest {

    private val api = mockk<TrackingApi>()
    private val repo = TrackingRepository(api)

    @Test
    fun `auto duration computed when updateExercise called without duration`() = runTest {
        val startDto = ExerciseAttemptDto(
            id = "attempt_1",
            exerciseId = "chat_session",
            exerciseType = "chat",
            status = "started",
            score = null,
            durationSec = null,
            startedAt = "2025-11-20T10:00:00Z",
            finishedAt = null
        )
        coEvery { api.startAttempt(ExerciseAttemptReq("chat_session", "chat")) } returns startDto

        val updateSlot = slot<ExerciseAttemptUpdate>()
        val completedDto = startDto.copy(status = "completed", durationSec = 2)
        coEvery { api.updateAttempt("attempt_1", capture(updateSlot)) } returns completedDto

        val startRes = repo.startExercise("chat_session", "chat")
        assertTrue(startRes.isSuccess)

        val attemptStartsField = TrackingRepository::class.java.getDeclaredField("attemptStarts")
        attemptStartsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val attemptStarts = attemptStartsField.get(repo) as MutableMap<String, Long>
        attemptStarts["attempt_1"] = System.currentTimeMillis() - 2500

        val updateRes = repo.updateExercise("attempt_1", status = "completed", score = null, durationSec = null)
        assertTrue(updateRes.isSuccess)

        coVerify { api.startAttempt(ExerciseAttemptReq("chat_session", "chat")) }
        coVerify { api.updateAttempt("attempt_1", any()) }

        val computedDuration = updateSlot.captured.durationSec ?: 0
        assertTrue(computedDuration >= 2, "Expected computed duration to be at least 2 seconds")
        assertEquals("completed", updateSlot.captured.status)
    }
}
