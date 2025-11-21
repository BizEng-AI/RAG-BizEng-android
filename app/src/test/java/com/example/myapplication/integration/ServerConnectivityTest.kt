package com.example.myapplication.integration

import com.example.myapplication.core.network.KtorClientProvider
import com.example.myapplication.data.remote.dto.RegisterReq
import com.example.myapplication.data.remote.dto.TokenResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration test for server connectivity and endpoint validation
 * Run this test to diagnose server issues
 */
class ServerConnectivityTest {

    private val baseUrl = "https://bizeng-server.fly.dev"
    private val client = KtorClientProvider.client
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test server health endpoint`() = runBlocking {
        try {
            val response = client.get("$baseUrl/health")
            println("✅ Health endpoint status: ${response.status}")
            println("✅ Health response: ${response.bodyAsText()}")

            assertTrue("Health endpoint should return 200", response.status.isSuccess())
        } catch (e: Exception) {
            fail("❌ Health endpoint failed: ${e.message}")
        }
    }

    @Test
    fun `test registration endpoint returns correct format`() = runBlocking {
        val testEmail = "test_${System.currentTimeMillis()}@test.com"
        val request = RegisterReq(
            email = testEmail,
            password = "Test123!",
            displayName = "Test User",
            groupNumber = null
        )

        try {
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val rawBody = response.bodyAsText()
            println("📥 Registration response status: ${response.status}")
            println("📥 Registration response body: $rawBody")

            // Check if response is successful
            assertTrue("Registration should return 200/201",
                response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created)

            // Try to parse the response
            try {
                val tokenResponse = json.decodeFromString<TokenResponse>(rawBody)
                println("✅ Response parsed successfully")
                println("✅ Access token present: ${tokenResponse.accessToken != null}")
                println("✅ Refresh token present: ${tokenResponse.refreshToken != null}")

                // Validate tokens are present
                assertNotNull("Access token should not be null", tokenResponse.accessToken)
                assertNotNull("Refresh token should not be null", tokenResponse.refreshToken)
                assertTrue("Access token should not be blank", !tokenResponse.accessToken.isNullOrBlank())
                assertTrue("Refresh token should not be blank", !tokenResponse.refreshToken.isNullOrBlank())

            } catch (e: Exception) {
                fail("❌ Failed to parse response as TokenResponse: ${e.message}\nRaw response: $rawBody")
            }

        } catch (e: Exception) {
            fail("❌ Registration request failed: ${e.message}")
        }
    }

    @Test
    fun `diagnose what server actually returns`() = runBlocking {
        val testEmail = "diagnostic_${System.currentTimeMillis()}@test.com"

        println("═══════════════════════════════════════════════════════")
        println("🔍 DIAGNOSTIC TEST - What does the server return?")
        println("═══════════════════════════════════════════════════════")
        println("Test email: $testEmail")
        println("Target URL: $baseUrl/auth/register")
        println("")

        try {
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "email": "$testEmail",
                        "password": "Test123!",
                        "display_name": "Diagnostic Test"
                    }
                """.trimIndent())
            }

            val rawBody = response.bodyAsText()
            val headers = response.headers.entries().joinToString("\n") { "  ${it.key}: ${it.value}" }

            println("📥 HTTP Status: ${response.status.value} ${response.status.description}")
            println("📥 Response Headers:")
            println(headers)
            println("")
            println("📥 Raw Response Body:")
            println(rawBody)
            println("")

            // Analyze the response
            println("═══════════════════════════════════════════════════════")
            println("📊 ANALYSIS:")
            println("═══════════════════════════════════════════════════════")

            when {
                rawBody.isBlank() -> {
                    println("❌ Response is EMPTY")
                    println("   The server is not returning any data")
                }
                rawBody == "{}" -> {
                    println("❌ Response is EMPTY OBJECT {}")
                    println("   The server returns empty JSON")
                }
                !rawBody.contains("access_token") -> {
                    println("❌ Response does NOT contain 'access_token' field")
                    println("   The server is not configured to return tokens")
                }
                !rawBody.contains("refresh_token") -> {
                    println("❌ Response does NOT contain 'refresh_token' field")
                    println("   The server is missing refresh token")
                }
                else -> {
                    println("✅ Response appears to contain token fields")
                    try {
                        val tokenResponse = json.decodeFromString<TokenResponse>(rawBody)
                        println("✅ Successfully parsed as TokenResponse")
                        println("   Access Token: ${if (tokenResponse.accessToken.isNullOrBlank()) "MISSING" else "Present"}")
                        println("   Refresh Token: ${if (tokenResponse.refreshToken.isNullOrBlank()) "MISSING" else "Present"}")
                    } catch (e: Exception) {
                        println("❌ Failed to parse: ${e.message}")
                    }
                }
            }

            println("═══════════════════════════════════════════════════════")

        } catch (e: Exception) {
            println("❌ Request failed completely: ${e.message}")
            e.printStackTrace()
        }
    }
}

