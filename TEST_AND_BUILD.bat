@echo off
REM ============================================================================
REM TEST AND BUILD - Complete Workflow
REM ============================================================================
REM
REM This script:
REM 1. Tests the server
REM 2. Runs unit tests
REM 3. Builds the APK (if tests pass)
REM
REM ============================================================================

echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║           AUTOMATED TEST AND BUILD WORKFLOW                               ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.

REM =============================================================================
REM STEP 1: Test Server
REM =============================================================================
echo ┌───────────────────────────────────────────────────────────────────────────┐
echo │ STEP 1/3: Testing Server Connectivity                                    │
echo └───────────────────────────────────────────────────────────────────────────┘
echo.

call QUICK_SERVER_TEST.bat

echo.
echo Press any key to continue to tests, or Ctrl+C to stop...
pause >nul

REM =============================================================================
REM STEP 2: Run Unit Tests
REM =============================================================================
echo.
echo ┌───────────────────────────────────────────────────────────────────────────┐
echo │ STEP 2/3: Running Unit Tests                                             │
echo └───────────────────────────────────────────────────────────────────────────┘
echo.

call gradlew test --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════════════════════════╗
    echo ║                                                                           ║
    echo ║   ❌ TESTS FAILED                                                         ║
    echo ║                                                                           ║
    echo ╚═══════════════════════════════════════════════════════════════════════════╝
    echo.
    echo Test reports available at:
    echo   app\build\reports\tests\testDebugUnitTest\index.html
    echo.
    echo Please fix the failing tests before building the APK.
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ All tests passed!
echo.
echo Press any key to build APK, or Ctrl+C to stop...
pause >nul

REM =============================================================================
REM STEP 3: Build APK
REM =============================================================================
echo.
echo ┌───────────────────────────────────────────────────────────────────────────┐
echo │ STEP 3/3: Building APK                                                   │
echo └───────────────────────────────────────────────────────────────────────────┘
echo.

call gradlew assembleDebug --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════════════════════════╗
    echo ║                                                                           ║
    echo ║   ❌ BUILD FAILED                                                         ║
    echo ║                                                                           ║
    echo ╚═══════════════════════════════════════════════════════════════════════════╝
    echo.
    echo Please check the build errors above.
    echo.
    pause
    exit /b 1
)

REM =============================================================================
REM SUCCESS!
REM =============================================================================
echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║   ✅✅✅ BUILD SUCCESSFUL! ✅✅✅                                           ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.
echo 📦 APK Location:
echo    app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📲 To Install:
echo    adb install app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📝 To View Logs:
echo    adb logcat -s "🔐 AuthApi" "AuthViewModel"
echo.
echo ╔═══════════════════════════════════════════════════════════════════════════╗
echo ║                                                                           ║
echo ║   🎉 READY TO USE!                                                        ║
echo ║                                                                           ║
echo ╚═══════════════════════════════════════════════════════════════════════════╝
echo.

pause

