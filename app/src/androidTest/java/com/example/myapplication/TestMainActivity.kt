package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.uiPack.navigation.MainNavigation
import com.example.rag.ui.theme.MyApplicationTheme

/**
 * Test-only activity that bypasses AppNavigation (Splash/Login/Profile)
 * and goes straight to MainNavigation (Home with bottom tabs).
 */
class TestMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                MainNavigation(
                    onLogout = { finish() },
                    onExit = { finish() },
                    onOpenProfile = { /* no-op in tests */ }
                )
            }
        }
    }
}
