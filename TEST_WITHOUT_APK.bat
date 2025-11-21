@echo off
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║           QUICK TEST - NO APK BUILD NEEDED               ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo ✓ Server is running at: http://192.168.1.60:8020
echo ✓ Firewall rule is active
echo ✓ NetworkModule.kt updated to use WiFi IP
echo.
echo ═══════════════════════════════════════════════════════════
echo OPTION 1: Test with Android Studio (FASTEST)
echo ═══════════════════════════════════════════════════════════
echo.
echo 1. Open this project in Android Studio
echo 2. Make sure your device is connected (USB or WiFi debugging)
echo 3. Click the green "Run" button (▶️) at the top
echo 4. Android Studio will:
echo    - Compile with new WiFi settings
echo    - Install directly on your device
echo    - Launch the app
echo.
echo This takes ~1-2 minutes instead of full APK build!
echo.
echo.
echo ═══════════════════════════════════════════════════════════
echo OPTION 2: Test Current APK (if it has WiFi IP)
echo ═══════════════════════════════════════════════════════════
echo.
echo If your current installed APK was already built with:
echo   - PRODUCTION_SERVER_IP = "192.168.1.60"
echo   - SERVER_PORT = "8020"
echo.
echo Then just:
echo 1. Make sure both devices on same WiFi
echo 2. Open the app
echo 3. Try Chat or Roleplay
echo.
echo Check what IP your current APK uses by opening app and checking logs
echo.
echo.
echo ═══════════════════════════════════════════════════════════
echo TESTING STEPS
echo ═══════════════════════════════════════════════════════════
echo.
echo While app is running:
echo.
echo 1. Check connection logs:
echo    The app tests connection on startup
echo    Look at MainActivity.kt onCreate - it logs connection test
echo.
echo 2. Try Chat feature:
echo    - Type a message like "Hello"
echo    - Should get response from server
echo.
echo 3. Try Roleplay feature:
echo    - Select a scenario
echo    - Should start the roleplay session
echo.
echo 4. Watch for errors:
echo    - "Connection refused" = firewall or server issue
echo    - "Timeout" = wrong IP or network issue
echo    - "404" = endpoint not found on server
echo.
echo.
echo ═══════════════════════════════════════════════════════════
echo CURRENT SERVER STATUS
echo ═══════════════════════════════════════════════════════════
echo.
echo Testing server right now...
echo.

curl http://192.168.1.60:8020/health 2^>nul
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Server is responding!
    echo.
) else (
    echo.
    echo ✗ Server not responding
    echo   Make sure Python server is running
    echo.
)

curl http://192.168.1.60:8020/version 2^>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Version endpoint working
) else (
    echo ✗ Version endpoint not found
)

echo.
echo ═══════════════════════════════════════════════════════════
echo QUICK DIAGNOSIS
echo ═══════════════════════════════════════════════════════════
echo.
echo Your NetworkModule.kt currently points to:
echo   http://192.168.1.60:8020
echo.
echo To test if your INSTALLED app uses this:
echo   1. Open the app
echo   2. Look at the first screen that appears
echo   3. If it connects: ✓ You're using new WiFi config
echo   4. If it fails: Your APK still has old ngrok URL
echo.
echo If APK has old URL, you need to either:
echo   - Run from Android Studio (Option 1 above)
echo   - Or build new APK with BUILD_APK_NOW.bat
echo.
echo ═══════════════════════════════════════════════════════════
echo.
echo RECOMMENDED: Use Android Studio to run the app
echo This is the fastest way to test your changes!
echo.
pause

