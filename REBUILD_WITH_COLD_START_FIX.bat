@echo off
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║         COLD START FIX APPLIED - REBUILD REQUIRED        ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo ✅ FIXES APPLIED:
echo.
echo 1. Timeout increased: 30s → 90s (for Fly.io cold start)
echo 2. Retry logic added: Auto-retry on timeout
echo 3. Exponential backoff: Wait 2s before retry
echo.
echo FILES CHANGED:
echo   • KtorClientProvider.kt (timeout config)
echo   • RoleplayApi.kt (retry logic)
echo.
echo ═══════════════════════════════════════════════════════════
echo.
echo 🏗️  HOW TO REBUILD IN ANDROID STUDIO:
echo.
echo 1. Open Android Studio
echo 2. Click "Build" menu
echo 3. Select "Build Bundle(s) / APK(s)"
echo 4. Click "Build APK(s)"
echo 5. Wait for build to complete (2-3 minutes)
echo 6. APK will be at: app\build\outputs\apk\debug\app-debug.apk
echo.
echo ═══════════════════════════════════════════════════════════
echo.
echo 📱 OR FASTER: Run directly on your device:
echo.
echo 1. In Android Studio, click the ▶️ Run button (Shift+F10)
echo 2. Select your device: R5CRC2TLYTN
echo 3. App will install automatically
echo.
echo ═══════════════════════════════════════════════════════════
echo.
echo ✅ WHAT THIS FIXES:
echo.
echo BEFORE:
echo   • First request after app start → timeout error
echo   • Second request → works fine
echo   • Exit and restart → same problem
echo.
echo AFTER:
echo   • First request → auto-retries if timeout
echo   • Shows "server may be waking up..." message
echo   • User sees loading, then success
echo   • No more manual retry needed
echo.
echo ═══════════════════════════════════════════════════════════
echo.
echo 🎯 TESTING AFTER REBUILD:
echo.
echo 1. Close the app completely
echo 2. Wait 5 minutes (let Fly.io go to sleep)
echo 3. Open app and go to Roleplay
echo 4. Start a session
echo 5. Should work on first try (may take 30-60 seconds)
echo.
echo Check Logcat for: "⏳ Retry attempt" if server was sleeping
echo.
echo ═══════════════════════════════════════════════════════════
echo.
pause

