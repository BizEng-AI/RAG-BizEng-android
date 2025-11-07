@echo off
REM ============================================
REM ANDROID GIT INITIALIZATION GUIDE
REM Repository: https://github.com/BizEng-AI/android.git
REM ============================================

REM This batch file contains setup instructions and ready-to-run commands
REM To use: Copy and paste commands into PowerShell or Command Prompt

echo.
echo ============================================
echo Android Git Setup for BizEng-AI/android
echo ============================================
echo.
echo Location: C:\Users\sanja\rag-biz-english\android
echo Repository: https://github.com/BizEng-AI/android.git
echo.
echo ============================================
echo OPTION 1: AUTOMATED (EASIEST)
echo ============================================
echo Run: init-git-bizeng.bat
echo This script handles everything automatically
echo.
echo ============================================
echo OPTION 2: MANUAL COMMANDS (For troubleshooting)
echo ============================================
echo.
echo Step 1: Navigate to project directory
echo   cd C:\Users\sanja\rag-biz-english\android
echo.
echo Step 2: Remove old .git if needed
echo   rmdir /s /q .git
echo   (or in PowerShell: Remove-Item -Force -Recurse .git)
echo.
echo Step 3: Initialize git
echo   git init
echo.
echo Step 4: Set main branch
echo   git branch -M main
echo.
echo Step 5: Add remote
echo   git remote add origin https://github.com/BizEng-AI/android.git
echo.
echo Step 6: Verify remote
echo   git remote -v
echo.
echo Step 7: Stage all files
echo   git add .
echo.
echo Step 8: Create initial commit
echo   git commit -m "Initial commit: Android Business English RAG Application"
echo.
echo Step 9: Push to GitHub
echo   git push -u origin main
echo.
echo ============================================
echo AUTHENTICATION METHODS
echo ============================================
echo.
echo Method 1: Personal Access Token (PAT)
echo   1. Go to: https://github.com/settings/tokens
echo   2. Click "Generate new token"
echo   3. Select scopes: repo, gist
echo   4. Copy token
echo   5. When prompted for password, paste token
echo.
echo Method 2: SSH (Recommended for future)
echo   1. Generate SSH key: ssh-keygen -t ed25519
echo   2. Go to: https://github.com/settings/ssh
echo   3. Add public key from ~/.ssh/id_ed25519.pub
echo   4. Update remote: git remote set-url origin git@github.com:BizEng-AI/android.git
echo.
echo ============================================
echo VERIFICATION
echo ============================================
echo.
echo After pushing, verify success:
echo   git remote -v
echo     Should show: https://github.com/BizEng-AI/android.git
echo.
echo   git log --oneline
echo     Should show: "Initial commit: Android Business English RAG Application"
echo.
echo   Visit: https://github.com/BizEng-AI/android
echo     Should see all source files
echo.
echo ============================================
echo TROUBLESHOOTING
echo ============================================
echo.
echo Q: "Could not read Username"
echo A: Git needs authentication. Use PAT or SSH (see above)
echo.
echo Q: "Repository not found"
echo A: Check access to https://github.com/BizEng-AI/android
echo    Ask admin for write access
echo.
echo Q: "fatal: not a git repository"
echo A: Run init-git-bizeng.bat first
echo.
echo Q: "Permission denied (publickey)"
echo A: SSH key not set up. Use PAT method instead
echo.
echo ============================================
echo READY TO START!
echo ============================================
echo.
echo 1. Choose automated or manual option above
echo 2. Prepare GitHub credentials (PAT or SSH)
echo 3. Run the commands
echo 4. Verify success on GitHub
echo.
pause

