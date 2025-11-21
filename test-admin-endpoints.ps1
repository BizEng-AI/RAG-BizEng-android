# Admin Endpoints Verification Script
# Tests all admin analytics endpoints
# Usage: .\test-admin-endpoints.ps1

$ErrorActionPreference = "Stop"
$baseUrl = "https://bizeng-server.fly.dev"
$adminEmail = "yoo@gmail.com"
$adminPassword = "qwerty"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ADMIN ENDPOINTS VERIFICATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Login
Write-Host "STEP 1: Logging in..." -ForegroundColor Yellow

$loginBody = @{
    email = $adminEmail
    password = $adminPassword
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.access_token

    if ([string]::IsNullOrWhiteSpace($token)) {
        throw "No access token received"
    }

    Write-Host "   SUCCESS: Login OK" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{ Authorization = "Bearer $token" }

# Test Overview
Write-Host "STEP 2: Testing /admin/monitor/overview..." -ForegroundColor Yellow
try {
    $overview = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/overview" -Headers $headers
    Write-Host "   SUCCESS: Overview working" -ForegroundColor Green
    Write-Host "   Activity Events: $($overview.activity_events.Count)" -ForegroundColor Gray
    Write-Host "   Exercise Attempts: $($overview.exercise_attempts.Count)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test Attempts Daily
Write-Host "STEP 3: Testing /admin/monitor/attempts_daily..." -ForegroundColor Yellow
try {
    $attempts = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/attempts_daily" -Headers $headers
    Write-Host "   SUCCESS: Attempts daily working" -ForegroundColor Green
    Write-Host "   Data points: $($attempts.Count)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test Users Signups Daily
Write-Host "STEP 4: Testing /admin/monitor/users_signups_daily..." -ForegroundColor Yellow
try {
    $signups = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/users_signups_daily" -Headers $headers
    Write-Host "   SUCCESS: Users signups daily working" -ForegroundColor Green
    Write-Host "   Data points: $($signups.Count)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test Active Today
Write-Host "STEP 5: Testing /admin/monitor/active_today..." -ForegroundColor Yellow
try {
    $activeToday = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/active_today" -Headers $headers
    Write-Host "   SUCCESS: Active today working" -ForegroundColor Green
    Write-Host "   Active Students: $($activeToday.active_students)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test Recent Attempts
Write-Host "STEP 6: Testing /admin/monitor/recent_attempts..." -ForegroundColor Yellow
try {
    $recentAttempts = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/recent_attempts?limit=5" -Headers $headers
    Write-Host "   SUCCESS: Recent attempts working" -ForegroundColor Green
    Write-Host "   Attempts returned: $($recentAttempts.Count)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test Users Activity
Write-Host "STEP 7: Testing /admin/monitor/users_activity..." -ForegroundColor Yellow
try {
    $usersActivity = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/users_activity?days=30" -Headers $headers
    Write-Host "   SUCCESS: Users activity working" -ForegroundColor Green
    Write-Host "   Total users returned: $($usersActivity.Count)" -ForegroundColor Gray

    $withAttempts = ($usersActivity | Where-Object { $_.total_exercises -gt 0 }).Count
    $withZero = ($usersActivity | Where-Object { $_.total_exercises -eq 0 }).Count

    Write-Host "   Users with exercises: $withAttempts" -ForegroundColor Gray
    Write-Host "   Users with ZERO exercises: $withZero" -ForegroundColor Gray

    if ($withZero -gt 0) {
        Write-Host "   SUCCESS: Users with zero attempts included!" -ForegroundColor Green
    }
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test Groups Activity
Write-Host "STEP 8: Testing /admin/monitor/groups_activity..." -ForegroundColor Yellow
try {
    $groupsActivity = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/groups_activity?days=30" -Headers $headers
    Write-Host "   SUCCESS: Groups activity working" -ForegroundColor Green
    Write-Host "   Groups returned: $($groupsActivity.Count)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test User Activity Detail
Write-Host "STEP 9: Testing /admin/monitor/user_activity..." -ForegroundColor Yellow
try {
    if ($usersActivity -and $usersActivity.Count -gt 0) {
        $testUserId = $usersActivity[0].user_id
        $userDetail = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/user_activity/$testUserId?days=30" -Headers $headers
        Write-Host "   SUCCESS: User activity detail working" -ForegroundColor Green
        Write-Host "   Activity items: $($userDetail.items.Count)" -ForegroundColor Gray
    } else {
        Write-Host "   SKIPPED: No users available" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICATION COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "All tests completed. Check results above." -ForegroundColor Green
Write-Host ""

