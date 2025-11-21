@echo off
setlocal enabledelayedexpansion
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║          CONNECTION PROBLEM - QUICK FIX                    ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Your app is trying to connect to:
echo   https://colette-unvoluble-nonsynoptically.ngrok-free.dev
echo.
echo This ngrok URL likely expired. Here's what to do:
echo.
echo ═══════════════════════════════════════════════════════════
echo SOLUTION 1: USE LOCAL WIFI (RECOMMENDED)
echo ═══════════════════════════════════════════════════════════
echo.
echo Step 1: Get your computer's WiFi IP
echo ────────────────────────────────────
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set IP=%%a
    set IP=!IP:~1!
    echo Your WiFi IP: !IP!
)
echo.
echo Step 2: Make sure Python server is running
echo ────────────────────────────────────────────
echo Open a new terminal and run:
echo   cd C:\Users\sanja\rag-biz-english
echo   python app.py
echo.
echo Step 3: Add firewall rule (run as Administrator)
echo ──────────────────────────────────────────────────
echo   netsh advfirewall firewall add rule name="Python Server 8020" dir=in action=allow protocol=TCP localport=8020
echo.
echo Step 4: Update the app configuration
echo ──────────────────────────────────────
echo   File: android\app\src\main\java\com\example\myapplication\di\NetworkModule.kt
echo   Line 59: Change to: val PRODUCTION_SERVER_IP = "!IP!"
echo   Line 60: Change to: val SERVER_PORT = "8020"
echo   Line 61: Change to: val USE_HTTPS = false
echo.
echo Step 5: Rebuild the APK
echo ────────────────────────
echo   cd C:\Users\sanja\rag-biz-english\android
echo   gradlew.bat assembleDebug
echo.
echo.
echo ═══════════════════════════════════════════════════════════
echo SOLUTION 2: USE NEW NGROK URL
echo ═══════════════════════════════════════════════════════════
echo.
echo Step 1: Start Python server
echo ────────────────────────────
echo   cd C:\Users\sanja\rag-biz-english
echo   python app.py
echo.
echo Step 2: Start ngrok (in another terminal)
echo ───────────────────────────────────────────
echo   ngrok http 8020
echo.
echo Step 3: Copy the new URL from ngrok
echo ─────────────────────────────────────
echo   It looks like: abc-xyz-123.ngrok-free.app
echo   (WITHOUT the https:// part)
echo.
echo Step 4: Update the app configuration
echo ──────────────────────────────────────
echo   File: android\app\src\main\java\com\example\myapplication\di\NetworkModule.kt
echo   Line 59: Change to: val PRODUCTION_SERVER_IP = "YOUR_NEW_NGROK_URL"
echo   Line 60: Keep as: val SERVER_PORT = ""
echo   Line 61: Keep as: val USE_HTTPS = true
echo.
echo Step 5: Rebuild the APK
echo ────────────────────────
echo   cd C:\Users\sanja\rag-biz-english\android
echo   gradlew.bat assembleDebug
echo.
echo.
echo ═══════════════════════════════════════════════════════════
echo WHAT HAPPENED?
echo ═══════════════════════════════════════════════════════════
echo.
echo The ngrok URL in your app expired (ngrok free tunnels expire).
echo.
echo You need to either:
echo   A) Use local WiFi IP (more stable, no expiration)
echo   B) Get a new ngrok URL and rebuild the app
echo.
echo After updating NetworkModule.kt, you MUST rebuild the APK
echo for the changes to take effect!
echo.
echo ═══════════════════════════════════════════════════════════
echo.
pause

