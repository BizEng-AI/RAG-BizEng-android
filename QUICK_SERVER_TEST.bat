@echo off
REM ============================================================================
REM QUICK SERVER TEST - Identify Token Issue Immediately
REM ============================================================================

echo.
echo ============================================================================
echo          TESTING SERVER - Does it return tokens?
echo ============================================================================
echo.
echo Testing: https://bizeng-server.fly.dev/auth/register
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference = 'SilentlyContinue'; " ^
    "$testEmail = \"quicktest_$(Get-Random)@test.com\"; " ^
    "$body = @{ email = $testEmail; password = 'Test123!'; display_name = 'Quick Test' } | ConvertTo-Json; " ^
    "Write-Host 'Sending registration request...' -ForegroundColor Cyan; " ^
    "Write-Host ''; " ^
    "try { " ^
        "$response = Invoke-WebRequest -Uri 'https://bizeng-server.fly.dev/auth/register' -Method Post -Body $body -ContentType 'application/json' -TimeoutSec 10 -UseBasicParsing; " ^
        "Write-Host '✅ Server responded!' -ForegroundColor Green; " ^
        "Write-Host \"Status: $($response.StatusCode)\" -ForegroundColor Gray; " ^
        "Write-Host ''; " ^
        "Write-Host 'Raw Response:' -ForegroundColor Yellow; " ^
        "Write-Host $response.Content -ForegroundColor Gray; " ^
        "Write-Host ''; " ^
        "Write-Host '═══════════════════════════════════════════════════════' -ForegroundColor Cyan; " ^
        "Write-Host 'DIAGNOSIS:' -ForegroundColor Yellow; " ^
        "Write-Host '═══════════════════════════════════════════════════════' -ForegroundColor Cyan; " ^
        "if ($response.Content -match '\"access_token\"') { " ^
            "Write-Host '✅ Response contains access_token field' -ForegroundColor Green; " ^
        "} else { " ^
            "Write-Host '❌ Response does NOT contain access_token field' -ForegroundColor Red; " ^
        "} " ^
        "if ($response.Content -match '\"refresh_token\"') { " ^
            "Write-Host '✅ Response contains refresh_token field' -ForegroundColor Green; " ^
        "} else { " ^
            "Write-Host '❌ Response does NOT contain refresh_token field' -ForegroundColor Red; " ^
        "} " ^
        "Write-Host ''; " ^
        "if ($response.Content -match '\"access_token\"' -and $response.Content -match '\"refresh_token\"') { " ^
            "Write-Host '✅✅✅ SERVER IS WORKING CORRECTLY! ✅✅✅' -ForegroundColor Green; " ^
            "Write-Host ''; " ^
            "Write-Host 'The app should work now. You can build the APK!' -ForegroundColor Green; " ^
        "} else { " ^
            "Write-Host '❌❌❌ SERVER ISSUE DETECTED! ❌❌❌' -ForegroundColor Red; " ^
            "Write-Host ''; " ^
            "Write-Host 'The server is NOT returning required token fields.' -ForegroundColor Red; " ^
            "Write-Host 'This is why the app registration fails.' -ForegroundColor Red; " ^
            "Write-Host ''; " ^
            "Write-Host '🔧 FIX REQUIRED:' -ForegroundColor Yellow; " ^
            "Write-Host 'The /auth/register endpoint must return:' -ForegroundColor Yellow; " ^
            "Write-Host '{' -ForegroundColor Gray; " ^
            "Write-Host '  \"access_token\": \"<jwt_token>\",' -ForegroundColor Gray; " ^
            "Write-Host '  \"refresh_token\": \"<jwt_token>\",' -ForegroundColor Gray; " ^
            "Write-Host '  \"token_type\": \"bearer\"' -ForegroundColor Gray; " ^
            "Write-Host '}' -ForegroundColor Gray; " ^
        "} " ^
    "} catch { " ^
        "Write-Host '❌ Request failed!' -ForegroundColor Red; " ^
        "Write-Host \"Error: $($_.Exception.Message)\" -ForegroundColor Red; " ^
        "Write-Host ''; " ^
        "Write-Host 'Possible reasons:' -ForegroundColor Yellow; " ^
        "Write-Host '  - Server is down' -ForegroundColor Gray; " ^
        "Write-Host '  - No internet connection' -ForegroundColor Gray; " ^
        "Write-Host '  - Firewall blocking request' -ForegroundColor Gray; " ^
    "} " ^
    "Write-Host ''; " ^
    "Write-Host '═══════════════════════════════════════════════════════' -ForegroundColor Cyan; "

echo.
echo For detailed diagnostics, run: DIAGNOSE_SERVER.bat
echo For full documentation, see: SERVER_TOKEN_ISSUE_FIX.md
echo.
pause

