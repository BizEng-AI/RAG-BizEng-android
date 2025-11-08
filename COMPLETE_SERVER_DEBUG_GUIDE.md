# 🔧 COMPLETE SERVER DEBUGGING GUIDE
**Date:** October 24, 2025

## 🚨 ISSUES DETECTED

Based on your error messages, here are the problems:

1. **❌ HTTP 500: Model not found** - Azure model deployment name is incorrect
2. **❌ Ngrok offline** - Your ngrok URL has expired or is not running
3. **❌ Pronunciation file error** - Fixed in Android app (now records proper WAV files)
4. **❌ Roleplay error correction not working** - Likely `roleplay_referee.py` not using Azure
5. **❌ Chat 500 error** - Azure configuration issues

---

## ✅ ANDROID APP FIXES COMPLETED

I've already fixed the Android app with:

### 1. **Pronunciation Recording Fixed** ✅
- Created `AudioRecorder.kt` - records proper WAV files (16kHz, mono, 16-bit PCM)
- Updated `PronunciationVm.kt` - uses real audio recording instead of STT
- Added comprehensive error logging with full debugging output

### 2. **Chat Debugging Enhanced** ✅
- Added detailed logging for every chat request
- Shows which endpoint is being called (/ask vs /chat)
- Catches and explains Azure model configuration errors
- Better error messages for 500 errors

### 3. **Roleplay Debugging Enhanced** ✅
- Added extensive logging for error correction detection
- Shows when errors are detected and what they are
- Logs correction feedback in detail
- Better error handling for server issues

---

## 🔥 SERVER FIXES NEEDED (DO THESE NOW!)

### Step 1: Update Ngrok URL

Your current ngrok URL is **OFFLINE**: `colette-unvoluble-nonsynoptically.ngrok-free.dev`

**Action:**
1. Open terminal/PowerShell
2. Run: `ngrok http 8020` (or whatever port your server uses)
3. Copy the new HTTPS URL (e.g., `xyz123.ngrok-free.app`)
4. Update Android app:

Open: `C:\Users\sanja\rag-biz-english\android\app\src\main\java\com\example\myapplication\di\NetworkModule.kt`

Change line ~59:
```kotlin
val PRODUCTION_SERVER_IP = "YOUR_NEW_NGROK_URL_HERE"  // Without https://
```

---

### Step 2: Fix Azure Model Configuration

The "Model not found" error means your Azure deployment names are wrong.

**Find your server settings file** (likely `settings.py` or `.env`):

```python
# ============================================================
# AZURE OPENAI CONFIGURATION
# ============================================================

USE_AZURE = True  # ✅ Make sure this is True

# Azure credentials
AZURE_OPENAI_KEY = "your-azure-key"
AZURE_OPENAI_ENDPOINT = "https://your-resource.openai.azure.com/"
AZURE_OPENAI_API_VERSION = "2024-02-15-preview"

# ⚠️ CRITICAL: These deployment names must match your Azure portal!
AZURE_OPENAI_CHAT_DEPLOYMENT = "gpt-4"  # ❌ This is probably WRONG
AZURE_OPENAI_EMBEDDING_DEPLOYMENT = "text-embedding-ada-002"  # ❌ Check this too
```

**How to find your correct deployment names:**

1. Go to: https://portal.azure.com
2. Navigate to your Azure OpenAI resource
3. Click "Model deployments" or "Deployments"
4. Copy the EXACT deployment name (not the model name!)

**Example:**
- Model: `gpt-4`
- Deployment name: `my-gpt4-deployment` ← Use this one!

**Fix your settings:**
```python
AZURE_OPENAI_CHAT_DEPLOYMENT = "my-gpt4-deployment"  # ✅ Your actual deployment name
AZURE_OPENAI_EMBEDDING_DEPLOYMENT = "my-embeddings"  # ✅ Your actual deployment name
```

---

### Step 3: Fix roleplay_referee.py (Error Correction)

The error correction isn't working because `roleplay_referee.py` is still using OpenAI directly.

**Find this file in your server** (probably in your Python server directory)

**Look for this code:**
```python
from openai import OpenAI
from settings import OPENAI_API_KEY, CHAT_MODEL

oai = OpenAI(api_key=OPENAI_API_KEY)
```

**Replace with:**
```python
from openai import OpenAI, AzureOpenAI
from settings import (
    OPENAI_API_KEY,
    CHAT_MODEL,
    USE_AZURE,
    AZURE_OPENAI_KEY,
    AZURE_OPENAI_ENDPOINT,
    AZURE_OPENAI_API_VERSION,
    AZURE_OPENAI_CHAT_DEPLOYMENT
)

if USE_AZURE:
    oai = AzureOpenAI(
        api_key=AZURE_OPENAI_KEY,
        azure_endpoint=AZURE_OPENAI_ENDPOINT,
        api_version=AZURE_OPENAI_API_VERSION
    )
    model_name = AZURE_OPENAI_CHAT_DEPLOYMENT
else:
    oai = OpenAI(api_key=OPENAI_API_KEY)
    model_name = CHAT_MODEL
```

