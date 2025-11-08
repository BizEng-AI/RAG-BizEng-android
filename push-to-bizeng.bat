@echo off
REM Push Android project to BizEng-AI GitHub organization

echo Checking git status...
git status

echo.
echo Current remote:
git remote -v

echo.
echo Setting new remote to: https://github.com/BizEng-AI/backend.git
git remote set-url origin https://github.com/BizEng-AI/backend.git

echo.
echo New remote:
git remote -v

echo.
echo Adding all files...
git add .

echo.
echo Committing changes...
git commit -m "Android project - Business English RAG Application"

echo.
echo Pushing to new repository...
git push -u origin main

echo.
echo Done! Check GitHub repository for updates.
pause

