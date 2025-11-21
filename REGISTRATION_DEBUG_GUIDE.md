# 🔍 REGISTRATION ERROR - ENHANCED DEBUGGING

**Status:** Added comprehensive logging to identify exact error location  
**Next Step:** Test registration in app and check logs

---

## 📝 WHAT I ADDED

### Enhanced Logging in 3 Places:

#### 1. AuthApi.register()
```kotlin
✅ Added detailed logging:
   - Raw server response
   - Explicit TokenResponse parsing
   - Detailed serialization error messages
   - Stack trace on errors
   - Validation checks
```

#### 2. AuthApi.getProfile()
```kotlin
✅ Added detailed logging:
   - Raw profile response
   - Explicit ProfileDto parsing
   - Detailed serialization error messages
   - Stack trace on errors
   - Field-by-field validation
```

#### 3. AuthRepository.register()
```kotlin
✅ Added step-by-step logging:
   - STEP 1: Call authApi.register()
   - STEP 2: Validate token response
   - STEP 3: Save tokens
   - STEP 4: Get user info (response vs /me)
   - Success/failure at each step
```

---

## 🎯 HOW TO USE THIS

### 1. Build and Install App
```bash
cd c:\Users\sanja\rag-biz-english\android
gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Clear Logcat
```bash
adb logcat -c
```

### 3. Start Logcat Monitoring
```bash
adb logcat | grep -E "AuthRepository|AuthApi"
```

Or in Android Studio:
- Open Logcat tab
- Filter by: `AuthRepository|AuthApi`

### 4. Try Registration in App
- Open app
- Fill registration form
- Click "Register"
- **Watch the logs carefully**

---

## 📊 WHAT TO LOOK FOR IN LOGS

### If Error Occurs During Token Parsing:
```
🔐 AuthApi: 📤 Registering user: test@example.com
🔐 AuthApi: 📥 RAW SERVER RESPONSE: {...}
🔐 AuthApi: 🔍 Attempting to parse as TokenResponse...
🔐 AuthApi: ❌ SERIALIZATION ERROR: ...
🔐 AuthApi: ❌ Error type: ...
🔐 AuthApi: ❌ Raw response that failed to parse: ...
```

**This means:** TokenResponse DTO doesn't match server response

### If Error Occurs During Profile Fetch:
```
AuthRepository: 🔵 STEP 4: Getting user info...
AuthRepository:    ℹ️  No user data in token response, fetching from /me...
AuthRepository:    🔵 Calling authApi.getProfile()...
🔐 AuthApi: 📤 Fetching user profile from /me
🔐 AuthApi: 📥 RAW PROFILE RESPONSE: {...}
🔐 AuthApi: 🔍 Attempting to parse as ProfileDto...
🔐 AuthApi: ❌ SERIALIZATION ERROR parsing ProfileDto:
🔐 AuthApi: ❌ Error message: Fields [id, email, roles, created_at] are required...
```

**This means:** ProfileDto DTO doesn't match /me response OR tokens not saved correctly

---

## 🔍 POSSIBLE CAUSES & FIXES

### Cause 1: TokenResponse Missing Fields
**Symptom:** Error during `authApi.register()` parsing

**Check logs for:**
```
❌ Raw response that failed to parse: {...}
```

**Fix:** Update `TokenResponse` DTO to match server response

---

### Cause 2: ProfileDto Missing Fields
**Symptom:** Error during `authApi.getProfile()` parsing

**Check logs for:**
```
❌ Raw response that failed: {...}
❌ Fields [id, email, roles, created_at] are required
```

**Possible Issues:**
1. **Server not returning fields** → Check server logs
2. **Field names don't match** → Check @SerialName annotations
3. **Wrong data types** → Check server returns Int for id, List for roles
4. **Auth header not set** → Check if tokens saved before getProfile call

**Fix:** Check what server actually returns:
```bash
# Get token first
TOKEN=$(curl -X POST https://bizeng-server.fly.dev/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"debug@test.com","password":"Test123!","display_name":"Debug"}' \
  | jq -r '.access_token')

# Call /me
curl -X GET https://bizeng-server.fly.dev/me \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

### Cause 3: Tokens Not Saved Before getProfile()
**Symptom:** 401 error when calling `/me`

**Check logs for:**
```
AuthRepository: 🔵 STEP 3: Saving tokens to AuthManager...
AuthRepository:    ✅ Tokens saved successfully
AuthRepository:    🔵 Calling authApi.getProfile()...
🔐 AuthApi: 📥 HTTP Status: 401
```

**Fix:** Tokens ARE being saved before getProfile(), so this isn't the issue

---

### Cause 4: BasicClient Has Auth Headers
**Symptom:** `/auth/register` gets invalid auth header

**Check:** BasicClient shouldn't add Authorization headers
**Fix:** Already using BasicClient for AuthApi (no auth headers)

---

## 🎯 DEBUGGING CHECKLIST

When you test registration, capture these from logs:

- [ ] What is the RAW SERVER RESPONSE from `/auth/register`?
- [ ] Does it parse successfully as TokenResponse?
- [ ] Are tokens saved to AuthManager?
- [ ] Is getProfile() being called?
- [ ] What is the RAW PROFILE RESPONSE from `/me`?
- [ ] Does it parse successfully as ProfileDto?
- [ ] What is the EXACT error message?
- [ ] What is the EXACT stack trace?

---

## 📤 SEND ME THIS INFO

After you test registration and it fails, send me:

1. **Full log output** from registration attempt
2. **The exact error message**
3. **The raw JSON responses** (from logs)

With that info, I can identify the exact mismatch and fix it.

---

## 🔧 QUICK FIX ATTEMPTS

If the error is still "ProfileDto fields missing", try these:

### Option 1: Make ALL ProfileDto Fields Optional
```kotlin
@Serializable
data class ProfileDto(
    val id: Int? = null,  // Make optional
    val email: String? = null,  // Make optional
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("group_number") val groupNumber: String? = null,
    val roles: List<String>? = null,  // Make optional
    @SerialName("created_at") val createdAt: String? = null
)
```

### Option 2: Use Different JSON Config
```kotlin
val json = Json { 
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    prettyPrint = false
    encodeDefaults = false
}
```

### Option 3: Manual Parsing
```kotlin
suspend fun getProfile(): ProfileDto {
    val response = client.get("$baseUrl/me")
    val rawBody = response.bodyAsText()
    
    // Parse JSON manually
    val jsonObject = JSONObject(rawBody)
    return ProfileDto(
        id = jsonObject.optInt("id"),
        email = jsonObject.optString("email"),
        displayName = jsonObject.optString("display_name"),
        groupNumber = jsonObject.optString("group_number"),
        roles = jsonObject.optJSONArray("roles")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        } ?: emptyList(),
        createdAt = jsonObject.optString("created_at")
    )
}
```

---

## ✅ NEXT STEPS

1. **Build APK** with enhanced logging
2. **Test registration** in app
3. **Capture full logs**
4. **Send me the logs** with:
   - Raw server responses
   - Exact error messages
   - Stack traces
5. **I'll identify the exact issue** and provide a targeted fix

---

**Status:** Comprehensive logging added  
**Action:** Test registration and capture logs  
**Expected:** Logs will reveal exact serialization issue


