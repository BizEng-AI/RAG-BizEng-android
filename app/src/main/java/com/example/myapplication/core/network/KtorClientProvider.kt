package com.example.myapplication.core.network
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import android.util.Log

object KtorClientProvider {
    private const val TAG = "🌐 HTTP_CLIENT"

    val client = HttpClient(Android) {
        expectSuccess = false

        // 🔥 MAXIMUM LOGGING - SEE EVERYTHING 🔥
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, "════════════════════════════════════════")
                    Log.d(TAG, message)
                    Log.d(TAG, "════════════════════════════════════════")
                }
            }
            level = LogLevel.ALL  // Log EVERYTHING - headers, body, everything!
        }

        install(HttpTimeout) {
            // 🚀 EXTENDED TIMEOUTS FOR FLY.IO COLD START
            // Fly.io can take 30-60 seconds to wake up from sleep
            requestTimeoutMillis = 90_000  // 90 seconds (was 30)
            connectTimeoutMillis = 30_000  // 30 seconds (was 10)
            socketTimeoutMillis = 90_000   // 90 seconds (was 30)
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
                coerceInputValues = true
            })
        }
    }
}
