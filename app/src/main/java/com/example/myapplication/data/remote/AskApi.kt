package com.example.myapplication.data.remote
import com.example.myapplication.core.network.KtorClientProvider
import com.example.myapplication.data.remote.dto.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

// data/remote/AskApi.kt
class AskApi(private val baseUrl: String) {
    private val client get() = KtorClientProvider.client
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun ask(body: AskReqDto): AskRespDto {

        val url = "$baseUrl/ask"
        android.util.Log.d("NETWORK", "=== NETWORK REQUEST ===")
        android.util.Log.d("NETWORK", "Attempting to connect to: $url")
        android.util.Log.d("NETWORK", "Request body: query=${body.query}, unit=${body.unit}, k=${body.k}, maxContextChars=${body.maxContextChars}")

        try {
            val resp = client.post("$baseUrl/ask") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            val raw = resp.bodyAsText()

            android.util.Log.d("NETWORK", "Response status=${resp.status.value} body=$raw")

            if (!resp.status.isSuccess()) {
                // Try to surface FastAPI's error shape
                val detail = runCatching {
                    json.parseToJsonElement(raw).jsonObject["detail"]?.toString()
                }.getOrNull()
                throw IllegalStateException(
                    "HTTP ${resp.status.value}: " + (detail ?: raw.take(500))
                )
            }
            return json.decodeFromString<AskRespDto>(raw)
        } catch (e: Exception) {
            android.util.Log.e("NETWORK", "=== NETWORK ERROR ===")
            android.util.Log.e("NETWORK", "Failed to connect to: $url")
            android.util.Log.e("NETWORK", "Error: ${e.message}")
            android.util.Log.e("NETWORK", "Error type: ${e.javaClass.simpleName}")
            throw e
        }
    }

    suspend fun getVersion(): String {
        val url = "$baseUrl/version"
        val resp = client.get(url)
        val raw = resp.bodyAsText()
        android.util.Log.d("JJJJ", "GET /version status=${resp.status.value} body=$raw")
        return raw
    }

    suspend fun getLatestUpdate(): String {
        val url = "$baseUrl/version"
        val resp = client.get(url)
        val raw = resp.bodyAsText()
        android.util.Log.d("kkk", "GET /version status=${resp.status.value} body=$raw")
        return raw
    }

    suspend fun getHealth(): String {
        val url = "$baseUrl/health"
        val resp = client.get(url)
        val raw = resp.bodyAsText()
        android.util.Log.d("kkk", "GET /health status=${resp.status.value} body=$raw")
        return raw
    }
}
