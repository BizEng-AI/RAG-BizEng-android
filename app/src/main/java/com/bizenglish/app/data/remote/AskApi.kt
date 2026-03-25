package com.bizenglish.app.data.remote

import android.util.Log
import com.bizenglish.app.BuildConfig
import com.bizenglish.app.core.network.KtorClientProvider
import com.bizenglish.app.data.remote.dto.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class AskApi(private val baseUrl: String) {
    private val client get() = KtorClientProvider.client
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun ask(body: AskReqDto): AskRespDto {
        val url = "$baseUrl/ask"
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "POST /ask query=${body.query}")
        }

        try {
            val resp = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            val raw = resp.bodyAsText()

            if (!resp.status.isSuccess()) {
                val detail = runCatching {
                    json.parseToJsonElement(raw).jsonObject["detail"]?.toString()
                }.getOrNull()
                throw IllegalStateException(
                    "HTTP ${resp.status.value}: " + (detail ?: raw.take(500))
                )
            }
            return json.decodeFromString<AskRespDto>(raw)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed /ask: ${e.message}")
            }
            throw e
        }
    }

    suspend fun getVersion(): String {
        val resp = client.get("$baseUrl/version")
        return resp.bodyAsText()
    }

    suspend fun getHealth(): String {
        val resp = client.get("$baseUrl/health")
        return resp.bodyAsText()
    }

    companion object {
        private const val TAG = "ASK_API"
    }
}
