@echo off
echo ========================================
echo Testing Server Connection
echo ========================================
echo.

REM The server URL from NetworkModule.kt
set SERVER_URL=https://colette-unvoluble-nonsynoptically.ngrok-free.dev

echo Testing connection to: %SERVER_URL%
echo.

echo Test 1: Health Check
echo --------------------
curl -i "%SERVER_URL%/health" 2^>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ✗ FAILED: Cannot connect to server
    echo.
    echo Common issues:
    echo 1. ngrok tunnel expired (free ngrok tunnels expire after some time)
    echo 2. Python server is not running
    echo 3. Python server is not using the correct ngrok URL
    echo.
    echo Solutions:
    echo 1. Check if Python server is running
    echo 2. Get new ngrok URL: ngrok http 8020
    echo 3. Update NetworkModule.kt with new URL
    echo 4. Rebuild APK
    goto :end
) else (
    echo.
    echo ✓ Health check passed
)

echo.
echo Test 2: Version Check
echo --------------------
curl -i "%SERVER_URL%/version" 2^>nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ FAILED: Version endpoint not accessible
) else (
    echo ✓ Version check passed
)

echo.
echo Test 3: Ask Endpoint (POST)
echo --------------------
curl -X POST "%SERVER_URL%/ask" ^
  -H "Content-Type: application/json" ^
  -d "{\"query\":\"test\",\"k\":3}" 2^>nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ FAILED: Ask endpoint not accessible
) else (
    echo ✓ Ask endpoint passed
)

echo.
echo ========================================
echo All tests completed!
echo ========================================

:end
echo.
pause

