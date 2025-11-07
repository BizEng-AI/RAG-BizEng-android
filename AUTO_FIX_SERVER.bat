@echo off
echo ============================================================
echo AUTOMATIC SERVER FIX SCRIPT
echo ============================================================
echo.
echo This script will:
echo 1. Find your main server file
echo 2. Check your settings/config
echo 3. Show you what needs to be fixed
echo.

cd /d "C:\Users\sanja\rag-biz-english"

echo Step 1: Looking for server files...
echo.

if exist "server.py" (
    echo [FOUND] server.py
    echo Location: C:\Users\sanja\rag-biz-english\server.py
    echo.
)

if exist "main.py" (
    echo [FOUND] main.py
    echo Location: C:\Users\sanja\rag-biz-english\main.py
    echo.
)

if exist "app.py" (
    echo [FOUND] app.py
    echo Location: C:\Users\sanja\rag-biz-english\app.py
    echo.
)

if exist "settings.py" (
    echo [FOUND] settings.py
    echo Location: C:\Users\sanja\rag-biz-english\settings.py
    echo.
    echo === SETTINGS FILE CONTENT ===
    type settings.py
    echo.
    echo === END SETTINGS ===
    echo.
)

if exist ".env" (
    echo [FOUND] .env
    echo Location: C:\Users\sanja\rag-biz-english\.env
    echo.
    echo === ENV FILE CONTENT ===
    type .env
    echo.
    echo === END ENV ===
    echo.
)

if exist "config.py" (
    echo [FOUND] config.py
    echo Location: C:\Users\sanja\rag-biz-english\config.py
    echo.
)

echo.
echo Step 2: Looking for roleplay_referee.py...
echo.

if exist "roleplay_referee.py" (
    echo [FOUND] roleplay_referee.py
    echo Location: C:\Users\sanja\rag-biz-english\roleplay_referee.py
    echo.
    echo Checking if it uses Azure...
    findstr /C:"AzureOpenAI" roleplay_referee.py >nul
    if errorlevel 1 (
        echo [WARNING] roleplay_referee.py does NOT use AzureOpenAI!
        echo This is why error correction isn't working!
        echo.
    ) else (
        echo [OK] roleplay_referee.py uses AzureOpenAI
        echo.
    )
)

echo.
echo Step 3: Checking all Python files for Azure configuration...
echo.

dir /b *.py 2>nul
if errorlevel 1 (
    echo No Python files found in C:\Users\sanja\rag-biz-english
    echo.
    echo Searching subdirectories...
    dir /s /b *.py | findstr /v "android\\app\\build" | findstr /v "__pycache__" | findstr /v "venv"
)

echo.
echo ============================================================
echo ANALYSIS COMPLETE
echo ============================================================
echo.
echo Please review the output above and share:
echo 1. Which server file exists (server.py, main.py, or app.py)
echo 2. The settings/config content
echo 3. Whether roleplay_referee.py needs fixing
echo.
pause

