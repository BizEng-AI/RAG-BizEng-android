@echo off
echo ====================================================================
echo 🚀 QUICK TEST - Token Issue Fix
echo ====================================================================
echo.
echo This will:
echo   1. Reinstall the updated APK
echo   2. Clear logs
echo   3. Start monitoring
echo.
echo Make sure your device is connected!
echo.
pause

echo.
echo 📦 Step 1: Reinstalling APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo ❌ Failed to install APK. Check if device is connected.
    pause
    exit /b 1
)
echo ✅ APK installed

echo.
echo 🧹 Step 2: Clearing old logs...
adb logcat -c
echo ✅ Logs cleared

echo.
echo 🎯 Step 3: Starting log monitor...
echo ====================================================================
echo.
echo 📱 NOW: Open the app and try to REGISTER
echo.
echo Look for:
echo   - "RAW SERVER RESPONSE:" ^<-- The actual server response
echo   - "Successfully parsed TokenResponse" ^<-- Parsing success
echo   - "access token is null" ^<-- The error (if it still happens)
echo.
echo Press Ctrl+C to stop monitoring
echo.
echo ====================================================================
echo.

adb logcat | findstr /C:"🔐 AuthApi" /C:"AuthRepository" /C:"RAW SERVER RESPONSE"