**Then find all `oai.chat.completions.create()` calls and update them:**

Change:
```python
response = oai.chat.completions.create(
    model=CHAT_MODEL,  # ❌ Old way
    messages=[...]
)
```

To:
```python
response = oai.chat.completions.create(
    model=model_name,  # ✅ Uses Azure deployment name
    messages=[...]
)
```

---

### Step 4: Verify All Server Endpoints

**Run these checks:**

1. **Check /ask endpoint** (RAG queries):
   - Should use Azure embeddings
   - Should use Azure chat model

2. **Check /chat endpoint** (Free chat):
   - Should use Azure chat model

3. **Check /roleplay/start and /roleplay/turn**:
   - Should use Azure chat model
   - Error correction should work

4. **Check /pronunciation/assess**:
   - Should use Azure Speech Service

---

## 🧪 TESTING PROCEDURE

### After fixing server, test in this order:

1. **Start Server**
   ```bash
   cd your_server_directory
   python main.py  # or whatever starts your server
   ```

2. **Start Ngrok** (in another terminal)
   ```bash
   ngrok http 8020
   ```

3. **Update Android App** with new ngrok URL

4. **Rebuild APK**
   ```bash
   cd C:\Users\sanja\rag-biz-english\android
   gradlew assembleDebug
   ```

5. **Install and Test**
   - Install APK on device
   - Check Android Studio Logcat for detailed logs

---

## 📊 READING THE NEW DEBUG LOGS

### Chat Section Logs:
```
CHAT: ═══════════════════════════════════════
CHAT: SENDING CHAT MESSAGE
CHAT: User message: hello
CHAT: Grounded mode: true
CHAT: Using grounded (RAG) mode - calling /ask endpoint
CHAT: ✓ Grounded response received: ...
CHAT: ✓ Chat successful
```

### Roleplay Section Logs:
```
ROLEPLAY: ═══════════════════════════════════════
ROLEPLAY: SENDING ROLEPLAY TURN
ROLEPLAY: Student message: F off
ROLEPLAY: ✓ ROLEPLAY TURN SUCCESSFUL
ROLEPLAY: ⚠️ ERRORS DETECTED IN USER MESSAGE:
ROLEPLAY:   Error 1:
ROLEPLAY:     Type: inappropriate
ROLEPLAY:     Wrong: 'F off'
ROLEPLAY:     Correct: 'I would prefer not to continue'
ROLEPLAY:     Explanation: Inappropriate language in business context
```

### Pronunciation Logs:
```
PRONUNCIATION: ═══════════════════════════════════════
PRONUNCIATION: STARTING PRONUNCIATION RECORDING
PRONUNCIATION: Target phrase: Good morning
PRONUNCIATION: Recording to: /data/user/.../pronunciation_1761318130377.wav
PRONUNCIATION: ✓ Recording saved: 48000 bytes
PRONUNCIATION: STARTING PRONUNCIATION ASSESSMENT
PRONUNCIATION: ✓ ASSESSMENT SUCCESSFUL
PRONUNCIATION: Overall score: 85/100
```

---

## 🎯 EXPECTED BEHAVIOR AFTER FIXES

### Chat Section:
- ✅ No 500 errors
- ✅ Responses use Azure OpenAI
- ✅ RAG queries work with Azure embeddings

### Roleplay Section:
- ✅ Error correction detects profanity/mistakes
- ✅ Red correction messages appear for errors
- ✅ AI provides professional alternatives

### Pronunciation Section:
- ✅ Records actual audio (not just text)
- ✅ Sends WAV file to server
- ✅ Gets detailed pronunciation scores

---

## 🆘 IF STILL NOT WORKING

Check Android Studio Logcat and look for these tags:
- `CHAT`
- `ROLEPLAY`
- `PRONUNCIATION`
- `NETWORK`
- `NETWORK_CONFIG`

Copy the entire error log and check:
1. What endpoint is being called?
2. What is the exact error message?
3. Is it 404 (endpoint not found) or 500 (server error)?

---

## 📝 QUICK CHECKLIST

- [ ] Ngrok is running and URL is updated in NetworkModule.kt
- [ ] Server is running and accessible
- [ ] USE_AZURE = True in server settings
- [ ] AZURE_OPENAI_CHAT_DEPLOYMENT matches Azure portal deployment name
- [ ] AZURE_OPENAI_EMBEDDING_DEPLOYMENT matches Azure portal deployment name
- [ ] roleplay_referee.py updated to use Azure
- [ ] All API endpoints use Azure when USE_AZURE=True
- [ ] Android app rebuilt with new changes
- [ ] Testing in Logcat to see detailed error messages

---

## 🎉 YOU'RE READY!

After completing these fixes:
1. Your Android app will have proper audio recording
2. Server will use Azure OpenAI correctly
3. Error correction will work in roleplay
4. You'll have comprehensive debugging logs everywhere

Test each section systematically and check the Logcat logs!

