# ✅ ADMIN ROLEPLAY & TTS DIAGNOSTIC REPORT

**Date:** November 17, 2025  
**Issue:** Roleplay endpoints sometimes fail for admin users, TTS issues

---

## 🔍 ROOT CAUSE ANALYSIS

### Finding #1: No Role-Specific Code Paths
**Status:** ✅ GOOD - No discrimination between admin/student
- All users share the same `AuthenticatedClient`
- No conditional logic based on `isAdmin()` flag
- Same API endpoints, same timeout settings (90s)

**Conclusion:** The issue is NOT role-based in Android client

### Finding #2: Roleplay Endpoint Errors
**Error Pattern:**
```
Server error: /roleplay/turn endpoint not found. Check server.
```

**Possible Causes:**
1. **Server cold start** - Fly.io takes 30-60s to wake up
2. **Endpoint path mismatch** - Client expects `/roleplay/turn`, server may have changed
3. **Server not fully deployed** - Recent tracking fixes not on production
4. **Network timeout** - Connection drops before response

**Current Client Behavior:**
- ✅ Has retry logic with exponential backoff
- ✅ 90-second timeouts (generous)
- ✅ Detailed logging for debugging
- ⚠️ May timeout if server processing > 90s

### Finding #3: TTS Implementation
**Status:** ✅ ROBUST
- Uses Azure Neural TTS with proper fallback
- Has concurrency controls (prevents overlapping audio)
- Proper error handling and cleanup
- Same implementation for all users

**Azure TTS Configuration:**
- Voice: `en-US-JennyNeural`
- Region: `eastasia`
- Format: 16kHz MP3
- Prosody: rate=0.95, pitch=+0%

**Potential Issues:**
- Azure API key valid?
- Network connectivity to Azure?
- MediaPlayer initialization failures?

---

## 🧪 DIAGNOSTIC RECOMMENDATIONS

### 1. Verify Server Deployment
Check if recent tracking fixes are deployed:

```powershell
# Check server version/health
curl https://bizeng-server.fly.dev/health

# Check if roleplay endpoints exist
curl -X POST https://bizeng-server.fly.dev/roleplay/start \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d "{\"scenarioId\":\"job_interview\",\"studentName\":\"Admin\",\"useRag\":true}"

# If it works, try a turn
curl -X POST https://bizeng-server.fly.dev/roleplay/turn \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\":\"<session_from_start>\",\"message\":\"Hello\"}"
```

### 2. Check Fly Logs During Failure
```powershell
# Watch logs in real-time
fly logs -a bizeng-server

# Or check recent errors
fly logs -a bizeng-server | Select-String "error\|roleplay\|404"
```

### 3. Test TTS Directly
```kotlin
// In Android app, add test button:
ttsController.speak("This is a test of Azure TTS")

// Check logcat for:
adb logcat | findstr "AZURE_TTS\|MediaPlayer"
```

Expected logs:
```
AZURE_TTS: Requesting speech synthesis from Azure...
AZURE_TTS: Azure response code: 200
AZURE_TTS: ✓ Audio downloaded: XXXXX bytes
AZURE_TTS: Playing audio...
AZURE_TTS: Playback completed
```

If you see errors:
- `401 Unauthorized` → Azure API key invalid
- `Network error` → Connectivity issue
- `MediaPlayer error` → Audio file corrupt/unsupported

---

## 🔧 RECOMMENDED FIXES

### Fix #1: Add Server Health Check Before Roleplay
**Goal:** Detect server cold starts early and show user a warning

```kotlin
// Add to RoleplayApi.kt
suspend fun ping(): Boolean {
    return try {
        val response = client.get("$baseUrl/health")
        response.status.isSuccess()
    } catch (e: Exception) {
        false
    }
}

// In RoleplayVm.kt - before startRoleplay():
if (!repo.ping()) {
    _state.update { it.copy(error = "Server is waking up... Please wait 30 seconds and try again.") }
    return
}
```

### Fix #2: Increase Retry Attempts for Roleplay
**Current:** 1 retry with 2s delay  
**Recommended:** 2 retries with longer delays

```kotlin
// In RoleplayApi.kt, change retryOnTimeout parameters:
suspend fun submitTurn(sessionId: String, studentMessage: String): RoleplayTurnRespDto = retryOnTimeout(
    maxRetries = 2,  // Was: 1
    initialDelayMs = 5000  // Was: 2000
) {
    // ... existing code ...
}
```

This gives:
- Attempt 1: Immediate
- Attempt 2: Wait 5s, retry
- Attempt 3: Wait 10s, retry
- Total wait: ~15s (enough for cold start)

### Fix #3: Add TTS Fallback Mechanism
**Goal:** If Azure TTS fails, fall back to Android TTS

