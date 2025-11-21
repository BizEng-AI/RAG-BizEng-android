package com.example.myapplication.tracking

import android.app.Application
import com.example.myapplication.data.remote.TrackingApi
import com.example.myapplication.data.remote.dto.ExerciseAttemptDto
import com.example.myapplication.data.remote.dto.ExerciseAttemptReq
import com.example.myapplication.data.remote.dto.ExerciseAttemptUpdate
import com.example.myapplication.data.repository.TrackingRepository
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.uiPack.chat.ChatVm
import com.example.myapplication.utils.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ChatVmAttemptTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val ragRepo = mockk<RagRepository>(relaxed = true)
    private val stt = FakeSpeechToTextController()
    private val tts = FakeTextToSpeechController()
    private val trackingRepo = mockk<TrackingRepository>(relaxed = true)

    @Test
    fun `chat attempt marked abandoned if disposed before assistant reply`() = runTest {
        coEvery { trackingRepo.startExercise(any(), any()) } returns Result.success(ExerciseAttemptDto(
            id = "attempt_chat_1",
            exerciseId = "chat_session",
            exerciseType = "chat",
            status = "started",
            score = null,
            durationSec = null,
            startedAt = "2025-11-20T10:00:00Z",
            finishedAt = null
        ))
        coEvery { trackingRepo.abandonExercise(any(), any(), any(), any()) } returns Result.success(
            ExerciseAttemptDto(
                id = "attempt_chat_1",
                exerciseId = "chat_session",
                exerciseType = "chat",
                status = "abandoned",
                score = null,
                durationSec = 1,
                startedAt = "2025-11-20T10:00:00Z",
                finishedAt = "2025-11-20T10:00:01Z"
            )
        )

        val vm = ChatVm(
            repo = ragRepo,
            stt = stt,
            tts = tts,
            trackingRepository = trackingRepo,
            dispatcher = dispatcherRule.testDispatcher
        )
        vm.onInputChange("Hello")
        vm.send()
        advanceUntilIdle()
        vm.completeAttemptIfNeeded()
        advanceUntilIdle()

        coVerify { trackingRepo.abandonExercise("attempt_chat_1", "chat_session", "chat", null) }
        assertNotNull(vm.state.value.attemptTrackingId)
    }
}
