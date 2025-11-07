package com.example.myapplication.data.remote

import android.util.Log
import com.example.myapplication.data.remote.dto.PronunciationResultDto
import com.example.myapplication.data.remote.dto.PronunciationQuickCheckDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PronunciationApi @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun assessPronunciation(audioFile: File, referenceText: String): PronunciationResultDto {
        Log.d("PRONUNCIATION_API", "=== PRONUNCIATION ASSESSMENT ===")
        Log.d("PRONUNCIATION_API", "URL: $baseUrl/pronunciation/assess")
        Log.d("PRONUNCIATION_API", "Reference text: $referenceText")
        Log.d("PRONUNCIATION_API", "Audio file: ${audioFile.name}, size: ${audioFile.length()} bytes")

        try {
            val response = client.submitFormWithBinaryData(
                url = "$baseUrl/pronunciation/assess",
                formData = formData {
                    append("reference_text", referenceText)
                    append("audio", audioFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "audio/wav")
                        append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                    })
                }
            )

            val status = response.status.value
            val bodyText = response.body<String>()

            Log.d("PRONUNCIATION_API", "Response status: $status")
            Log.d("PRONUNCIATION_API", "Response body: ${bodyText.take(200)}...")

            if (status == 200) {
                val result = json.decodeFromString<PronunciationResultDto>(bodyText)
                Log.d("PRONUNCIATION_API", "✓ Assessment successful")
                Log.d("PRONUNCIATION_API", "  Transcript: ${result.transcript}")
                Log.d("PRONUNCIATION_API", "  Overall score: ${result.pronunciationScore}/100")
                Log.d("PRONUNCIATION_API", "  Accuracy: ${result.accuracyScore}/100")
                Log.d("PRONUNCIATION_API", "  Fluency: ${result.fluencyScore}/100")
                Log.d("PRONUNCIATION_API", "  Completeness: ${result.completenessScore}/100")
                Log.d("PRONUNCIATION_API", "  Feedback: ${result.feedback}")
                Log.d("PRONUNCIATION_API", "  Words with IPA: ${result.words.count { it.ipaExpected != null }}")
                return result
            } else {
                Log.e("PRONUNCIATION_API", "✗ Assessment failed with status $status")
                Log.e("PRONUNCIATION_API", "Error body: $bodyText")
                throw IllegalStateException("HTTP $status: $bodyText")
            }
        } catch (e: Exception) {
            Log.e("PRONUNCIATION_API", "=== EXCEPTION ===")
            Log.e("PRONUNCIATION_API", "Error type: ${e.javaClass.simpleName}")
            Log.e("PRONUNCIATION_API", "Error message: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun quickCheck(audioFile: File, referenceText: String): PronunciationQuickCheckDto {
        Log.d("PRONUNCIATION_API", "=== QUICK PRONUNCIATION CHECK ===")
        Log.d("PRONUNCIATION_API", "URL: $baseUrl/pronunciation/quick-check")
        Log.d("PRONUNCIATION_API", "Reference text: $referenceText")

        try {
            val response = client.submitFormWithBinaryData(
                url = "$baseUrl/pronunciation/quick-check",
                formData = formData {
                    append("reference_text", referenceText)
                    append("audio", audioFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "audio/wav")
                        append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                    })
                }
            )

            val status = response.status.value
            val bodyText = response.body<String>()

            Log.d("PRONUNCIATION_API", "Response status: $status")
            Log.d("PRONUNCIATION_API", "Response body: $bodyText")

            if (status == 200) {
                val result = json.decodeFromString<PronunciationQuickCheckDto>(bodyText)
                Log.d("PRONUNCIATION_API", "✓ Quick check successful")
                Log.d("PRONUNCIATION_API", "  Score: ${result.score}/100")
                Log.d("PRONUNCIATION_API", "  Needs practice: ${result.needs_practice}")
                return result
            } else {
                Log.e("PRONUNCIATION_API", "✗ Quick check failed with status $status")
                throw IllegalStateException("HTTP $status: $bodyText")
            }
        } catch (e: Exception) {
            Log.e("PRONUNCIATION_API", "=== EXCEPTION ===", e)
            throw e
        }
    }

    suspend fun testService(): Boolean {
        Log.d("PRONUNCIATION_API", "Testing pronunciation service...")
        try {
            val response = client.get("$baseUrl/pronunciation/test")
            val status = response.status.value
            val bodyText = response.body<String>()

            Log.d("PRONUNCIATION_API", "Test response: $status - $bodyText")
            return status == 200
        } catch (e: Exception) {
            Log.e("PRONUNCIATION_API", "Test failed", e)
            return false
        }
    }
}

