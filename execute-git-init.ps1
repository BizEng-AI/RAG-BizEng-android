# Initialize Git and Push to BizEng-AI/android
Set-Location "C:\Users\sanja\rag-biz-english\android"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Initializing Git for BizEng-AI/android" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Remove old .git if exists
if (Test-Path ".git") {
    Write-Host "Removing old .git directory..." -ForegroundColor Yellow
    Remove-Item -Force -Recurse .git
    Write-Host "✓ Removed old .git" -ForegroundColor Green
}

# Initialize git
Write-Host "Initializing git repository..." -ForegroundColor Yellow
git init
Write-Host "✓ Git initialized" -ForegroundColor Green
Write-Host ""

# Set branch to main
Write-Host "Setting branch to main..." -ForegroundColor Yellow
git branch -M main
Write-Host "✓ Branch set to main" -ForegroundColor Green
Write-Host ""

# Add remote
Write-Host "Adding remote: https://github.com/BizEng-AI/android.git" -ForegroundColor Yellow
git remote add origin https://github.com/BizEng-AI/android.git
Write-Host "✓ Remote added" -ForegroundColor Green
Write-Host ""

# Verify remote
Write-Host "Verifying remote configuration..." -ForegroundColor Yellow
git remote -v
Write-Host ""

# Stage all files
Write-Host "Staging all files..." -ForegroundColor Yellow
git add .
Write-Host "✓ Files staged" -ForegroundColor Green
Write-Host ""

# Create initial commit
Write-Host "Creating initial commit..." -ForegroundColor Yellow
$commitMessage = @"
Initial commit: Android Business English RAG Application

- Chat feature with free conversation and RAG-based Q&A
- Roleplay practice scenarios with AI referee
- Pronunciation assessment with IPA transcription
- Voice input/output support
- Full conversation history
- Built with Jetpack Compose and Ktor
"@

git commit -m $commitMessage
Write-Host "✓ Initial commit created" -ForegroundColor Green
Write-Host ""

# Push to GitHub
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Pushing to GitHub..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "You may be prompted for GitHub credentials." -ForegroundColor Yellow
Write-Host "Use Personal Access Token or SSH authentication." -ForegroundColor Yellow
Write-Host ""

git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ SUCCESS!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Repository pushed to: https://github.com/BizEng-AI/android" -ForegroundColor Green
    Write-Host ""
    Write-Host "Verify at: https://github.com/BizEng-AI/android" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "⚠ Push failed or requires authentication" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check:" -ForegroundColor Yellow
    Write-Host "1. GitHub credentials (PAT or SSH)" -ForegroundColor Yellow
    Write-Host "2. Access permissions to the repository" -ForegroundColor Yellow
    Write-Host "3. Network connection" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

