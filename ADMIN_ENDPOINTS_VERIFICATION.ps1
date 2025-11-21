# ============================================================================
# ADMIN ENDPOINTS VERIFICATION SCRIPT
# ============================================================================
# This script tests all admin analytics endpoints to verify they're working
# correctly after the recent server fixes (exercise tracking + all users).
#
# Prerequisites:
# - Admin account credentials (default: yoo@gmail.com / qwerty)
# - Server deployed at: https://bizeng-server.fly.dev
#
# Usage:
#   .\ADMIN_ENDPOINTS_VERIFICATION.ps1
#
# Last Updated: November 17, 2025
# ============================================================================

$ErrorActionPreference = "Stop"
$baseUrl = "https://bizeng-server.fly.dev"
$adminEmail = "yoo@gmail.com"
$adminPassword = "qwerty"

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "ADMIN ENDPOINTS VERIFICATION" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================================
# STEP 1: Login and get admin token
# ============================================================================
Write-Host "STEP 1: Logging in as admin..." -ForegroundColor Yellow

$loginBody = @{
    email = $adminEmail
    password = $adminPassword
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json"

    $token = $loginResponse.access_token

    if ([string]::IsNullOrWhiteSpace($token)) {
        throw "Login succeeded but no access token received"
    }

    Write-Host "   ✅ Login successful" -ForegroundColor Green
    Write-Host "   Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   ❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    Authorization = "Bearer $token"
}

# ============================================================================
# STEP 2: Test Overview Endpoint
# ============================================================================
Write-Host "STEP 2: Testing /admin/monitor/overview..." -ForegroundColor Yellow

