# ✅ WiFi Configuration Complete!

## What I've Done:

1. **Updated NetworkModule.kt** to use your WiFi IP:
   - Changed from: `https://colette-unvoluble-nonsynoptically.ngrok-free.dev` (expired)
   - Changed to: `http://192.168.1.60:8020` (your local WiFi)
   
   File: `android/app/src/main/java/com/example/myapplication/di/NetworkModule.kt`

## What You Need to Do:

### ⚠️ STEP 1: Add Firewall Rule (AS ADMINISTRATOR)

**Right-click `ADD_FIREWALL_RULE.bat` → Run as Administrator**

Or manually run in Administrator CMD:
```cmd
netsh advfirewall firewall add rule name="Python Server 8020" dir=in action=allow protocol=TCP localport=8020
```

This allows your Android device to connect to port 8020 on your computer.

---

### 🐍 STEP 2: Start Python Server

Your Python server must be running and accessible on your WiFi network.

**Important**: Server must bind to `0.0.0.0:8020` (not `localhost` or `127.0.0.1`)

Example if using Flask:
```python
app.run(host='0.0.0.0', port=8020)
```

Example if using FastAPI/Uvicorn:
```python
uvicorn.run(app, host='0.0.0.0', port=8020)
```

Look for server file in: `C:\Users\sanja\rag-biz-english\`

---

### 📱 STEP 3: Rebuild the APK

Since we changed the network configuration, you MUST rebuild:

```cmd
cd C:\Users\sanja\rag-biz-english\android
gradlew.bat assembleDebug
```

The new APK will be at:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

---

### 🧪 STEP 4: Test Connection (Before Installing APK)

Test from your computer that the server is accessible:

```cmd
curl http://192.168.1.60:8020/health
```

Should return something like: `{"status": "healthy"}`

If this doesn't work, the Android app won't work either!

---

### 📲 STEP 5: Install New APK

1. Transfer the new APK to your Android device
2. Install it (uninstall old version first if needed)
3. Make sure both devices are on the SAME WiFi network
4. Open the app and test

---

## Testing the App

### Check Logs (while app is running):
```cmd
adb logcat | findstr "NETWORK"
```

Look for:
- `NETWORK_CONFIG` - Shows what URL the app is using
- `NETWORK_TEST` - Shows connection test results
- Should see: "Server connection successful!"

---

## Troubleshooting

### Problem: curl to 192.168.1.60:8020 fails

**Solutions:**
1. Check firewall rule added (run `ADD_FIREWALL_RULE.bat` as Administrator)
2. Check Python server is running
3. Check server is binding to `0.0.0.0` (not `localhost`)
4. First test `curl http://localhost:8020/health` - if this fails, server isn't running

### Problem: curl works but app doesn't connect

**Solutions:**
1. Did you rebuild APK after changing NetworkModule.kt?
2. Did you install the NEW APK on device?
3. Are both devices on the SAME WiFi network?
4. Check app logs: `adb logcat | findstr "NETWORK"`

### Problem: "Connection refused" in app

**Solutions:**
1. Firewall is blocking - add rule as Administrator
2. Wrong IP address - verify with `ipconfig`
3. Server not running or bound to wrong interface

### Problem: WiFi IP changed

Your IP (192.168.1.60) might change if you restart router or reconnect to WiFi.

If IP changes:
1. Run `ipconfig` to get new IP
2. Update `NetworkModule.kt` line 59 with new IP
3. Rebuild APK: `gradlew.bat assembleDebug`
4. Reinstall APK on device

---

## Quick Start Commands

```cmd
# 1. Add firewall (as Administrator)
ADD_FIREWALL_RULE.bat

# 2. Start Python server (in another terminal)
cd C:\Users\sanja\rag-biz-english
python app.py

# 3. Test connection
curl http://192.168.1.60:8020/health

# 4. Rebuild APK
cd android
gradlew.bat assembleDebug

# 5. Check app logs
adb logcat | findstr "NETWORK"
```

---

## Why This is Better Than ngrok

✅ **Stable** - No expiration, works as long as both on same WiFi
✅ **Fast** - Direct connection, no internet middleman
✅ **Free** - No ngrok limits or account needed
✅ **Private** - Only accessible on your local network

❌ Only works when both devices on same WiFi
❌ IP might change (but rare, and easy to fix)

---

## Summary

Your app is now configured to connect to:
- **URL**: `http://192.168.1.60:8020`
- **Mode**: Local WiFi (Production)
- **Requirement**: Both devices on same WiFi network

**Next steps:**
1. Run `ADD_FIREWALL_RULE.bat` as Administrator
2. Start Python server (on 0.0.0.0:8020)
3. Test: `curl http://192.168.1.60:8020/health`
4. Rebuild: `gradlew.bat assembleDebug`
5. Install new APK on device
6. Test the app!

