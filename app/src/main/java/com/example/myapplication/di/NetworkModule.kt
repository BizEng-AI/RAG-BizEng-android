package com.example.myapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import io.ktor.client.*
import com.example.myapplication.core.network.KtorClientProvider

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideBaseUrl(): String {
        // ========================================================================
        // NETWORK CONFIGURATION FOR DEPLOYMENT
        // ========================================================================
        //
        // CHOOSE ONE MODE:
        //
        // MODE 1: DEVELOPMENT (localhost via ADB) - For testing on your dev device
        //   - useLocalhost = true
        //   - Requires: adb reverse tcp:8020 tcp:8020
        //   - Only works on USB-connected device
        //
        // MODE 2: PRODUCTION (Wi-Fi IP) - For distributing APK to other devices
        //   - useLocalhost = false
        //   - Set PRODUCTION_SERVER_IP to your computer's Wi-Fi IP
        //   - All devices must be on the SAME Wi-Fi network
        //   - To find your IP: Open CMD → run "ipconfig" → look for "Wireless LAN adapter Wi-Fi" IPv4
        //   - Example: 192.168.1.100
        //
        // MODE 3: PRODUCTION (Public URL) - For internet-accessible server
        //   - useLocalhost = false
        //   - Set PRODUCTION_SERVER_IP to your public domain/IP
        //   - Example: "myserver.com" or "203.0.113.10"
        //   - Server must be accessible from internet
        //
        // ========================================================================

        val useLocalhost = false  // ⚠️ SET TO FALSE FOR APK DISTRIBUTION!

        // ========================================================================
        // 👇 DEPLOYMENT CONFIGURATION
        // ========================================================================
        //
        // FOR LOCAL NETWORK (Same Wi-Fi):
        //   PRODUCTION_SERVER_IP = "10.177.165.92" (your computer's IP)
        //   SERVER_PORT = "8020"
        //   USE_HTTPS = false
        //
        // FOR ONLINE DEPLOYMENT (ngrok/Railway/Render):
        //   PRODUCTION_SERVER_IP = "abc123.ngrok.io" (without http:// or https://)
        //   SERVER_PORT = "" (leave empty)
        //   USE_HTTPS = true
        //
        // ========================================================================

        val PRODUCTION_SERVER_IP = "colette-unvoluble-nonsynoptically.ngrok-free.dev"  // 👈 Your ngrok URL
        val SERVER_PORT = ""  // 👈 Empty for online deployment
        val USE_HTTPS = true  // 👈 ngrok uses HTTPS

        val serverUrl = if (useLocalhost) {
            "http://localhost:$SERVER_PORT"  // Development only
        } else {
            // Production mode
            if (SERVER_PORT.isEmpty()) {
                // Online deployment (ngrok, Railway, Render, etc.)
                val protocol = if (USE_HTTPS) "https" else "http"
                "$protocol://$PRODUCTION_SERVER_IP"
            } else {
                // Local network deployment
                "http://$PRODUCTION_SERVER_IP:$SERVER_PORT"
            }
        }

        android.util.Log.d("NETWORK_CONFIG", "╔═══════════════════════════════════════════════════════════╗")
        android.util.Log.d("NETWORK_CONFIG", "║           SERVER CONNECTION CONFIGURATION                 ║")
        android.util.Log.d("NETWORK_CONFIG", "╚═══════════════════════════════════════════════════════════╝")
        android.util.Log.d("NETWORK_CONFIG", "")
        android.util.Log.d("NETWORK_CONFIG", "📡 Server URL: $serverUrl")
        android.util.Log.d("NETWORK_CONFIG", "🔧 Mode: ${if (useLocalhost) "Development (localhost)" else "Production (Wi-Fi/Internet)"}")
        android.util.Log.d("NETWORK_CONFIG", "")
        if (!useLocalhost) {
            android.util.Log.d("NETWORK_CONFIG", "✅ APK Distribution Mode Active")
            android.util.Log.d("NETWORK_CONFIG", "")
            android.util.Log.d("NETWORK_CONFIG", "Requirements for other devices:")
            android.util.Log.d("NETWORK_CONFIG", "  • All devices must be on same Wi-Fi network")
            android.util.Log.d("NETWORK_CONFIG", "  • Server IP: $PRODUCTION_SERVER_IP")
            android.util.Log.d("NETWORK_CONFIG", "  • Server must be running on port $SERVER_PORT")
            android.util.Log.d("NETWORK_CONFIG", "  • Windows Firewall must allow port $SERVER_PORT")
        } else {
            android.util.Log.d("NETWORK_CONFIG", "⚠️  Development Mode - localhost only")
            android.util.Log.d("NETWORK_CONFIG", "")
            android.util.Log.d("NETWORK_CONFIG", "To set it up (in Android Studio Terminal):")
            android.util.Log.d("NETWORK_CONFIG", "  1. adb devices")
            android.util.Log.d("NETWORK_CONFIG", "  2. adb reverse tcp:$SERVER_PORT tcp:$SERVER_PORT")
        }
        android.util.Log.d("NETWORK_CONFIG", "")
        android.util.Log.d("NETWORK_CONFIG", "═══════════════════════════════════════════════════════════")

        return serverUrl
    }

    @Provides @Singleton
    fun provideHttpClient(): HttpClient = KtorClientProvider.client
}
