package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.uiPack.chat.ChatScreen
import com.example.myapplication.uiPack.chat.ChatVm
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import com.example.myapplication.domain.repository.RagRepository
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request microphone permission if not granted
        if (checkSelfPermission(micPermission) != PackageManager.PERMISSION_GRANTED) {
            reqMic.launch(micPermission)
        }

        lifecycleScope.launch {
            android.util.Log.d("NETWORK_TEST", "=== Testing server connection ===")
            android.util.Log.d("NETWORK_TEST", "Attempting to connect to server...")
            runCatching {
                ragRepository.getHealth()
                android.util.Log.d("NETWORK_TEST", "✓ Server connection successful!")
            }.onFailure { e ->
                android.util.Log.e("NETWORK_TEST", "✗ Server connection FAILED!")
                android.util.Log.e("NETWORK_TEST", "Error: ${e.message}")
                android.util.Log.e("NETWORK_TEST", "")
                android.util.Log.e("NETWORK_TEST", "Troubleshooting:")
                android.util.Log.e("NETWORK_TEST", "1. Is the Python server running on port 8020?")
                android.util.Log.e("NETWORK_TEST", "2. Is it listening on 0.0.0.0 (not localhost)?")
                android.util.Log.e("NETWORK_TEST", "3. Did you add Windows Firewall exception?")
                android.util.Log.e("NETWORK_TEST", "4. Are both devices on the same Wi-Fi?")
            }
        }
        setContent {
            MaterialTheme {
                com.example.myapplication.uiPack.navigation.MainNavigation()
            }
        }
    }
}
