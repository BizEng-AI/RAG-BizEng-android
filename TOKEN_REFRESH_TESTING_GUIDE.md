# 🧪 TOKEN REFRESH - MANUAL TESTING GUIDE

## 🎯 Purpose
This guide helps you manually test the token refresh implementation to ensure it works correctly with the live server.

---

## 📋 Prerequisites

- ✅ Server running at: `https://bizeng-server.fly.dev`
- ✅ Test account created (or ability to create one)
- ✅ Android app installed on device/emulator
- ✅ ADB access for viewing logs

---

## 🧪 TEST SCENARIOS

### ✅ TEST 1: Basic Login Flow (Verify Tokens are Saved)

**Steps:**
1. Open the app
2. Go to Login/Register screen
3. Register or login with test account:
   - Email: `test@example.com`
   - Password: `TestPass123!`
4. Check logs for token storage

**Expected Logs:**
```
🔐 AuthApi: ✅ Access token present: true (eyJhbGciOiJIUzI1NiI...)
🔐 AuthApi: ✅ Refresh token present: true (eyJhbGciOiJIUzI1NiI...)
```

**Success Criteria:**
- ✅ Login succeeds
- ✅ Tokens are saved in EncryptedSharedPreferences
- ✅ User is navigated to main screen
- ✅ No errors in logs

---

### ✅ TEST 2: Authenticated API Call (Token is Added to Request)

**Steps:**
1. After logging in, navigate to Chat screen
2. Send a message
3. Check logs for authorization header

**Expected Logs:**
```
🔐 AUTH_CLIENT: 🔑 Adding access token to request
🔐 AUTH_CLIENT: 📥 Response status: 200 OK
```

**Success Criteria:**
- ✅ Request includes `Authorization: Bearer <token>` header
- ✅ Server accepts the token
- ✅ Response is successful (200 OK)
- ✅ Chat response is displayed

---

### ✅ TEST 3: Token Refresh (Automatic - Wait 31 Minutes)

**⚠️ This is the MAIN test for token refresh functionality**

**Steps:**
1. Login to the app
2. Use any feature (Chat, Roleplay, etc.) - verify it works
3. **WAIT 31 MINUTES** (access token expires after 30 minutes)
4. Use the same feature again
5. Watch the logs carefully

**Expected Logs:**
```
🔐 AUTH_CLIENT: 📥 Response status: 401 Unauthorized
🔐 AUTH_CLIENT: ⚠️ Got 401 Unauthorized - token expired!
🔐 AUTH_CLIENT: 🔄 Attempting token refresh...
🔐 AuthInterceptor: 📤 Calling /auth/refresh endpoint...
🔐 AuthInterceptor: ✅ Token refresh successful!
🔐 AuthInterceptor: ✅ New access token: eyJhbGciOiJIUzI1NiI...
🔐 AUTH_CLIENT: ✅ Token refresh successful, retrying original request...
🔐 AUTH_CLIENT: 📥 Retry response status: 200 OK
```

**Success Criteria:**
- ✅ First request returns 401
- ✅ Refresh is automatically triggered
- ✅ New tokens are obtained
- ✅ Original request is retried with new token
- ✅ Request succeeds
- ✅ **User doesn't notice anything - no logout, no error!**

---

### ✅ TEST 4: Invalid Refresh Token (Logout Scenario)

**Steps:**
1. Login to the app
2. Manually corrupt the refresh token:
   - Use ADB: `adb shell`
   - Navigate to app data
   - Clear encrypted preferences (simulates invalid token)
3. Wait 31 minutes OR manually trigger a 401
4. Try to use the app

**Expected Logs:**
```
🔐 AUTH_CLIENT: ⚠️ Got 401 Unauthorized - token expired!
🔐 AUTH_CLIENT: 🔄 Attempting token refresh...
🔐 AuthInterceptor: ❌ Token refresh failed: [error message]
🔐 AuthInterceptor: ❌ Clearing all tokens and logging out user
```

**Success Criteria:**
- ✅ Refresh fails (invalid token)
- ✅ All tokens are cleared
- ✅ User is navigated to login screen
- ✅ No app crash

---

### ✅ TEST 5: Multiple Concurrent Requests (Mutex Protection)

**Steps:**
1. Login to the app
2. Wait 31 minutes
3. Quickly navigate between screens that make API calls:
   - Open Chat → Send message
   - Open Roleplay → Start scenario
   - Open RAG/Ask → Ask question
4. Check logs

**Expected Behavior:**
```
🔐 AUTH_CLIENT: ⚠️ Got 401 Unauthorized - token expired!
🔐 AUTH_CLIENT: ⚠️ Got 401 Unauthorized - token expired!
🔐 AUTH_CLIENT: ⚠️ Got 401 Unauthorized - token expired!
🔐 AUTH_CLIENT: 🔄 Attempting token refresh...
🔐 AuthInterceptor: 📤 Calling /auth/refresh endpoint...
🔐 AuthInterceptor: ✅ Token refresh successful!
🔐 AUTH_CLIENT: ✅ Token refresh successful, retrying original request...
🔐 AUTH_CLIENT: ✅ Token refresh successful, retrying original request...
🔐 AUTH_CLIENT: ✅ Token refresh successful, retrying original request...
```

**Success Criteria:**
- ✅ Multiple 401 responses detected
- ✅ Only ONE refresh call is made (mutex prevents duplicates)
- ✅ All requests retry with new token
- ✅ All requests succeed

