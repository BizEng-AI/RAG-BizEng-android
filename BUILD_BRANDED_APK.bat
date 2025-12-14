@echo off
echo.
echo ╔═══════════════════════════════════════════════════════╗
echo ║     Building BizEng Chatbot with Custom Branding     ║
echo ╚═══════════════════════════════════════════════════════╝
echo.
echo App Name: BizEng Chatbot
echo Custom Icon: ✓ (from icon.png)
echo Splash Screen: ✓ (with your logo)
echo.
echo Building APK...
echo.

cd /d "%~dp0"
call gradlew assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════╗
    echo ║              BUILD SUCCESSFUL! ✓                      ║
    echo ╚═══════════════════════════════════════════════════════╝
    echo.
    echo 📱 APK Location:
    echo    app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 🚀 Your branded app is ready:
    echo    ✓ App Name: BizEng Chatbot
    echo    ✓ Custom Icon (your logo)
    echo    ✓ Custom Splash Screen
    echo.
    echo 📥 To install on device:
    echo    adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ╔═══════════════════════════════════════════════════════╗
    echo ║              BUILD FAILED! ✗                          ║
    echo ╚═══════════════════════════════════════════════════════╝
    echo.
    echo Check the error messages above.
    echo.
)

pause