try {
    $overview = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/overview" -Headers $headers

    Write-Host "   ✅ Overview endpoint working" -ForegroundColor Green
    Write-Host "   Activity Events: $($overview.activity_events.Count)" -ForegroundColor Gray
    Write-Host "   Exercise Attempts: $($overview.exercise_attempts.Count)" -ForegroundColor Gray
    Write-Host "   User Signups: $($overview.user_signups.Count)" -ForegroundColor Gray
    Write-Host "   Roles: $($overview.roles.Count)" -ForegroundColor Gray

    if ($overview.totals) {
        Write-Host "   Total Users: $($overview.totals.total_users)" -ForegroundColor Gray
        Write-Host "   Total Attempts: $($overview.totals.total_attempts)" -ForegroundColor Gray
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Overview failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 3: Test Attempts Daily
# ============================================================================
Write-Host "STEP 3: Testing /admin/monitor/attempts_daily..." -ForegroundColor Yellow

try {
    $attempts = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/attempts_daily" -Headers $headers

    Write-Host "   ✅ Attempts daily endpoint working" -ForegroundColor Green
    Write-Host "   Data points: $($attempts.Count)" -ForegroundColor Gray

    if ($attempts.Count -gt 0) {
        $recent = $attempts | Select-Object -First 3
        foreach ($a in $recent) {
            Write-Host "      $($a.day): $($a.count) attempts" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Attempts daily failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 4: Test Users Signups Daily
# ============================================================================
Write-Host "STEP 4: Testing /admin/monitor/users_signups_daily..." -ForegroundColor Yellow

try {
    $signups = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/users_signups_daily" -Headers $headers

    Write-Host "   ✅ Users signups daily endpoint working" -ForegroundColor Green
    Write-Host "   Data points: $($signups.Count)" -ForegroundColor Gray

    if ($signups.Count -gt 0) {
        $recent = $signups | Select-Object -First 3
        foreach ($s in $recent) {
            Write-Host "      $($s.day): $($s.count) signups" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Users signups daily failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 5: Test Active Today
# ============================================================================
Write-Host "STEP 5: Testing /admin/monitor/active_today..." -ForegroundColor Yellow

try {
    $activeToday = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/active_today" -Headers $headers

    Write-Host "   ✅ Active today endpoint working" -ForegroundColor Green
    Write-Host "   Date: $($activeToday.date)" -ForegroundColor Gray
    Write-Host "   Active Students: $($activeToday.active_students)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   ❌ Active today failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 6: Test Recent Attempts
# ============================================================================
Write-Host "STEP 6: Testing /admin/monitor/recent_attempts..." -ForegroundColor Yellow

try {
    $recentAttempts = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/recent_attempts?limit=5" -Headers $headers

    Write-Host "   ✅ Recent attempts endpoint working" -ForegroundColor Green
    Write-Host "   Attempts returned: $($recentAttempts.Count)" -ForegroundColor Gray

    if ($recentAttempts.Count -gt 0) {
        foreach ($attempt in $recentAttempts) {
            Write-Host "      $($attempt.student_email): $($attempt.exercise_type)" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Recent attempts failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 7: Test Users Activity (NEW - should return ALL users)
# ============================================================================
Write-Host "STEP 7: Testing /admin/monitor/users_activity (ALL USERS)..." -ForegroundColor Yellow

try {
    $usersActivity = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/users_activity?days=30" -Headers $headers

    Write-Host "   ✅ Users activity endpoint working" -ForegroundColor Green
    Write-Host "   Total users returned: $($usersActivity.Count)" -ForegroundColor Gray

    $withAttempts = ($usersActivity | Where-Object { $_.total_exercises -gt 0 }).Count
    $withZero = ($usersActivity | Where-Object { $_.total_exercises -eq 0 }).Count

    Write-Host "   Users with exercises: $withAttempts" -ForegroundColor Gray
    Write-Host "   Users with ZERO exercises: $withZero" -ForegroundColor Gray

    if ($withZero -gt 0) {
        Write-Host "   ✅ SUCCESS! Users with zero attempts ARE included (LEFT JOIN working!)" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  WARNING: All users have exercises (or only 1 user registered)" -ForegroundColor Yellow
    }

    # Show sample data
    if ($usersActivity.Count -gt 0) {
        Write-Host "`n   Sample users:" -ForegroundColor Gray
        $sample = $usersActivity | Select-Object -First 3
        foreach ($user in $sample) {
            Write-Host "      $($user.email): $($user.total_exercises) exercises" -ForegroundColor Gray
            if ($user.group_name) {
                Write-Host "         Group: $($user.group_name)" -ForegroundColor DarkGray
            }
        }
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Users activity failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 8: Test Groups Activity
# ============================================================================
Write-Host "STEP 8: Testing /admin/monitor/groups_activity..." -ForegroundColor Yellow

try {
    $groupsActivity = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/groups_activity?days=30" -Headers $headers

    Write-Host "   ✅ Groups activity endpoint working" -ForegroundColor Green
    Write-Host "   Groups returned: $($groupsActivity.Count)" -ForegroundColor Gray

    foreach ($group in $groupsActivity) {
        $groupName = if ($group.group_name) { $group.group_name } else { "Unassigned" }
        Write-Host "      $groupName : $($group.student_count) students, $($group.total_exercises) exercises" -ForegroundColor Gray
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Groups activity failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# STEP 9: Test User Activity Detail (pick first user)
# ============================================================================
Write-Host "STEP 9: Testing /admin/monitor/user_activity/{id}..." -ForegroundColor Yellow

try {
    # Get first user ID from users_activity
    if ($usersActivity -and $usersActivity.Count -gt 0) {
        $testUserId = $usersActivity[0].user_id

        $userDetail = Invoke-RestMethod -Uri "$baseUrl/admin/monitor/user_activity/$testUserId?days=30" -Headers $headers

        Write-Host "   ✅ User activity detail endpoint working" -ForegroundColor Green
        Write-Host "   User: $($userDetail.user.email)" -ForegroundColor Gray
        Write-Host "   Activity items: $($userDetail.items.Count)" -ForegroundColor Gray

        if ($userDetail.items.Count -gt 0) {
            Write-Host "`n   Recent activities:" -ForegroundColor Gray
            $recent = $userDetail.items | Select-Object -First 3
            foreach ($item in $recent) {
                Write-Host "      $($item.exercise_type) - $($item.duration_seconds)s" -ForegroundColor Gray
                if ($item.pronunciation_score) {
                    Write-Host "         Score: $($item.pronunciation_score)" -ForegroundColor DarkGray
                }
            }
        }
    } else {
        Write-Host "   ⚠️  Skipped: No users available to test" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ User activity detail failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# ============================================================================
# SUMMARY
# ============================================================================
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "VERIFICATION COMPLETE" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ All admin endpoints tested" -ForegroundColor Green
Write-Host ""
Write-Host "📱 Next Steps for Android:" -ForegroundColor Yellow
Write-Host "   1. Build and run the app" -ForegroundColor Gray
Write-Host "   2. Login as admin (yoo@gmail.com)" -ForegroundColor Gray
Write-Host "   3. Navigate to Admin Dashboard tab" -ForegroundColor Gray
Write-Host "   4. Verify all sections display correctly:" -ForegroundColor Gray
Write-Host "      - Overview (stats cards)" -ForegroundColor Gray
Write-Host "      - Students (list with ALL users)" -ForegroundColor Gray
Write-Host "      - Groups (grouped statistics)" -ForegroundColor Gray
Write-Host "      - Recent Attempts (activity log)" -ForegroundColor Gray
Write-Host ""
Write-Host "🔧 If Android shows errors:" -ForegroundColor Yellow
Write-Host "   - Check logcat for serialization errors" -ForegroundColor Gray
Write-Host "   - Verify DTOs match server response format" -ForegroundColor Gray
Write-Host "   - Ensure isAdmin flag is set correctly" -ForegroundColor Gray
Write-Host ""

