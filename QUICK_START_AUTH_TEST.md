# 🚀 QUICK START - TEST AUTHENTICATION NOW

## Step 1: Build the App (2 minutes)

Open terminal in Android Studio or CMD:

```cmd
cd C:\Users\sanja\rag-biz-english\android
gradlew.bat assembleDebug
```

Wait for build to complete...

## Step 2: Install on Device (1 minute)

### Option A: Android Emulator
1. Start emulator in Android Studio
2. Run: `gradlew.bat installDebug`

### Option B: Physical Device  
1. Enable USB Debugging on your phone
2. Connect via USB
3. Run: `gradlew.bat installDebug`

## Step 3: Test Authentication Flow

### First Time User Journey:

1. **Open App** 
   - You should see: **Login Screen** 📱
   - If you see the main app instead, authentication is already working!

2. **Click "Don't have an account? Register"**
   - Fill in:
     - Display Name: `John Doe`
     - Email: `john@test.com`
     - Password: `test123456`
     - Confirm Password: `test123456`
     - Group Number: `Group A` (optional)
   - Click **Register**

3. **After Successful Registration**
   - You should be automatically logged in
   - Main app appears with bottom navigation
   - Top bar shows "BizEng" and your name
   - Logout button (arrow icon) in top-right

4. **Test Logout**
   - Click logout button in top bar
   - Should return to Login Screen

5. **Test Login**
   - Enter email: `john@test.com`
   - Enter password: `test123456`
   - Click **Login**
   - Should return to main app

6. **Test Remember Me**
   - Force close the app (swipe away)
   - Open app again
   - Should go directly to main app (no login needed)
   - This proves token storage is working!

---

## 🎯 What to Check

### ✅ Login Screen
- [ ] Email input works
- [ ] Password input works
- [ ] Password visibility toggle works
- [ ] "Login" button disabled when fields empty
- [ ] Loading spinner shows during login
- [ ] Error message shows on wrong credentials
- [ ] "Register" link works

### ✅ Register Screen
- [ ] All fields work (name, email, password, confirm, group)
- [ ] Password validation (min 6 chars)
- [ ] Confirm password validation (must match)
- [ ] Loading spinner shows during registration
- [ ] Error message shows on failure
- [ ] "Login" link works

### ✅ Main App
- [ ] User name shows in top bar
- [ ] Logout button present
- [ ] Bottom navigation works (Chat, Roleplay, Pronunciation)
- [ ] All features work as before

### ✅ Token Persistence
- [ ] After login, close app completely
- [ ] Reopen app - should NOT show login screen
- [ ] This means tokens are saved and loaded correctly!

---

## 🐛 Common Issues

### Issue: "Cannot connect to server"
**Solution:** Check server URL in `NetworkModule.kt`:
```kotlin
val PRODUCTION_SERVER_IP = "bizeng-server.fly.dev"
val USE_HTTPS = true
```

### Issue: "Registration failed"
**Possible causes:**
1. Server not running - Check: `https://bizeng-server.fly.dev/health`
2. Email already exists - Use different email
3. Password too short - Min 6 characters

### Issue: App crashes on startup
**Solution:** 
1. Check logcat for errors: `adb logcat | findstr "ERROR"`
2. Make sure all new files compiled correctly
3. Try: `gradlew clean assembleDebug`

### Issue: Build fails
**Solution:**
```cmd
gradlew clean
gradlew assembleDebug
```

---

## 📱 Quick Test Script

Run these commands in order:

```cmd
cd C:\Users\sanja\rag-biz-english\android

# Build
gradlew.bat clean assembleDebug

# Install (make sure device/emulator is connected)
gradlew.bat installDebug

# Check logs while testing
adb logcat -s "AUTH" "NETWORK_CONFIG"
```

---

## 🎉 Success Criteria

Authentication is working if you can:
1. ✅ Register a new account
2. ✅ Login with credentials
3. ✅ See your name in the app
4. ✅ Logout
5. ✅ Login again
6. ✅ Reopen app without needing to login

---

## 📞 Next Steps After Testing

Once authentication works:

### Phase 1 Complete ✅
- Users can register
- Users can login
- Tokens are stored securely
- All API requests include auth token

### Phase 2: Add Tracking (Optional)
Add exercise tracking to Chat, Roleplay, and Pronunciation screens.

### Phase 3: Admin Dashboard (Optional)
Create a screen for teachers to view student progress.

---

**Ready to test? Build and install the app now!** 🚀

```cmd
cd C:\Users\sanja\rag-biz-english\android
gradlew.bat assembleDebug
gradlew.bat installDebug
```

