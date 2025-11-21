package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import java.net.SocketTimeoutException

class RoleplayApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Retry logic for handling Fly.io cold starts
     * - First attempt: Immediate
     * - Retry 1: Wait 5s (server waking up)
     * - Retry 2: Wait 10s (final attempt)
     * - Total attempts: 3 (enough for slow cold starts)
     */
    private suspend fun <T> retryOnTimeout(
        maxRetries: Int = 2,  // Increased from 1 to 2
        initialDelayMs: Long = 5000,  // Increased from 2000 to 5000
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                if (attempt > 0) {
                    val delayMs = initialDelayMs * (1 shl (attempt - 1)) // exponential backoff
                    android.util.Log.d("ROLEPLAY_API", "⏳ Retry attempt $attempt after ${delayMs}ms delay (server may be waking up...)")
                    delay(delayMs)
                }
                return block()
            } catch (e: Exception) {
                lastException = e
                val isTimeoutError = e is SocketTimeoutException ||
                                   e.message?.contains("timeout", ignoreCase = true) == true ||
                                   e.message?.contains("timed out", ignoreCase = true) == true

                if (isTimeoutError && attempt < maxRetries) {
                    android.util.Log.w("ROLEPLAY_API", "⚠️ Request timeout on attempt ${attempt + 1}. Retrying...")
                    // Continue to next retry
                } else {
                    // Not a timeout error, or out of retries
                    throw e
                }
            }
        }

        // Should never reach here, but just in case
        throw lastException ?: IllegalStateException("Retry failed")
    }

    suspend fun startRoleplay(scenarioId: String, useRag: Boolean = true): RoleplayStartRespDto = retryOnTimeout {
        val requestId = "start_${System.currentTimeMillis()}"
        val url = "$baseUrl/roleplay/start"
        android.util.Log.d("ROLEPLAY_API", "=== START ROLEPLAY ===")
        android.util.Log.d("ROLEPLAY_API", "Request ID: $requestId")
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
                headers {
                    append("X-Request-ID", requestId)
                }
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
            json.decodeFromString<RoleplayStartRespDto>(raw)
        } catch (e: Exception) {
            android.util.Log.e("ROLEPLAY_API", "=== EXCEPTION ===")
            android.util.Log.e("ROLEPLAY_API", "Error: ${e.message}", e)
            throw e
        }
    }

    suspend fun submitTurn(sessionId: String, studentMessage: String): RoleplayTurnRespDto = retryOnTimeout {
        val requestId = "turn_${System.currentTimeMillis()}"
        val url = "$baseUrl/roleplay/turn"
        android.util.Log.d("ROLEPLAY_API", "=== SUBMIT TURN ===")
        android.util.Log.d("ROLEPLAY_API", "Request ID: $requestId")
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
                headers {
                    append("X-Request-ID", requestId)
                }
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
            json.decodeFromString<RoleplayTurnRespDto>(raw)
        } catch (e: Exception) {
            android.util.Log.e("ROLEPLAY_API", "=== EXCEPTION ===")
            android.util.Log.e("ROLEPLAY_API", "Error: ${e.message}", e)
            throw e
        }
    }
}

