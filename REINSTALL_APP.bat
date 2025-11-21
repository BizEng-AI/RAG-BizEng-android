@echo off
echo ============================================
echo REINSTALLING UPDATED APK
echo ============================================
echo.

set ADB=C:\Users\sanja\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=C:\Users\sanja\rag-biz-english\android\app\build\outputs\apk\debug\app-debug.apk

echo [1/3] Checking device connection...
%ADB% devices
echo.

echo [2/3] Uninstalling old app...
%ADB% uninstall com.example.myapplication
echo.

echo [3/3] Installing new APK with roleplay fixes...
%ADB% install -r "%APK%"
echo.

if %ERRORLEVEL% EQU 0 (
    echo ============================================
    echo ✅ SUCCESS! Updated app installed
    echo ============================================
    echo.
    echo Now:
    echo 1. Open the app on your device
    echo 2. It will auto-test the endpoints
    echo 3. Navigate to Roleplay tab
    echo 4. Try "Job Interview" scenario
    echo.
) else (
    echo ============================================
    echo ❌ FAILED! Check if device is connected
    echo ============================================
    echo.
    echo Run: adb devices
    echo.
)

pause