---

## 🔍 HOW TO VIEW LOGS

### Using Android Studio:
1. Open Android Studio
2. Go to **Logcat** tab (bottom panel)
3. Filter by tag: `🔐`
4. Set level to: **Debug**

### Using ADB:
```bash
# View all auth-related logs
adb logcat | grep "🔐"

# View specific tags
adb logcat -s "AUTH_CLIENT" "AuthInterceptor" "AuthApi"

# Clear logs and start fresh
adb logcat -c
adb logcat | grep "🔐"
```

---

## 📊 VERIFICATION CHECKLIST

After running all tests, verify:

- [ ] ✅ **TEST 1**: Login saves tokens correctly
- [ ] ✅ **TEST 2**: API calls include Authorization header
- [ ] ✅ **TEST 3**: Token refresh works automatically (31+ min wait)
- [ ] ✅ **TEST 4**: Invalid refresh token triggers logout
- [ ] ✅ **TEST 5**: Concurrent requests only refresh once

---

## 🐛 TROUBLESHOOTING

### Issue: No logs appearing
**Solution:** Make sure log level is set to DEBUG in Android Studio Logcat

### Issue: Token refresh not triggering
**Possible causes:**
- Token hasn't actually expired yet (wait full 31 minutes)
- AuthInterceptor not installed properly
- Check if `AuthenticatedClientProvider` is being used

### Issue: Multiple refresh calls happening
**Possible cause:**
- Mutex not working correctly
- Check `AuthInterceptor.refreshMutex` implementation

### Issue: App crashes on 401
**Possible cause:**
- Error handling not working
- Check try-catch blocks in `AuthInterceptor.handleUnauthorized()`

---

## 📞 TESTING WITH CURL (Server-Side Verification)

### 1. Register/Login to get tokens:
```bash
curl -X POST https://bizeng-server.fly.dev/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123!"
  }'
```

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "token_type": "bearer",
  "user": {...}
}
```

### 2. Use access token:
```bash
curl -X GET https://bizeng-server.fly.dev/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Test token refresh:
```bash
curl -X POST https://bizeng-server.fly.dev/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "YOUR_REFRESH_TOKEN"
  }'
```

**Expected:**
```json
{
  "access_token": "NEW_ACCESS_TOKEN",
  "refresh_token": "NEW_REFRESH_TOKEN",
  "token_type": "bearer"
}
```

### 4. Test with expired token:
```bash
# Use an old/invalid token
curl -X GET https://bizeng-server.fly.dev/me \
  -H "Authorization: Bearer EXPIRED_TOKEN"
```

**Expected:**
```json
{
  "detail": "Could not validate credentials"
}
```
**Status:** 401 Unauthorized

---

## 🎯 QUICK TEST (No 31-Minute Wait)

If you want to test without waiting 31 minutes, you can:

### Option 1: Modify Server Token Expiry (Development Only)
Edit server config to expire tokens faster:
```python
# server/config.py
ACCESS_TOKEN_EXPIRE_MINUTES = 1  # Instead of 30
```

### Option 2: Manually Trigger 401
Modify AuthenticatedClientProvider temporarily to always treat first request as 401:
```kotlin
// For testing only!
if (response.status == HttpStatusCode.OK && !testRefreshTriggered) {
    testRefreshTriggered = true
    // Pretend it's 401
    response = mockUnauthorizedResponse()
}
```

### Option 3: Use Debugging Tools
1. Login
2. Get access token from logs
3. Use JWT decoder (jwt.io) to see expiry
4. Wait until expiry time
5. Make request

---

## ✅ SUCCESS INDICATORS

Your token refresh is working correctly if:

1. ✅ Users can stay logged in for 45 days (refresh token lifespan)
2. ✅ Users don't get logged out after 30 minutes of inactivity
3. ✅ No "401 Unauthorized" errors visible to users
4. ✅ Logs show automatic refresh happening
5. ✅ Only one refresh call per token expiry (not multiple)
6. ✅ Invalid refresh tokens trigger clean logout

---

## 📊 EXPECTED PRODUCTION BEHAVIOR

In production, users should experience:

### Day 1:
- User logs in → Gets tokens
- Uses app → Access token added to all requests
- Everything works

### After 30 Minutes:
- Access token expires
- User makes request → Gets 401
- **INVISIBLE TO USER:** Token auto-refreshes
- Request retries automatically
- User sees normal response

### After 45 Days:
- Refresh token expires
- Auto-refresh fails
- User is logged out (expected behavior)
- User logs in again → Gets new tokens

---

## 🎉 FINAL VERIFICATION

Run through this real-world scenario:

1. **Morning:** User logs in, uses app
2. **Lunch Break (31+ min later):** User opens app, uses features
   - ✅ Should work without login
   - ✅ Logs show token refresh
3. **Evening (6+ hours later):** User opens app again
   - ✅ Should work without login
   - ✅ Multiple refreshes may have occurred
4. **Next Day:** User opens app
   - ✅ Should work without login
   - ✅ Fresh token refresh on first use
5. **After 45 Days:** User opens app
   - ✅ Gets logged out (refresh token expired)
   - ✅ User logs in again (normal behavior)

---

**Token Refresh Implementation:** ✅ READY FOR TESTING  
**Recommended Test:** Wait 31 minutes and use the app (TEST 3)  
**Alternative:** Temporarily reduce token expiry time on server

---

**Created:** November 12, 2025  
**Status:** Ready for manual testing

