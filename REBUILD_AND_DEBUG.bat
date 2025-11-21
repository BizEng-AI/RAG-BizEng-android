@echo off
echo ╔═══════════════════════════════════════════════════════════╗
echo ║     REBUILD APP WITH ENHANCED DEBUG LOGGING               ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo This will:
echo   1. Clean previous build
echo   2. Build debug APK with enhanced logging
echo   3. Install on connected device
echo   4. Open logcat filtered for auth debugging
echo.
echo ⏳ Starting build process...
echo.

cd /d "%~dp0"

echo [1/4] Cleaning previous build...
call gradlew.bat clean

echo.
echo [2/4] Building debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Build failed! Check errors above.
    pause
    exit /b 1
)

echo.
echo [3/4] Installing on device...
call gradlew.bat installDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Install failed! Make sure device is connected.
    echo Run: adb devices
    pause
    exit /b 1
)

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║              ✅ BUILD & INSTALL COMPLETE!                 ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo [4/4] Opening logcat with auth filter...
echo.
echo ════════════════════════════════════════════════════════════
echo IMPORTANT: Now in the app:
echo   1. Open the app on your device
echo   2. Click "Register"
echo   3. Fill out the form
echo   4. Press "Register" button
echo   5. Watch for the lines below:
echo.
echo Look for:
echo   📥 RAW SERVER RESPONSE: [This shows what server returns]
echo   📥 HTTP Status: [This shows if request succeeded]
echo.
echo Press Ctrl+C to stop logcat when done.
echo ════════════════════════════════════════════════════════════
echo.
echo Starting logcat in 3 seconds...
timeout /t 3 /nobreak > nul

adb logcat -s "🔐 AuthApi:D" "NETWORK_CONFIG:D" "*:E"

