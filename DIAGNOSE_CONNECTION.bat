@echo off
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║       ANDROID APP CONNECTION DIAGNOSTIC TOOL              ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

REM Check if Python server is running locally
echo [1/5] Checking if Python server is running locally...
echo.
curl -s http://localhost:8020/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Python server is running on localhost:8020
    echo.
    curl http://localhost:8020/health
) else (
    echo ✗ Python server is NOT running on localhost
    echo.
    echo   To fix: cd C:\Users\sanja\rag-biz-english && python app.py
)
echo.
echo ───────────────────────────────────────────────────────────
echo.

REM Check ngrok connection
echo [2/5] Checking ngrok URL from NetworkModule.kt...
echo.
set NGROK_URL=https://colette-unvoluble-nonsynoptically.ngrok-free.dev
echo Configured ngrok URL: %NGROK_URL%
echo.
curl -s --max-time 5 "%NGROK_URL%/health" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ ngrok URL is accessible
    echo.
    curl "%NGROK_URL%/health"
) else (
    echo ✗ ngrok URL is NOT accessible
    echo.
    echo   Possible reasons:
    echo   1. ngrok tunnel expired (restart with: ngrok http 8020)
    echo   2. Server not running
    echo   3. Network/firewall blocking connection
)
echo.
echo ───────────────────────────────────────────────────────────
echo.

REM Check ADB connection
echo [3/5] Checking ADB device connection...
echo.
adb devices 2>nul | findstr /r "device$" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Android device connected via ADB
    echo.
    adb devices
) else (
    echo ✗ No Android device connected via ADB
    echo.
    echo   To fix: Connect device and enable USB debugging
)
echo.
echo ───────────────────────────────────────────────────────────
echo.

REM Get computer's WiFi IP
echo [4/5] Getting your computer's WiFi IP address...
echo.
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set IP=%%a
    set IP=!IP:~1!
    echo Your WiFi IP: !IP!
)
echo.
echo   Use this IP in NetworkModule.kt for local network mode
echo.
echo ───────────────────────────────────────────────────────────
echo.

REM Check firewall rule
echo [5/5] Checking Windows Firewall for port 8020...
echo.
netsh advfirewall firewall show rule name="Python Server 8020" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Firewall rule exists for port 8020
) else (
    echo ✗ Firewall rule NOT found for port 8020
    echo.
    echo   To fix: Run this command as Administrator:
    echo   netsh advfirewall firewall add rule name="Python Server 8020" dir=in action=allow protocol=TCP localport=8020
)
echo.
echo ───────────────────────────────────────────────────────────
echo.

REM Summary
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║                      RECOMMENDATIONS                       ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Based on the results above:
echo.
echo 1. If Python server is NOT running:
echo    - Open new terminal: cd C:\Users\sanja\rag-biz-english
echo    - Run: python app.py
echo.
echo 2. If ngrok URL is NOT accessible:
echo    - Start new ngrok tunnel: ngrok http 8020
echo    - Copy new URL and update NetworkModule.kt
echo    - Rebuild APK
echo.
echo 3. For local network (recommended):
echo    - Use your WiFi IP shown above
echo    - Update NetworkModule.kt:
echo      PRODUCTION_SERVER_IP = "YOUR_IP"
echo      SERVER_PORT = "8020"
echo      USE_HTTPS = false
echo    - Rebuild APK: gradlew.bat assembleDebug
echo.
echo 4. Check app logs for actual connection attempts:
echo    - adb logcat ^| findstr "NETWORK"
echo.
echo ═══════════════════════════════════════════════════════════
echo.
pause