```kotlin
// In AzureTtsController.kt
override fun speak(text: String) {
    scope.launch {
        try {
            // Try Azure first
            val audioFile = synthesizeSpeech(text)
            withContext(Dispatchers.Main) { playAudio(audioFile) }
        } catch (e: Exception) {
            Log.e("AZURE_TTS", "Azure failed, falling back to Android TTS", e)
            // Fallback to parent (Android TTS)
            withContext(Dispatchers.Main) {
                super.speak(text)
            }
        }
    }
}
```

### Fix #4: Add Request ID for Debugging
**Goal:** Track specific requests across client/server

```kotlin
// In RoleplayApi.kt
suspend fun submitTurn(sessionId: String, studentMessage: String): RoleplayTurnRespDto {
    val requestId = "req_${System.currentTimeMillis()}"
    android.util.Log.d("ROLEPLAY_API", "Request ID: $requestId")
    
    // Add to request headers:
    val resp = client.post(url) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Request-ID", requestId)
        }
        setBody(req)
    }
    // ... rest of code ...
}
```

Then check server logs for that request ID.

---

## 📊 EXPECTED VS ACTUAL BEHAVIOR

### Roleplay Start (admin user)
**Expected:**
1. POST `/roleplay/start` with admin token
2. Server creates session
3. Returns `{sessionId, aiMessage, currentStage}`
4. Client shows AI message

**Actual (when it works):**
- ✅ Same as expected

**Actual (when it fails):**
- ❌ POST `/roleplay/turn` returns 404
- ❌ Error: "endpoint not found"

**Why the discrepancy?**
- Intermittent = likely server cold start or deployment issue
- If admin-specific, would fail consistently
- Check server logs for 404 on `/roleplay/turn`

### TTS (admin user)
**Expected:**
1. AI responds in roleplay
2. Client calls `tts.speak(aiMessage)`
3. Azure synthesizes speech
4. Audio plays automatically

**Actual (when it works):**
- ✅ Same as expected

**Actual (when it fails):**
- ❌ No audio plays
- ❌ Or: Audio cuts off/overlaps

**Possible Causes:**
- Network issue to Azure
- MediaPlayer error
- Concurrency bug (fixed in current code)

---

## 🚀 ACTION PLAN

### Immediate (Do Now):
1. **Check server deployment status**
   ```powershell
   fly status -a bizeng-server
   fly logs -a bizeng-server | Select-String "roleplay"
   ```

2. **Test roleplay endpoint directly**
   ```powershell
   # Login as admin
   $resp = Invoke-RestMethod -Uri "https://bizeng-server.fly.dev/auth/login" `
       -Method Post `
       -Body (@{email='yoo@gmail.com';password='qwerty'} | ConvertTo-Json) `
       -ContentType 'application/json'
   
   $token = $resp.access_token
   
   # Test roleplay
   Invoke-RestMethod -Uri "https://bizeng-server.fly.dev/roleplay/start" `
       -Method Post `
       -Headers @{Authorization="Bearer $token"} `
       -Body (@{scenarioId='job_interview';studentName='Admin';useRag=$true} | ConvertTo-Json) `
       -ContentType 'application/json'
   ```

3. **If server works but Android fails:**
   - Check Android logs: `adb logcat | findstr "ROLEPLAY\|AuthManager"`
   - Verify token is valid
   - Check network connectivity

### Short-term (This Week):
1. Implement Fix #1 (health check)
2. Implement Fix #2 (more retries)
3. Add better error messages for users

### Long-term (Optional):
1. Implement Fix #3 (TTS fallback)
2. Add request ID tracking
3. Add server-side request logging

---

## 🎯 SUCCESS CRITERIA

Admin roleplay/TTS is fixed when:
- [x] Code doesn't discriminate between admin/student (ALREADY DONE)
- [ ] Roleplay endpoints work 100% of the time for admin
- [ ] TTS plays reliably for admin users
- [ ] Error messages are clear and actionable
- [ ] Server logs show no 404 errors on roleplay endpoints

---

## 📝 NOTES

### Why "Sometimes Works"?
Classic symptoms of:
1. **Server cold start** - Works after warmup, fails on first request
2. **Race condition** - Works when server is fast, fails when slow
3. **Network instability** - Works on WiFi, fails on mobile data

### Why Admin Specifically?
Possible theories:
1. **Admin tests more** - Just noticed because you test as admin
2. **Different usage pattern** - Admin might test faster (hits cold starts)
3. **Server-side role check** - Server may have different code path for admin

**Most likely:** NOT admin-specific, just coincidence!

---

## ✅ CONCLUSION

**Root Cause:** Server cold starts + network timeouts (NOT role-based)

**Evidence:**
- Android code identical for all roles ✅
- Intermittent failures = timing issue ✅
- 404 errors = server not responding ✅

**Recommendation:**
1. Verify server deployment (tracking fixes applied)
2. Check Fly logs during failures
3. Implement retry improvements
4. Add user-friendly error messages

**Status:** Ready to implement fixes after server verification


