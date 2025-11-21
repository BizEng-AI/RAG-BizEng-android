@echo off
REM Test script for admin analytics endpoints
echo ====================================
echo Testing Admin Analytics Endpoints
echo ====================================
echo.

REM Set base URL
set BASE_URL=https://bizeng-server.fly.dev

echo Step 1: Login as admin...
curl -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"yoo@gmail.com\",\"password\":\"qwerty\"}" ^
  -o login_response.json 2>nul

echo Extracting access token...
powershell -Command "$json = Get-Content login_response.json | ConvertFrom-Json; $json.access_token | Out-File -Encoding ASCII token.txt"
set /p TOKEN=<token.txt

echo.
echo ====================================
echo Testing Overview Endpoint
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/overview" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o overview.json
echo Response saved to overview.json
type overview.json
echo.

echo ====================================
echo Testing Users Activity (Students)
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/users_activity?days=30" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o users_activity.json
echo Response saved to users_activity.json
type users_activity.json
echo.

echo ====================================
echo Testing Groups Activity
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/groups_activity?days=30" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o groups_activity.json
echo Response saved to groups_activity.json
type groups_activity.json
echo.

echo ====================================
echo Testing Active Today
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/active_today" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o active_today.json
echo Response saved to active_today.json
type active_today.json
echo.

echo ====================================
echo Testing Recent Attempts
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/recent_attempts?limit=7" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o recent_attempts.json
echo Response saved to recent_attempts.json
type recent_attempts.json
echo.

echo ====================================
echo Testing Attempts Daily
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/attempts_daily" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o attempts_daily.json
echo Response saved to attempts_daily.json
type attempts_daily.json
echo.

echo ====================================
echo Testing Users Signups Daily
echo ====================================
curl -X GET "%BASE_URL%/admin/monitor/users_signups_daily" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o signups_daily.json
echo Response saved to signups_daily.json
type signups_daily.json
echo.

echo ====================================
echo All Tests Complete!
echo ====================================
echo.
echo Check the JSON files for detailed responses:
echo - overview.json
echo - users_activity.json
echo - groups_activity.json
echo - active_today.json
echo - recent_attempts.json
echo - attempts_daily.json
echo - signups_daily.json
echo.

pause

