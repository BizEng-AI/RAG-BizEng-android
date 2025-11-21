@echo off
echo ╔═══════════════════════════════════════════════════════════╗
echo ║        RUNNING AUTHENTICATION TESTS                       ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo 📝 Test Summary:
echo    - AuthManagerTest (8 tests)
echo    - AuthRepositoryTest (10 tests)
echo    - AuthViewModelTest (11 tests)
echo    - TrackingRepositoryTest (8 tests)
echo    - AuthenticationIntegrationTest (8 tests)
echo    Total: 45 tests
echo.

echo ⏳ Running tests...
echo.

call gradlew.bat test --tests "com.example.myapplication.data.local.AuthManagerTest" ^
                        --tests "com.example.myapplication.data.repository.AuthRepositoryTest" ^
                        --tests "com.example.myapplication.ui.auth.AuthViewModelTest" ^
                        --tests "com.example.myapplication.data.repository.TrackingRepositoryTest" ^
                        --tests "com.example.myapplication.integration.AuthenticationIntegrationTest" ^
                        --console=plain

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ╔═══════════════════════════════════════════════════════════╗
    echo ║              ✅ ALL TESTS PASSED!                         ║
    echo ╚═══════════════════════════════════════════════════════════╝
    echo.
    echo 🎉 Authentication system is ready for APK build!
    echo.
    echo Next steps:
    echo   1. Review test results above
    echo   2. Run: gradlew.bat assembleDebug
    echo   3. Install: gradlew.bat installDebug
    echo.
) else (
    echo.
    echo ╔═══════════════════════════════════════════════════════════╗
    echo ║              ❌ TESTS FAILED!                             ║
    echo ╚═══════════════════════════════════════════════════════════╝
    echo.
    echo ⚠️  Please fix failing tests before building APK
    echo.
    echo Test report location:
    echo   app\build\reports\tests\testDebugUnitTest\index.html
    echo.
)

pause

