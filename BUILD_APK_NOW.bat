@echo off
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║              REBUILDING APK WITH WIFI CONFIG              ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo ✓ Server is running at: http://192.168.1.60:8020
echo ✓ Firewall rule is active
echo ✓ NetworkModule.kt updated
echo.
echo Now building APK...
echo.

cd /d "%~dp0"

REM Try to use gradlew
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════════╗
    echo ║                  BUILD SUCCESSFUL! ✓                      ║
    echo ╚═══════════════════════════════════════════════════════════╝
    echo.
    echo Your APK is ready at:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Next steps:
    echo 1. Transfer app-debug.apk to your Android device
    echo 2. Install it (may need to uninstall old version first)
    echo 3. Make sure device is on same WiFi as this computer
    echo 4. Open the app and test!
    echo.
    echo To check logs while testing:
    echo   adb logcat ^| findstr "NETWORK"
    echo.
) else (
    echo.
    echo ╔═══════════════════════════════════════════════════════════╗
    echo ║                    BUILD FAILED ✗                         ║
    echo ╚═══════════════════════════════════════════════════════════╝
    echo.
    echo Common issues:
    echo.
    echo 1. JAVA_HOME not set:
    echo    - Make sure Android Studio is installed
    echo    - Or install JDK 17 and set JAVA_HOME
    echo.
    echo 2. Build from Android Studio instead:
    echo    - Open this project in Android Studio
    echo    - Build ^> Build Bundle^(s^) / APK^(s^) ^> Build APK^(s^)
    echo    - APK will be at: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 3. Use existing APK if available:
    echo    - Check if app-debug.apk already exists
    echo    - If NetworkModule.kt was changed, you MUST rebuild
    echo.
)

echo.
pause

