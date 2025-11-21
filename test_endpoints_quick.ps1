Write-Host "`n=== TESTING PREVIOUSLY FAILED ENDPOINTS ===" -ForegroundColor Cyan

# Login
Write-Host "`nLogging in..." -ForegroundColor Yellow
$body = @{
    email = "yoo@gmail.com"
    password = "qwerty"
} | ConvertTo-Json

try {
    $loginResp = Invoke-RestMethod -Uri "https://bizeng-server.fly.dev/auth/login" -Method Post -Body $body -ContentType "application/json"
    $token = $loginResp.access_token
    Write-Host "✓ Login successful" -ForegroundColor Green
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test users_activity
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 1: /admin/monitor/users_activity" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

try {
    $users = Invoke-RestMethod -Uri "https://bizeng-server.fly.dev/admin/monitor/users_activity?days=30" -Headers @{Authorization="Bearer $token"}

    Write-Host "✅ SUCCESS - Endpoint is working!" -ForegroundColor Green
    Write-Host "Total student records: $($users.Count)" -ForegroundColor Cyan

    if ($users.Count -gt 0) {
        Write-Host "`nFirst record preview:" -ForegroundColor White
        $users[0] | Format-List user_id, email, display_name, group_name, total_exercises, pronunciation_count, chat_count, roleplay_count
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ FAILED" -ForegroundColor Red
    Write-Host "Status Code: $statusCode" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
}

# Test groups_activity
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 2: /admin/monitor/groups_activity" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

try {
    $groups = Invoke-RestMethod -Uri "https://bizeng-server.fly.dev/admin/monitor/groups_activity?days=30" -Headers @{Authorization="Bearer $token"}

    Write-Host "✅ SUCCESS - Endpoint is working!" -ForegroundColor Green
    Write-Host "Total group records: $($groups.Count)" -ForegroundColor Cyan

    if ($groups.Count -gt 0) {
        Write-Host "`nGroups preview:" -ForegroundColor White
        $groups | Format-Table group_name, student_count, total_exercises, pronunciation_count, chat_count, roleplay_count -AutoSize
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ FAILED" -ForegroundColor Red
    Write-Host "Status Code: $statusCode" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST COMPLETE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

