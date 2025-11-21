package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.request.forms.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.utils.io.*
import java.io.File

class ChatApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    // SSE stream
    suspend fun streamChat(req: ChatReqDto, onDelta: (String) -> Unit) {
        val httpResp = client.preparePost("$baseUrl/chat/stream") {
            contentType(ContentType.Application.Json)
            setBody(req)
            header(HttpHeaders.Accept, "text/event-stream")
        }.execute()
        val ch = httpResp.bodyAsChannel()
        val buf = ByteArray(8192)
        while (!ch.isClosedForRead) {
            val n = ch.readAvailable(buf, 0, buf.size)
            if (n > 0) {
                val text = buf.decodeToString(endIndex = n)
                text.lineSequence().forEach { line ->
                    if (line.startsWith("data: ")) {
                        val payload = line.removePrefix("data: ").trim()
                        val delta = runCatching {
                            Json.parseToJsonElement(payload).jsonObject["delta"]?.jsonPrimitive?.content
                        }.getOrNull()
                        if (delta != null) onDelta(delta)
                    }
                }
            }
        }
    }

    // Free chat endpoint
    suspend fun chat(req: ChatReqDto): ChatRespDto {
        // Retry once on transient 'unexpected end of stream'
        var lastError: Throwable? = null
        repeat(2) { attempt ->
            try {
                val resp = client.post("$baseUrl/chat") {
                    contentType(ContentType.Application.Json)
                    setBody(req)
                }
                val raw = resp.bodyAsText()
                if (!resp.status.isSuccess()) {
                    throw IllegalStateException("HTTP ${resp.status.value}: " + raw.take(500))
                }
                return Json.decodeFromString<ChatRespDto>(raw)
            } catch (t: Throwable) {
                lastError = t
                val msg = t.message ?: ""
                val transient = msg.contains("unexpected end of stream", ignoreCase = true) || msg.contains("EOF", ignoreCase = true)
                if (!transient || attempt == 1) throw t
                android.util.Log.w("CHAT_API", "Transient stream error, retrying once... ${t.message}")
            }
        }
        throw lastError ?: IllegalStateException("Unknown chat failure")
    }

    // STT
    suspend fun transcribe(file: File): String {
        val resp = client.post("$baseUrl/stt") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file",
                            file.readBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "audio/wav")
                                append(HttpHeaders.ContentDisposition, "filename=\"speech.wav\"")
                            }
                        )
                    }
                )
            )
        }
        val raw = resp.bodyAsText()
        return Json.parseToJsonElement(raw).jsonObject["text"]!!.jsonPrimitive.content
    }

    // TTS -> bytes
    suspend fun tts(text: String): ByteArray {
        val resp = client.post("$baseUrl/tts") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(listOf("text" to text).formUrlEncode())
        }
        return resp.body()
    }
}
