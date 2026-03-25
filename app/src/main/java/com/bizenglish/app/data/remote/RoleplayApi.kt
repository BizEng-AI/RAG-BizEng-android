package com.bizenglish.app.data.remote

import com.bizenglish.app.core.network.KtorClientProvider
import com.bizenglish.app.data.remote.dto.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class RoleplayApi(private val baseUrl: String) {
    private val client get() = KtorClientProvider.client
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun startRoleplay(scenarioId: String, useRag: Boolean = true): RoleplayStartRespDto {
        val url = "$baseUrl/roleplay/start"
        android.util.Log.d("ROLEPLAY_API", "=== START ROLEPLAY ===")
        android.util.Log.d("ROLEPLAY_API", "URL: $url")
        android.util.Log.d("ROLEPLAY_API", "Scenario: $scenarioId")
        android.util.Log.d("ROLEPLAY_API", "Use RAG: $useRag")

        try {
            val req = RoleplayStartReqDto(
                scenarioId = scenarioId,
                studentName = "Student",
                useRag = useRag
            )

            val resp = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            val raw = resp.bodyAsText()
            android.util.Log.d("ROLEPLAY_API", "Response status: ${resp.status.value}")
            android.util.Log.d("ROLEPLAY_API", "Response body: ${raw.take(300)}")

            if (!resp.status.isSuccess()) {
                android.util.Log.e("ROLEPLAY_API", "=== START ROLEPLAY ERROR ===")
                android.util.Log.e("ROLEPLAY_API", "HTTP ${resp.status.value}: ${resp.status.description}")
                android.util.Log.e("ROLEPLAY_API", "Full response: $raw")
                throw IllegalStateException("HTTP ${resp.status.value}: ${raw.take(500)}")
            }
            return json.decodeFromString<RoleplayStartRespDto>(raw)
        } catch (e: Exception) {
            android.util.Log.e("ROLEPLAY_API", "=== EXCEPTION ===")
            android.util.Log.e("ROLEPLAY_API", "Error: ${e.message}", e)
            throw e
        }
    }

    suspend fun submitTurn(sessionId: String, studentMessage: String): RoleplayTurnRespDto {
        val url = "$baseUrl/roleplay/turn"
        android.util.Log.d("ROLEPLAY_API", "=== SUBMIT TURN ===")
        android.util.Log.d("ROLEPLAY_API", "URL: $url")
        android.util.Log.d("ROLEPLAY_API", "Session ID: $sessionId")
        android.util.Log.d("ROLEPLAY_API", "Student message: $studentMessage")

        try {
            val req = RoleplayTurnReqDto(
                sessionId = sessionId,
                message = studentMessage  // Changed from studentMessage to message
            )

            val resp = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            val raw = resp.bodyAsText()
            android.util.Log.d("ROLEPLAY_API", "Response status: ${resp.status.value}")
            android.util.Log.d("ROLEPLAY_API", "Response body: ${raw.take(300)}")

            if (!resp.status.isSuccess()) {
                android.util.Log.e("ROLEPLAY_API", "=== SUBMIT TURN ERROR ===")
                android.util.Log.e("ROLEPLAY_API", "HTTP ${resp.status.value}: ${resp.status.description}")
                android.util.Log.e("ROLEPLAY_API", "Full response: $raw")
                throw IllegalStateException("HTTP ${resp.status.value}: ${raw.take(500)}")
            }
            return json.decodeFromString<RoleplayTurnRespDto>(raw)
        } catch (e: Exception) {
            android.util.Log.e("ROLEPLAY_API", "=== EXCEPTION ===")
            android.util.Log.e("ROLEPLAY_API", "Error: ${e.message}", e)
            throw e
        }
    }
}

