@echo off
REM ============================================================
REM BUILD APK - October 25, 2025
REM ============================================================
REM
REM Changes in this build:
REM - Enhanced logging for debugging 500 errors
REM - "Ground in book" default: ON (RAG mode)
REM - Smart error messages for Azure content filter
REM - Full HTTP request/response logging
REM
REM ============================================================

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║     BUILDING APK WITH ENHANCED LOGGING                 ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo [1/3] Cleaning previous build...
cd /d "%~dp0"
call gradlew clean

echo.
echo [2/3] Building debug APK...
call gradlew assembleDebug

echo.
echo [3/3] Locating APK...
echo.
echo ════════════════════════════════════════════════════════
echo ✅ BUILD COMPLETE!
echo ════════════════════════════════════════════════════════
echo.
echo 📱 Your APK is located at:
echo    app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📊 APK size:
dir app\build\outputs\apk\debug\app-debug.apk | find "app-debug.apk"
echo.
echo ════════════════════════════════════════════════════════
echo 🚀 NEXT STEPS:
echo ════════════════════════════════════════════════════════
echo.
echo Option 1: Install on connected device
echo    adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.
echo Option 2: Share APK file
echo    Send app\build\outputs\apk\debug\app-debug.apk to others
echo.
echo Option 3: View logs after install
echo    adb logcat -v time ^| findstr /i "CHAT HTTP_CLIENT CHAT_API"
echo.
echo ════════════════════════════════════════════════════════
echo 📝 WHAT'S NEW IN THIS BUILD:
echo ════════════════════════════════════════════════════════
echo.
echo ✅ Enhanced Pronunciation Assessment:
echo    - 🎯 IPA (phonetic) transcription for each word
echo    - 🔊 Phoneme-level analysis with color-coded scores
echo    - 💡 Detailed feedback tips section
echo    - 📊 6-level scoring (Outstanding to Keep Practicing)
echo    - 📝 Word-by-word breakdown with error types
echo.
echo ✅ Enhanced logging shows:
echo    - Exact messages sent to server (role + content)
echo    - Full HTTP request/response bodies
echo    - Detailed error messages with context
echo.
echo ✅ "Ground in book" mode is ON by default
echo    - Uses RAG (/ask endpoint) for grounded answers
echo    - Can toggle OFF for free chat mode
echo.
echo ✅ Better error handling for Azure content filter
echo    - Clear message when Azure blocks content
echo    - Suggestion to toggle mode or rephrase
echo.
echo ════════════════════════════════════════════════════════
echo 🔧 TO FIX "GROUND IN BOOK" 500 ERRORS:
echo ════════════════════════════════════════════════════════
echo.
echo Server-side fix needed (choose one):
echo.
echo   1. EASIEST: Adjust Azure Portal content filters
echo      - Go to portal.azure.com
echo      - Azure OpenAI resource → Content filters
echo      - Set to Medium instead of High
echo      - Allows casual language like "yo", "hey", etc.
echo.
echo   2. CODE FIX: Use FIX_AZURE_CONTENT_FILTER.py
echo      - Sanitizes slang before sending to Azure
echo      - Has fallback logic for blocked content
echo      - See file for implementation details
echo.
echo ════════════════════════════════════════════════════════
echo.
pause

