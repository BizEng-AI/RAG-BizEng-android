# 🚀 Initialize & Push Android Project to BizEng-AI/android

This guide explains how to initialize the Android client as a fresh git repository and push to the BizEng-AI GitHub organization.

## Prerequisites

✅ Git installed and configured with GitHub credentials  
✅ Access to `https://github.com/BizEng-AI/android.git` repository  
✅ GitHub account with admin/push permissions  

## Step-by-Step Instructions

### Option 1: Using the Provided Batch Script (EASIEST)

Simply run the batch script in the android directory:

```cmd
init-git-bizeng.bat
```

This will automatically:
1. ✅ Initialize git repository (removes old .git if exists)
2. ✅ Set branch to `main`
3. ✅ Add remote: `https://github.com/BizEng-AI/android.git`
4. ✅ Stage all files
5. ✅ Create initial commit
6. Display push command to run next

After the script completes, run:
```cmd
git push -u origin main
```

---

### Option 2: Using PowerShell (Manual)

Open PowerShell and run:

```powershell
cd C:\Users\sanja\rag-biz-english\android

# Remove old git if it exists
Remove-Item -Force -Recurse .git -ErrorAction SilentlyContinue

# Initialize new repository
git init

# Set branch to main
git branch -M main

# Add remote to BizEng-AI/android
git remote add origin https://github.com/BizEng-AI/android.git

# Verify remote
git remote -v

# Stage all files
git add .

# Create initial commit
git commit -m "Initial commit: Android Business English RAG Application

- Chat feature with free conversation and RAG-based Q&A
- Roleplay practice scenarios with AI referee
- Pronunciation assessment with IPA transcription
- Voice input/output support
- Full conversation history
- Built with Jetpack Compose and Ktor"

# Push to repository
git push -u origin main
```

### Option 2: Using Git Bash

```bash
cd /c/Users/sanja/rag-biz-english/android

# Follow the same commands as PowerShell above
git status
git remote -v
git remote set-url origin https://github.com/BizEng-AI/backend.git
git remote -v
git add .
git commit -m "Android Client: Business English RAG Application v1.0"
git push -u origin main
```

### Option 3: Using the Provided Batch Script

Run the batch file created in the android directory:

```cmd
push-to-bizeng.bat
```

This will automatically:
1. Check git status
2. Show current remote
3. Update remote to new URL
4. Stage all files
5. Commit changes
6. Push to the new repository

---

## Troubleshooting

### ❌ "Could not read Username"
**Solution:** GitHub requires authentication via personal access token or SSH.

**Using SSH (Recommended):**
```powershell
# Set up SSH key (one-time setup)
ssh-keygen -t ed25519 -C "your-email@example.com"

# Add public key to GitHub: Settings → SSH and GPG keys

# Update remote to use SSH
git remote set-url origin git@github.com:BizEng-AI/backend.git
git push -u origin main
```

**Using Personal Access Token:**
```powershell
# Generate token at: https://github.com/settings/tokens

# When prompted for password, paste the token instead
git push -u origin main
```

### ❌ "Permission denied (publickey)"
**Solution:** SSH key not set up or not added to GitHub.
- Generate SSH key: `ssh-keygen -t ed25519`
- Add public key to GitHub
- Test: `ssh -T git@github.com`

### ❌ "Fatal: Not a git repository"
**Solution:** Git initialization required.

```powershell
# Initialize git
git init

# Set branch to main
git branch -M main

# Add remote
git remote add origin https://github.com/BizEng-AI/android.git

# Stage and push
git add .
git commit -m "Initial commit: Android Business English RAG App"
git push -u origin main
```

### ❌ "Updates were rejected because the remote contains work"
**Solution:** Force push (use with caution!):

```powershell
git push -u origin main --force
```

⚠️ **WARNING:** Only use `--force` if you're sure about overwriting remote history.

### ❌ "LF will be converted to CRLF"
**Solution:** This is just a warning about line endings. It's safe to ignore or fix:

```powershell
git config core.autocrlf true
git add .
git commit -m "Normalize line endings"
git push
```

---

## Verification

After pushing, verify everything is on GitHub:

```powershell
# Check remote
git remote -v

# Should show:
# origin  https://github.com/BizEng-AI/android.git (fetch)
# origin  https://github.com/BizEng-AI/android.git (push)

# Check current branch
git branch -v

# View last commit
git log --oneline -5
```

Visit: https://github.com/BizEng-AI/android to see your Android project!

---

## What Gets Pushed

✅ Source code (app/src/main/)  
✅ Gradle configuration (build.gradle.kts, settings.gradle.kts)  
✅ Documentation (README.md, CLIENT_ARCHITECTURE.md)  
✅ .gitignore (configured to exclude build files)  

❌ Build artifacts (ignored)  
❌ .gradle directory (ignored)  
❌ .idea directory (ignored)  
❌ local.properties (not tracked for security)  

---

## Next Steps

1. ✅ Verify code on GitHub: https://github.com/BizEng-AI/backend
2. ✅ Add branch protection rules (Settings → Branches)
3. ✅ Set up CI/CD pipeline if needed
4. ✅ Create issues for known bugs or tasks
5. ✅ Set up discussions for team communication

---

## Quick Reference Commands

```powershell
# Status
git status

# View remotes
git remote -v

# Change remote
git remote set-url origin <NEW_URL>

# Stage changes
git add .

# Commit
git commit -m "Your message"

# Push
git push origin main

# View recent commits
git log --oneline -10

# Create new branch
git checkout -b feature/name

# Switch branch
git checkout branch-name

# Merge branch
git merge feature/name
```

---

## Team Collaboration

Once pushed, team members can clone and work:

```powershell
# Clone the repository
git clone https://github.com/BizEng-AI/android.git

# Navigate to project root
cd android

# Create feature branch
git checkout -b feature/your-feature

# Make changes and push
git push origin feature/your-feature

# Create pull request on GitHub
```

---

**Last Updated:** November 7, 2025  
**Repository:** https://github.com/BizEng-AI/android

