@echo off
echo.
echo ===================================
echo Testing Previously Failed Endpoints
echo ===================================
echo.

REM Get token
echo Getting admin token...
curl -s -X POST "https://bizeng-server.fly.dev/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"yoo@gmail.com\",\"password\":\"qwerty\"}" ^
  -o login_temp.json 2>nul

powershell -Command "$json = Get-Content login_temp.json | ConvertFrom-Json; $json.access_token" > token_temp.txt
set /p TOKEN=<token_temp.txt
echo Token obtained: %TOKEN:~0,20%...
echo.

echo ===================================
echo TEST 1: /users_activity
echo ===================================
curl -s -w "HTTP Status: %%{http_code}\n" ^
  "https://bizeng-server.fly.dev/admin/monitor/users_activity?days=30" ^
  -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo ===================================
echo TEST 2: /groups_activity
echo ===================================
curl -s -w "HTTP Status: %%{http_code}\n" ^
  "https://bizeng-server.fly.dev/admin/monitor/groups_activity?days=30" ^
  -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo ===================================
echo Tests Complete
echo ===================================

del login_temp.json token_temp.txt 2>nul
pause

