package com.example.myapplication.tracking

import android.app.Application
import com.example.myapplication.data.remote.dto.RoleplayStartRespDto
import com.example.myapplication.uiPack.roleplay.RoleplayVm
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.data.repository.TrackingRepository
import com.example.myapplication.data.remote.dto.ExerciseAttemptDto
import io.mockk.*
import kotlinx.coroutines.test.runTest
import com.example.myapplication.utils.MainDispatcherRule
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class RoleplayVmAttemptTest {
    private val ragRepo = mockk<RagRepository>(relaxed = true)
    private val stt = FakeSpeechToTextController()
    private val tts = FakeTextToSpeechController()
    private val trackingRepo = mockk<TrackingRepository>(relaxed = true)
    private val dispatcherRule = MainDispatcherRule()

    @Test
    fun `roleplay attempt abandoned when session reset early`() = runTest {
        // Start attempt
        coEvery { trackingRepo.startExercise("client_meeting", "roleplay") } returns Result.success(
            ExerciseAttemptDto(
                id = "attempt_rp_1",
                exerciseId = "client_meeting",
                exerciseType = "roleplay",
                status = "started",
                score = null,
                durationSec = null,
                startedAt = "2025-11-20T10:00:00Z",
                finishedAt = null
            )
        )
        // update abandoned
        coEvery { trackingRepo.abandonExercise("attempt_rp_1", "client_meeting", "roleplay", null) } returns Result.success(
            ExerciseAttemptDto(
                id = "attempt_rp_1",
                exerciseId = "client_meeting",
                exerciseType = "roleplay",
                status = "abandoned",
                score = null,
                durationSec = 10,
                startedAt = "2025-11-20T10:00:00Z",
                finishedAt = "2025-11-20T10:00:10Z"
            )
        )

        val vm = RoleplayVm(
            repo = ragRepo,
            stt = stt,
            tts = tts,
            trackingRepository = trackingRepo,
            dispatcher = dispatcherRule.testDispatcher
        )
        // Mock startRoleplay
        coEvery { ragRepo.startRoleplay("client_meeting", any()) } returns RoleplayStartRespDto(
            sessionId = "sess_1",
            scenarioTitle = "Client Meeting",
            scenarioDescription = "Discuss client needs",
            context = "You are the account manager",
            studentRole = "Account Manager",
            aiRole = "Client Representative",
            currentStage = "opening",
            initialMessage = "Hello, let's begin!"
        )

        vm.startSessionWithScenario("client_meeting")
        // Immediately reset before any completion turn
        vm.resetSession()

        coVerify { trackingRepo.abandonExercise("attempt_rp_1", "client_meeting", "roleplay", null) }
    }

    @Test
    fun `roleplay attempt marked completed on successful interaction and reset`() = runTest {
        // Start attempt
        coEvery { trackingRepo.startExercise("job_interview", "roleplay") } returns Result.success(
            ExerciseAttemptDto(
                id = "attempt_roleplay_1",
                exerciseId = "job_interview",
                exerciseType = "roleplay",
                status = "started",
                score = null,
                durationSec = null,
                startedAt = "2025-11-20T10:00:00Z",
                finishedAt = null
            )
        )
        // update completed
        coEvery { trackingRepo.updateExercise("attempt_roleplay_1", status = "completed", score = null, durationSec = null) } returns Result.success(
            ExerciseAttemptDto(
                id = "attempt_roleplay_1",
                exerciseId = "job_interview",
                exerciseType = "roleplay",
                status = "completed",
                score = null,
                durationSec = 120,
                startedAt = "2025-11-20T10:00:00Z",
                finishedAt = "2025-11-20T10:02:00Z"
            )
        )

        val vm = RoleplayVm(
            repo = ragRepo,
            stt = stt,
            tts = tts,
            trackingRepository = trackingRepo,
            dispatcher = dispatcherRule.testDispatcher
        )
        // Mock startRoleplay
        coEvery { ragRepo.startRoleplay("job_interview", any()) } returns RoleplayStartRespDto(
            sessionId = "sess_2",
            scenarioTitle = "Job Interview",
            scenarioDescription = "Interview for the position",
            context = "You are the interviewer",
            studentRole = "Candidate",
            aiRole = "Interviewer",
            currentStage = "opening",
            initialMessage = "Tell me about yourself."
        )

        vm.startSessionWithScenario("job_interview")
        vm.onInputChange("Hello interviewer")
        vm.send()
        vm.resetSession()

        coVerify { trackingRepo.updateExercise("attempt_roleplay_1", status = "completed", score = null, durationSec = null) }
    }
}
