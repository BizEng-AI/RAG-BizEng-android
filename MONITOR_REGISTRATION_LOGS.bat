@echo off
echo ====================================================================
echo 🔍 ANDROID REGISTRATION LOG MONITOR
echo ====================================================================
echo.
echo This will show ONLY the relevant authentication logs.
echo.
echo 📱 Instructions:
echo    1. Make sure your device is connected (adb devices)
echo    2. Press any key to start monitoring
echo    3. Open the app and try to register
echo    4. Press Ctrl+C when done to stop
echo.
pause

echo.
echo 🎯 Monitoring logs (filtering for auth-related tags)...
echo ====================================================================
echo.

adb logcat -c
adb logcat | findstr /C:"AuthApi" /C:"AuthRepository" /C:"AuthViewModel" /C:"AuthManager" /C:"TokenResponse" /C:"access_token" /C:"refresh_token"

