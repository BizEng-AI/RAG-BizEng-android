package com.example.myapplication.data.remote

import com.example.myapplication.core.network.KtorClientProvider
import com.example.myapplication.data.remote.dto.*
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

class ChatApi(private val baseUrl: String) {
    private val client get() = KtorClientProvider.client
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
        android.util.Log.d("CHAT_API", "╔═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ CHAT REQUEST STARTING")
        android.util.Log.d("CHAT_API", "╠═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ URL: $baseUrl/chat")
        android.util.Log.d("CHAT_API", "║ Method: POST")
        android.util.Log.d("CHAT_API", "║ Content-Type: application/json")
        android.util.Log.d("CHAT_API", "╠═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ REQUEST BODY:")
        android.util.Log.d("CHAT_API", "║ Messages count: ${req.messages.size}")
        req.messages.forEachIndexed { index, msg ->
            android.util.Log.d("CHAT_API", "║   [$index] role='${msg.role}' content='${msg.content.take(100)}${if(msg.content.length > 100) "..." else ""}'")
        }
        android.util.Log.d("CHAT_API", "╠═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ Raw JSON payload: $req")
        android.util.Log.d("CHAT_API", "╚═══════════════════════════════════════════════════════════")

        val resp = client.post("$baseUrl/chat") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }

        val raw = resp.bodyAsText()

        android.util.Log.d("CHAT_API", "╔═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ CHAT RESPONSE RECEIVED")
        android.util.Log.d("CHAT_API", "╠═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ Status Code: ${resp.status.value} ${resp.status.description}")
        android.util.Log.d("CHAT_API", "║ Is Success: ${resp.status.isSuccess()}")
        android.util.Log.d("CHAT_API", "╠═══════════════════════════════════════════════════════════")
        android.util.Log.d("CHAT_API", "║ RESPONSE BODY:")
        android.util.Log.d("CHAT_API", "║ $raw")
        android.util.Log.d("CHAT_API", "╚═══════════════════════════════════════════════════════════")

        if (!resp.status.isSuccess()) {
            android.util.Log.e("CHAT_API", "❌ ERROR: HTTP ${resp.status.value}")
            android.util.Log.e("CHAT_API", "❌ Response body: ${raw.take(500)}")
            throw IllegalStateException("HTTP ${resp.status.value}: " + raw.take(500))
        }

        val decoded = Json.decodeFromString<ChatRespDto>(raw)
        android.util.Log.d("CHAT_API", "✅ Successfully decoded response")
        android.util.Log.d("CHAT_API", "✅ Answer length: ${decoded.answer.length} chars")
        android.util.Log.d("CHAT_API", "✅ Sources count: ${decoded.sources.size}")

        return decoded
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
