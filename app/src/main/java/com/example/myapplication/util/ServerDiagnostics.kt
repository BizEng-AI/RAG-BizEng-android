package com.example.myapplication.util

import android.util.Log
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Utility class for diagnosing server connection and response issues
 */
object ServerDiagnostics {
    private const val TAG = "🔍 ServerDiagnostics"

    @Serializable
    data class DiagnosticResult(
        val endpoint: String,
        val status: Int,
        val responseBody: String,
        val headers: Map<String, String>,
        val success: Boolean,
        val error: String? = null
    )

    /**
     * Test server health endpoint
     */
    suspend fun testHealth(client: HttpClient, baseUrl: String): DiagnosticResult {
        Log.d(TAG, "Testing health endpoint: $baseUrl/health")
        return try {
            val response = client.get("$baseUrl/health")
            val body = response.bodyAsText()
            DiagnosticResult(
                endpoint = "$baseUrl/health",
                status = response.status.value,
                responseBody = body,
                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                success = response.status.isSuccess()
            )
        } catch (e: Exception) {
            DiagnosticResult(
                endpoint = "$baseUrl/health",
                status = 0,
                responseBody = "",
                headers = emptyMap(),
                success = false,
                error = e.message
            )
        }
    }

    /**
     * Test registration endpoint with dummy data
     */
    suspend fun testRegister(client: HttpClient, baseUrl: String): DiagnosticResult {
        Log.d(TAG, "Testing register endpoint: $baseUrl/auth/register")
        val testEmail = "diagnostic_${System.currentTimeMillis()}@test.com"
        val requestBody = """{"email":"$testEmail","password":"test123","display_name":"Diagnostic Test"}"""

        return try {
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body = response.bodyAsText()

            Log.d(TAG, "Register response status: ${response.status}")
            Log.d(TAG, "Register response body: $body")

            DiagnosticResult(
                endpoint = "$baseUrl/auth/register",
                status = response.status.value,
                responseBody = body,
                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                success = response.status.isSuccess() && body.contains("access_token")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Register test failed: ${e.message}", e)
            DiagnosticResult(
                endpoint = "$baseUrl/auth/register",
                status = 0,
                responseBody = "",
                headers = emptyMap(),
                success = false,
                error = e.message
            )
        }
    }

    /**
     * Run full diagnostics and log results
     */
    suspend fun runFullDiagnostics(client: HttpClient, baseUrl: String) {
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "🔍 STARTING SERVER DIAGNOSTICS")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "Target Server: $baseUrl")
        Log.d(TAG, "")

        // Test 1: Health
        Log.d(TAG, "Test 1: Health Check")
        val healthResult = testHealth(client, baseUrl)
        logResult(healthResult)

        // Test 2: Register
        Log.d(TAG, "")
        Log.d(TAG, "Test 2: Registration Endpoint")
        val registerResult = testRegister(client, baseUrl)
        logResult(registerResult)

        // Summary
        Log.d(TAG, "")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "📊 DIAGNOSTICS SUMMARY")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "Health: ${if (healthResult.success) "✅ PASS" else "❌ FAIL"}")
        Log.d(TAG, "Register: ${if (registerResult.success) "✅ PASS" else "❌ FAIL"}")

        if (!registerResult.success) {
            Log.e(TAG, "")
            Log.e(TAG, "⚠️  REGISTRATION ENDPOINT ISSUE DETECTED!")
            Log.e(TAG, "")
            if (registerResult.error != null) {
                Log.e(TAG, "Error: ${registerResult.error}")
            } else {
                Log.e(TAG, "The server is responding but NOT returning tokens.")
                Log.e(TAG, "Response body: ${registerResult.responseBody}")
                Log.e(TAG, "")
                Log.e(TAG, "🔧 SERVER FIX REQUIRED:")
                Log.e(TAG, "The /auth/register endpoint MUST return:")
                Log.e(TAG, "{")
                Log.e(TAG, "  \"access_token\": \"<jwt_token>\",")
                Log.e(TAG, "  \"refresh_token\": \"<jwt_token>\",")
                Log.e(TAG, "  \"token_type\": \"bearer\"")
                Log.e(TAG, "}")
            }
        }
        Log.d(TAG, "═══════════════════════════════════════════════════════")
    }

    private fun logResult(result: DiagnosticResult) {
        Log.d(TAG, "  Endpoint: ${result.endpoint}")
        Log.d(TAG, "  Status: ${result.status}")
        Log.d(TAG, "  Success: ${result.success}")
        if (result.error != null) {
            Log.e(TAG, "  Error: ${result.error}")
        }
        Log.d(TAG, "  Response Body: ${result.responseBody.take(200)}${if (result.responseBody.length > 200) "..." else ""}")
    }
}

