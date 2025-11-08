@echo off
REM Initialize Android project for BizEng-AI/android GitHub repository

cd /d "%~dp0"

echo ========================================
echo Git Initialization for BizEng-AI/android
echo ========================================
echo.

REM Check if .git already exists
if exist .git (
    echo WARNING: Git repository already exists. Removing old .git directory...
    rmdir /s /q .git 2>nul
    echo Removed .git directory.
    echo.
)

REM Initialize new git repository
echo [1/7] Initializing git repository...
git init
if %errorlevel% neq 0 (
    echo ERROR: Git init failed. Is git installed?
    pause
    exit /b 1
)
echo OK - Git initialized
echo.

REM Set branch to main
echo [2/7] Setting branch to main...
git branch -M main
if %errorlevel% neq 0 (
    echo ERROR: Failed to set branch
    pause
    exit /b 1
)
echo OK - Branch set to main
echo.

REM Add remote
echo [3/7] Adding remote origin: https://github.com/BizEng-AI/android.git
git remote add origin https://github.com/BizEng-AI/android.git
if %errorlevel% neq 0 (
    echo WARNING: Remote may already exist, removing and re-adding...
    git remote remove origin 2>nul
    git remote add origin https://github.com/BizEng-AI/android.git
)
echo OK - Remote added
echo.

REM Verify configuration
echo [4/7] Verifying git configuration...
git remote -v
echo.

REM Stage all files
echo [5/7] Staging all files...
git add .
if %errorlevel% neq 0 (
    echo ERROR: Failed to stage files
    pause
    exit /b 1
)
echo OK - Files staged
echo.

REM Create initial commit
echo [6/7] Creating initial commit...
git commit -m "Initial commit: Android Business English RAG Application" -m "- Chat feature with free conversation and RAG-based Q&A" -m "- Roleplay practice scenarios with AI referee" -m "- Pronunciation assessment with IPA transcription" -m "- Voice input/output support" -m "- Full conversation history" -m "- Built with Jetpack Compose and Ktor"
if %errorlevel% neq 0 (
    echo WARNING: Commit may have failed or nothing to commit
    git status
    echo.
) else (
    echo OK - Initial commit created
)
echo.

REM Push to GitHub
echo [7/7] Pushing to GitHub...
echo.
echo ========================================
echo AUTHENTICATION REQUIRED
echo ========================================
echo You will be prompted for GitHub credentials.
echo Use one of:
echo   - Personal Access Token as password
echo   - SSH key authentication
echo.
echo Pushing now...
echo.

git push -u origin main

if %errorlevel% eq 0 (
    echo.
    echo ========================================
    echo SUCCESS! Repository pushed to GitHub
    echo ========================================
    echo.
    echo Visit: https://github.com/BizEng-AI/android
    echo.
) else (
    echo.
    echo ========================================
    echo PUSH FAILED OR REQUIRES ACTION
    echo ========================================
    echo.
    echo Possible issues:
    echo 1. Authentication failed - check credentials
    echo 2. Repository doesn't exist on GitHub
    echo 3. No write access to repository
    echo.
    echo To retry push manually:
    echo   git push -u origin main
    echo.
    echo For SSH authentication:
    echo   git remote set-url origin git@github.com:BizEng-AI/android.git
    echo   git push -u origin main
    echo.
)

echo.
pause

