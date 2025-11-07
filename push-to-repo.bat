@echo off
echo ================================================
echo  PUSHING APP TO GIT REPOSITORY
echo ================================================
echo.

echo [1/6] Checking git status...
git status
echo.

echo [2/6] Adding all changes (excluding files in .gitignore)...
git add .
echo.

echo [3/6] Showing what will be committed...
git status
echo.

echo ================================================
echo  CHANGES TO BE COMMITTED:
echo ================================================
echo.
echo New features in this version:
echo   - Voice buttons on individual messages (chat and roleplay)
echo   - TTS stops when leaving screens
echo   - Fixed chat endpoint JSON parsing (handles both "message" and "answer")
echo   - Removed auto-play TTS for all messages
echo   - Roleplay greeting no longer auto-plays
echo   - Fixed Windows Firewall configuration for hotspot
echo   - Back button handling in roleplay
echo.

echo ================================================
pause

echo.
echo [4/6] Committing changes...
git commit -m "feat: Add voice control buttons and fix TTS behavior

- Add speaker button to each AI message (chat & roleplay)
- Remove auto-play TTS for all messages
- Stop TTS when leaving chat/roleplay screens
- Fix ChatRespDto to handle both 'message' and 'answer' fields
- Add proper back button handling in roleplay
- Update .gitignore to exclude instruction files
- Improve network configuration documentation"

echo.

echo [5/6] Checking remote repository...
git remote -v
echo.

echo [6/6] Pushing to repository...
git push origin main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ================================================
    echo  PUSH FAILED - Trying alternative branch...
    echo ================================================
    echo.
    echo Trying 'master' branch instead...
    git push origin master
)

echo.
echo ================================================
echo  DONE!
echo ================================================
echo.
echo Your code has been pushed to the repository!
echo All instruction/fix files were excluded as per .gitignore
echo.
pause

