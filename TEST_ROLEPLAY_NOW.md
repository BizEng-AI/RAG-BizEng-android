# 🧪 QUICK TEST GUIDE - ANDROID ROLEPLAY

## ✅ CHANGES APPLIED

1. **Fixed scenario ID:** `business_phone_call` → `business_call`
2. **Added roleplay test:** Automatically tests `/roleplay/start` on app launch
3. **Verified all DTOs:** Match server expectations perfectly

---

## 📱 TESTING STEPS

### Step 1: Launch App & Check Automatic Tests

**Action:** Open the app on your Samsung device

**Expected Logcat Output:**
```
NETWORK_TEST: Test 1/4: Health Check...
NETWORK_TEST: ✅ Health Check: {"status":"ok"}

NETWORK_TEST: Test 2/4: RAG Search Endpoint...
RAG_TEST: ✅ Found approximately 5 results

NETWORK_TEST: Test 3/4: Chat Endpoint...
CHAT_TEST: ✅ SUCCESS!

NETWORK_TEST: Test 4/4: Roleplay Start Endpoint...
ROLEPLAY_TEST: ✅ SUCCESS!
ROLEPLAY_TEST: Session ID: 356cc996-658e...
ROLEPLAY_TEST: Scenario: Job Interview
```

**If this succeeds:** Roleplay endpoints are working! ✅

---

### Step 2: Test Roleplay Feature Manually

**Action:** Navigate to Roleplay tab in the app

**Steps:**
1. Tap "Roleplay" tab
2. Select "Job Interview" from scenarios
3. Tap "Start Session" button

**Expected:**
- ✅ AI greeting appears: "Good morning! Thank you for coming in today..."
- ✅ Input field is active

---

### Step 3: Send a Test Message

**Action:** Type and send a message

**Try this message (intentionally has an error):**
```
Hello, I'm gonna work at your company
```

**Expected AI Response:**
- AI asks follow-up question
- Shows correction for "gonna" → "going to"
- Explains: "Use formal language in interviews"

**Expected Logcat:**
```
ROLEPLAY: ═══════════════════════════════════════
ROLEPLAY: SENDING ROLEPLAY TURN
ROLEPLAY: Student message: Hello, I'm gonna work at your company
ROLEPLAY: ✓ ROLEPLAY TURN SUCCESSFUL
ROLEPLAY: Has errors: true
ROLEPLAY: Error count: 1
ROLEPLAY:   Error 1:
ROLEPLAY:     Type: register
ROLEPLAY:     Wrong: 'gonna'
ROLEPLAY:     Correct: 'going to'
```

---

### Step 4: Send a Correct Message

**Action:** Try a proper professional message

**Try this message:**
```
I have five years of experience in business development.
```

**Expected:**
- AI asks about your experience
- No corrections shown (because message is correct)

**Expected Logcat:**
```
ROLEPLAY: Has errors: false
ROLEPLAY: ✓ No errors - message was correct
```

---

## 🔍 TROUBLESHOOTING

### If Step 1 Fails (Automatic Test)

**Symptom:** `ROLEPLAY_TEST: ❌ FAILED!`

**Check:**
1. Is the server running?
   - Test: `python test_flyio_roleplay.py`
   - Should show: "Test 5: ✅ SUCCESS!"

2. Is the device connected to internet?
   - Try opening browser on device
   - Check WiFi settings

3. Check error message:
   - If "404": Server doesn't have roleplay module
   - If "timeout": Server is slow, wait 30 seconds and relaunch app
   - If "connection refused": Network issue

---

### If Step 2 Fails (Start Session Button)

**Symptom:** Nothing happens when tapping "Start Session"

**Check Logcat for:**
```
DEBUG_ROLEPLAY: Calling /roleplay/start with scenario: job_interview
```

**If you see error:**
- "Session not found" → Server restarted, session cleared
- "404" → Wrong endpoint
- "400" → Bad request data

---

### If Step 3 Fails (No Corrections Showing)

**Symptom:** AI responds but no corrections appear

**Check Logcat for:**
```
ROLEPLAY: Correction object RAW: ...
ROLEPLAY: Has errors: ...
ROLEPLAY: toDisplayString() returned NULL
```

**If `toDisplayString() returned NULL`:**
- Server sent corrections in unexpected format
- Share the "Correction object RAW" line with developer

---

## 📊 SUCCESS INDICATORS

### ✅ Everything Working:
- [ ] Automatic test shows "ROLEPLAY_TEST: ✅ SUCCESS!"
- [ ] Start Session creates AI greeting
- [ ] Sending messages gets AI responses
- [ ] Errors in messages show corrections in red
- [ ] Correct messages show no corrections

### ❌ Something Wrong:
- [ ] Automatic test shows "❌ FAILED!"
- [ ] Start Session button does nothing
- [ ] Send button does nothing
- [ ] No AI response appears
- [ ] Corrections don't show up

---

## 🎯 QUICK COMMANDS

### View Logs (Run in CMD):
```cmd
adb logcat -s NETWORK_TEST:D ROLEPLAY_TEST:D ROLEPLAY:D ROLEPLAY_API:D
```

### Test Server (Run in CMD):
```cmd
python test_flyio_roleplay.py
```

### Reinstall App (if needed):
```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 📝 WHAT TO REPORT

If something doesn't work, share these:

1. **Automatic test result:**
   - Copy the "ROLEPLAY_TEST" lines from logcat

2. **Error message:**
   - Copy any "❌ FAILED!" or "ERROR:" lines

3. **What you tried:**
   - "Tapped Start Session"
   - "Typed message and sent"
   - etc.

4. **Server test result:**
   - Output from `python test_flyio_roleplay.py`

---

**Ready to test!** 🚀

Open the app now and check if the automatic tests pass!

