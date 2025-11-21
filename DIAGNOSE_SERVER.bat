@echo off
REM ============================================================================
REM SERVER DIAGNOSTICS - Test Server Connectivity and Response Format
REM ============================================================================
REM
REM This script runs diagnostic tests to identify server issues
REM
REM ============================================================================

echo.
echo ============================================================================
echo               SERVER DIAGNOSTICS - TESTING ENDPOINTS
echo ============================================================================
echo.

echo Target Server: https://bizeng-server.fly.dev
echo.

echo [1/2] Testing with PowerShell...
echo.

powershell -Command ^
    "$ErrorActionPreference = 'Continue'; " ^
    "Write-Host ''; " ^
    "Write-Host '=== Testing Health Endpoint ===' -ForegroundColor Cyan; " ^
    "try { " ^
        "$health = Invoke-RestMethod -Uri 'https://bizeng-server.fly.dev/health' -Method Get -TimeoutSec 10; " ^
        "Write-Host '✅ Health endpoint responded' -ForegroundColor Green; " ^
        "Write-Host \"Response: $($health | ConvertTo-Json)\" -ForegroundColor Gray; " ^
    "} catch { " ^
        "Write-Host '❌ Health endpoint failed' -ForegroundColor Red; " ^
        "Write-Host \"Error: $($_.Exception.Message)\" -ForegroundColor Red; " ^
    "} " ^
    "Write-Host ''; " ^
    "Write-Host '=== Testing Register Endpoint ===' -ForegroundColor Cyan; " ^
    "try { " ^
        "$testEmail = \"diagnostic_$(Get-Random)@test.com\"; " ^
        "$body = @{ email = $testEmail; password = 'Test123!'; display_name = 'Diagnostic Test' } | ConvertTo-Json; " ^
        "Write-Host \"Test email: $testEmail\" -ForegroundColor Gray; " ^
        "$response = Invoke-WebRequest -Uri 'https://bizeng-server.fly.dev/auth/register' -Method Post -Body $body -ContentType 'application/json' -TimeoutSec 10; " ^
        "Write-Host \"✅ Status: $($response.StatusCode)\" -ForegroundColor Green; " ^
        "Write-Host '📥 Raw Response:' -ForegroundColor Yellow; " ^
        "Write-Host $response.Content -ForegroundColor Gray; " ^
        "Write-Host ''; " ^
        "$json = $response.Content | ConvertFrom-Json; " ^
        "Write-Host '📊 Analysis:' -ForegroundColor Yellow; " ^
        "Write-Host \"  access_token present: $($null -ne $json.access_token)\" -ForegroundColor Gray; " ^
        "Write-Host \"  refresh_token present: $($null -ne $json.refresh_token)\" -ForegroundColor Gray; " ^
        "if ($null -eq $json.access_token -or $null -eq $json.refresh_token) { " ^
            "Write-Host ''; " ^
            "Write-Host '❌ SERVER ISSUE DETECTED!' -ForegroundColor Red; " ^
            "Write-Host 'The server is NOT returning required token fields.' -ForegroundColor Red; " ^
            "Write-Host 'Expected format:' -ForegroundColor Yellow; " ^
            "Write-Host '{' -ForegroundColor Gray; " ^
            "Write-Host '  \"access_token\": \"<jwt_token>\",' -ForegroundColor Gray; " ^
            "Write-Host '  \"refresh_token\": \"<jwt_token>\",' -ForegroundColor Gray; " ^
            "Write-Host '  \"token_type\": \"bearer\"' -ForegroundColor Gray; " ^
            "Write-Host '}' -ForegroundColor Gray; " ^
        "} else { " ^
            "Write-Host ''; " ^
            "Write-Host '✅ Server response is CORRECT!' -ForegroundColor Green; " ^
        "} " ^
    "} catch { " ^
        "Write-Host '❌ Register endpoint failed' -ForegroundColor Red; " ^
        "Write-Host \"Error: $($_.Exception.Message)\" -ForegroundColor Red; " ^
        "if ($_.ErrorDetails.Message) { " ^
            "Write-Host \"Details: $($_.ErrorDetails.Message)\" -ForegroundColor Red; " ^
        "} " ^
    "} " ^
    "Write-Host '';"

echo.
echo [2/2] Running Gradle diagnostic test...
echo.
call gradlew test --tests "com.example.myapplication.integration.ServerConnectivityTest.diagnose what server actually returns" --console=plain

echo.
echo ============================================================================
echo                      DIAGNOSTICS COMPLETE
echo ============================================================================
echo.
echo Next Steps:
echo.
echo If the server is NOT returning tokens:
echo   1. This is a SERVER-SIDE issue
echo   2. The /auth/register endpoint needs to be fixed
echo   3. Check the server code and ensure it returns:
echo      { "access_token": "...", "refresh_token": "...", "token_type": "bearer" }
echo.
echo If the server IS returning tokens but app still fails:
echo   1. Check logcat: adb logcat -s "🔐 AuthApi"
echo   2. The detailed logs will show the exact parsing error
echo   3. Report the issue with the logs
echo.

pause

