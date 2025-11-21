@echo off
REM ============================================================================
REM RUN ALL TESTS - Comprehensive Test Suite
REM ============================================================================
REM
REM This script runs all tests including:
REM - Unit tests
REM - Integration tests
REM - Server connectivity tests
REM
REM ============================================================================

echo.
echo ============================================================================
echo                    RUNNING COMPREHENSIVE TEST SUITE
echo ============================================================================
echo.

REM Run unit tests
echo [1/3] Running Unit Tests...
echo.
call gradlew test --no-daemon --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ UNIT TESTS FAILED!
    echo.
    echo Check the test results at:
    echo   app\build\reports\tests\testDebugUnitTest\index.html
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Unit tests passed!
echo.

REM Run integration tests (requires connected device/emulator)
echo [2/3] Running Integration Tests...
echo.
echo NOTE: This requires a connected Android device or emulator
echo.
call gradlew connectedAndroidTest --no-daemon --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ⚠️  INTEGRATION TESTS FAILED!
    echo.
    echo This may be because:
    echo   - No device/emulator is connected
    echo   - Device is offline
    echo   - Server is not accessible
    echo.
    echo Check the test results at:
    echo   app\build\reports\androidTests\connected\index.html
    echo.
)

echo.
echo [3/3] Running Server Connectivity Diagnostic...
echo.
echo NOTE: This will test the actual server endpoints
echo.
call gradlew test --tests "com.example.myapplication.integration.ServerConnectivityTest" --no-daemon --console=plain

echo.
echo ============================================================================
echo                         TEST SUITE COMPLETE
echo ============================================================================
echo.
echo 📊 Test Reports Available:
echo   - Unit Tests: app\build\reports\tests\testDebugUnitTest\index.html
echo   - Integration: app\build\reports\androidTests\connected\index.html
echo.
echo 📝 Logs Available:
echo   - Logcat: adb logcat -s "🔐 AuthApi" "🔍 ServerDiagnostics" "AuthViewModel" "AuthRepository"
echo.

pause

