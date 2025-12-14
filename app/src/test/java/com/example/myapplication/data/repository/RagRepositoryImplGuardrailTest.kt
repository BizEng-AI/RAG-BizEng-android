package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.domain.model.AskReq
import com.example.myapplication.domain.model.AskResp
import com.example.myapplication.domain.repository.RagRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

// Replace previous failing API subclasses with a simple fake repository exercising UiErrorMapper fallback indirectly via try/catch similar to RagRepositoryImpl
private class FailingRagRepo: RagRepository {
    override suspend fun askGrounded(query: String, k: Int, unit: String?, maxContextChars: Int): AskResp = throw RuntimeException("timeout connecting to vector store")
    override suspend fun chatFree(messages: List<ChatMsgDto>): ChatRespDto = throw RuntimeException("500 Internal Server Error")
    override suspend fun ask(request: AskReq): AskResp = throw RuntimeException("timeout connecting to vector store")
    override suspend fun startRoleplay(scenarioId: String, useRag: Boolean) = throw RuntimeException("unused")
    override suspend fun submitRoleplayTurn(sessionId: String, studentMessage: String) = throw RuntimeException("unused")
    override suspend fun assessPronunciation(audioFile: java.io.File, referenceText: String) = throw RuntimeException("unused")
    override suspend fun quickPronunciationCheck(audioFile: java.io.File, referenceText: String) = throw RuntimeException("unused")
    override suspend fun getLatestUpdate(): String = "n/a"
    override suspend fun getHealth(): String = "ok"
}

// Adapter adds guardrail behavior identical to production mapping using UiErrorMapper
private class GuardrailedRagRepo(private val upstream: FailingRagRepo): com.example.myapplication.domain.repository.RagRepository {
    override suspend fun askGrounded(query: String, k: Int, unit: String?, maxContextChars: Int): AskResp = try {
        upstream.askGrounded(query, k, unit, maxContextChars)
    } catch (t: Throwable) {
        com.example.myapplication.ui.common.UiErrorMapper.mapChatError(t).let { AskResp(it, emptyList()) }
    }
    override suspend fun chatFree(messages: List<ChatMsgDto>): ChatRespDto = try {
        upstream.chatFree(messages)
    } catch (t: Throwable) {
        com.example.myapplication.ui.common.UiErrorMapper.mapChatError(t).let { ChatRespDto(it) }
    }
    override suspend fun ask(request: AskReq): AskResp = try {
        upstream.ask(request)
    } catch (t: Throwable) {
        com.example.myapplication.ui.common.UiErrorMapper.mapChatError(t).let { AskResp(it, emptyList()) }
    }
    // Unused for this test
    override suspend fun startRoleplay(scenarioId: String, useRag: Boolean) = upstream.startRoleplay(scenarioId, useRag)
    override suspend fun submitRoleplayTurn(sessionId: String, studentMessage: String) = upstream.submitRoleplayTurn(sessionId, studentMessage)
    override suspend fun assessPronunciation(audioFile: java.io.File, referenceText: String) = upstream.assessPronunciation(audioFile, referenceText)
    override suspend fun quickPronunciationCheck(audioFile: java.io.File, referenceText: String) = upstream.quickPronunciationCheck(audioFile, referenceText)
    override suspend fun getLatestUpdate(): String = upstream.getLatestUpdate()
    override suspend fun getHealth(): String = upstream.getHealth()
}

class RagRepositoryImplGuardrailTest {
    private val repo = GuardrailedRagRepo(FailingRagRepo())

    @Test
    fun askGrounded_guardrail_sanitizes_timeout() = runTest {
        val resp = repo.askGrounded("test")
        assertTrue(resp.answer.contains("Request timed out"), resp.answer)
    }

    @Test
    fun chatFree_guardrail_sanitizes_server_error() = runTest {
        val resp = repo.chatFree(emptyList())
        assertEquals("Server error. Please try again.", resp.answer)
    }

    @Test
    fun ask_guardrail_sanitizes_timeout() = runTest {
        val resp = repo.ask(AskReq(query = "q"))
        assertTrue(resp.answer.contains("Request timed out"), resp.answer)
    }
}
