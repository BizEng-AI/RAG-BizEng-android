@echo off
echo ========================================
echo Fixing Android Studio Cache Issues
echo ========================================
echo.
echo This will delete cache folders to fix unresolved reference errors
echo.

cd /d "%~dp0"

echo Step 1: Deleting .gradle folder...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo ✓ Deleted .gradle
) else (
    echo - .gradle not found
)

echo.
echo Step 2: Deleting .idea folder...
if exist ".idea" (
    rmdir /s /q ".idea"
    echo ✓ Deleted .idea
) else (
    echo - .idea not found
)

echo.
echo Step 3: Deleting .kotlin folder...
if exist ".kotlin" (
    rmdir /s /q ".kotlin"
    echo ✓ Deleted .kotlin
) else (
    echo - .kotlin not found
)

echo.
echo Step 4: Deleting app/build folder...
if exist "app\build" (
    rmdir /s /q "app\build"
    echo ✓ Deleted app/build
) else (
    echo - app/build not found
)

echo.
echo Step 5: Deleting root build folder...
if exist "build" (
    rmdir /s /q "build"
    echo ✓ Deleted build
) else (
    echo - build not found
)

echo.
echo ========================================
echo Cache cleanup complete!
echo ========================================
echo.
echo Next steps:
echo 1. Close Android Studio if it's running
echo 2. Re-open your project in Android Studio
echo 3. Wait for Gradle sync and indexing to complete
echo 4. File ^> Invalidate Caches ^> Invalidate and Restart
echo.
pause

