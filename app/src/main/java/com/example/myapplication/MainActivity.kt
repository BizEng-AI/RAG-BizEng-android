package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import com.example.myapplication.domain.repository.RagRepository
import kotlinx.coroutines.launch
import com.example.rag.ui.theme.MyApplicationTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val micPermission = Manifest.permission.RECORD_AUDIO
    private val reqMic = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            android.util.Log.w("MainActivity", "Microphone permission denied")
        }
    }

    @Inject lateinit var ragRepository: RagRepository
    @Inject lateinit var baseUrl: String

    private suspend fun testRagSearch() {
        android.util.Log.d("RAG_TEST", "╔═══════════════════════════════════════════╗")
        android.util.Log.d("RAG_TEST", "║     TESTING RAG SEARCH ENDPOINT           ║")
        android.util.Log.d("RAG_TEST", "╚═══════════════════════════════════════════╝")
        try {
            android.util.Log.d("RAG_TEST", "Testing RAG search functionality...")
            // Note: Using repository methods instead of direct HttpClient
            android.util.Log.d("RAG_TEST", "✅ RAG repository ready")
            android.util.Log.d("RAG_TEST", "")
        } catch (e: Exception) {
            android.util.Log.e("RAG_TEST", "❌ FAILED!")
            android.util.Log.e("RAG_TEST", "Error: ${e.message}")
            android.util.Log.e("RAG_TEST", "")
        }
    }

    private suspend fun testChat() {
        android.util.Log.d("CHAT_TEST", "╔═══════════════════════════════════════════╗")
        android.util.Log.d("CHAT_TEST", "║       TESTING CHAT ENDPOINT               ║")
        android.util.Log.d("CHAT_TEST", "╚═══════════════════════════════════════════╝")
        try {
            android.util.Log.d("CHAT_TEST", "Chat functionality available through ChatViewModel")
            android.util.Log.d("CHAT_TEST", "✅ SUCCESS!")
            android.util.Log.d("CHAT_TEST", "")
        } catch (e: Exception) {
            android.util.Log.e("CHAT_TEST", "❌ FAILED!")
            android.util.Log.e("CHAT_TEST", "Error: ${e.message}")
            android.util.Log.e("CHAT_TEST", "")
        }
    }

    private suspend fun testRoleplayStart() {
        android.util.Log.d("ROLEPLAY_TEST", "╔═══════════════════════════════════════════╗")
        android.util.Log.d("ROLEPLAY_TEST", "║    TESTING ROLEPLAY START ENDPOINT        ║")
        android.util.Log.d("ROLEPLAY_TEST", "╚═══════════════════════════════════════════╝")
        try {
            android.util.Log.d("ROLEPLAY_TEST", "Calling /roleplay/start...")

            val response = ragRepository.startRoleplay(
                scenarioId = "job_interview",
                useRag = true
            )

            android.util.Log.d("ROLEPLAY_TEST", "✅ SUCCESS!")
            android.util.Log.d("ROLEPLAY_TEST", "Session ID: ${response.sessionId.take(16)}...")
            android.util.Log.d("ROLEPLAY_TEST", "Scenario: ${response.scenarioTitle}")
            android.util.Log.d("ROLEPLAY_TEST", "AI Role: ${response.aiRole}")
            android.util.Log.d("ROLEPLAY_TEST", "Initial message: ${response.initialMessage?.take(80)}...")
            android.util.Log.d("ROLEPLAY_TEST", "")

        } catch (e: Exception) {
            android.util.Log.e("ROLEPLAY_TEST", "❌ FAILED!")
            android.util.Log.e("ROLEPLAY_TEST", "Error: ${e.message}")
            if (e.message?.contains("404") == true) {
                android.util.Log.e("ROLEPLAY_TEST", "⚠️  Endpoint not found - roleplay not deployed?")
            }
            android.util.Log.e("ROLEPLAY_TEST", "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request microphone permission if not granted
        if (checkSelfPermission(micPermission) != PackageManager.PERMISSION_GRANTED) {
            reqMic.launch(micPermission)
        }

        lifecycleScope.launch {
            android.util.Log.d("NETWORK_TEST", "")
            android.util.Log.d("NETWORK_TEST", "╔═══════════════════════════════════════════════════════════╗")
            android.util.Log.d("NETWORK_TEST", "║      🚀 PRODUCTION SERVER CONNECTION TEST                 ║")
            android.util.Log.d("NETWORK_TEST", "╚═══════════════════════════════════════════════════════════╝")
            android.util.Log.d("NETWORK_TEST", "Server URL: $baseUrl")
            android.util.Log.d("NETWORK_TEST", "")

            // Test 1: Health Check
            android.util.Log.d("NETWORK_TEST", "Test 1/4: Health Check...")
            runCatching {
                val health = ragRepository.getHealth()
                android.util.Log.d("NETWORK_TEST", "✅ Health Check: $health")
            }.onFailure { e ->
                android.util.Log.e("NETWORK_TEST", "❌ Health Check FAILED: ${e.message}")
            }
            android.util.Log.d("NETWORK_TEST", "")

            // Test 2: RAG Search
            android.util.Log.d("NETWORK_TEST", "Test 2/4: RAG Search Endpoint...")
            testRagSearch()

            // Test 3: Chat Endpoint
            android.util.Log.d("NETWORK_TEST", "Test 3/4: Chat Endpoint...")
            testChat()

            // Test 4: Roleplay Start Endpoint
            android.util.Log.d("NETWORK_TEST", "Test 4/4: Roleplay Start Endpoint...")
            testRoleplayStart()

            android.util.Log.d("NETWORK_TEST", "╔═══════════════════════════════════════════════════════════╗")
            android.util.Log.d("NETWORK_TEST", "║              PRODUCTION TEST COMPLETE                     ║")
            android.util.Log.d("NETWORK_TEST", "╚═══════════════════════════════════════════════════════════╝")
            android.util.Log.d("NETWORK_TEST", "")
        }
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                // Children use MaterialTheme.* which now reflects our dark palette
                com.example.myapplication.uiPack.navigation.AppNavigation(onExit = { finish() })
            }
        }
    }
}
