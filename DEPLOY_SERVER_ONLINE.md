# ============================================================
# DEPLOYING SERVER FOR PUBLIC APK ACCESS
# How to make your RAG Business English app work outside your network
# ============================================================

## 🎯 THE PROBLEM:
Your app currently connects to:
- http://10.177.165.92:8020 (your local hotspot IP)
- This ONLY works when both devices are on the same Wi-Fi network
- Users outside your network cannot access your server

## ✅ THE SOLUTION:
You need to deploy your Python server to a cloud platform that provides a public URL.

# ============================================================
# OPTION 1: NGROK (FASTEST - 5 MINUTES SETUP)
# ============================================================
# Best for: Testing, demos, temporary sharing
# Cost: FREE (with limitations)
# Setup Time: 5 minutes

## STEPS:

1. Download ngrok:
   - Go to: https://ngrok.com/download
   - Create a free account
   - Download ngrok for Windows

2. Install and authenticate:
   ```cmd
   ngrok authtoken YOUR_AUTH_TOKEN_FROM_DASHBOARD
   ```

3. Start your Python server normally:
   ```cmd
   cd [your-python-server-folder]
   python -m uvicorn app:app --host 0.0.0.0 --port 8020 --reload
   ```

4. In a NEW terminal, start ngrok:
   ```cmd
   ngrok http 8020
   ```

5. You'll see output like:
   ```
   Forwarding: https://abc123.ngrok.io -> http://localhost:8020
   ```

6. Copy that URL (e.g., https://abc123.ngrok.io)

7. Update your Android app's NetworkModule.kt:
   ```kotlin
   val PRODUCTION_SERVER_IP = "abc123.ngrok.io"  // No http://, no port
   val SERVER_PORT = ""  // Leave empty for ngrok
   
   val serverUrl = if (useLocalhost) {
       "http://localhost:8020"
   } else {
       if (SERVER_PORT.isEmpty()) {
           "https://$PRODUCTION_SERVER_IP"  // ngrok uses HTTPS
       } else {
           "http://$PRODUCTION_SERVER_IP:$SERVER_PORT"
       }
   }
   ```

8. Rebuild APK and distribute!

## ⚠️ NGROK LIMITATIONS (FREE):
- URL changes every time you restart ngrok (need to rebuild APK each time)
- 40 connections/minute limit
- Session expires after 2 hours (need to restart)

## 💡 NGROK PRO ($10/month):
- Fixed custom domain (e.g., mybizapp.ngrok.io)
- No time limits
- More connections
- Worth it if you're sharing with multiple testers


# ============================================================
# OPTION 2: RAILWAY (EASY DEPLOYMENT - 15 MINUTES)
# ============================================================
# Best for: Production, permanent deployment
# Cost: FREE tier available ($5 credit/month), then pay-as-you-go
# Setup Time: 15 minutes

## STEPS:

1. Create account at: https://railway.app

2. Prepare your Python project:
   - Add requirements.txt to your server folder:
     ```
     fastapi
     uvicorn
     openai
     python-multipart
     # Add other dependencies
     ```
   
   - Add Procfile (no extension) to your server folder:
     ```
     web: uvicorn app:app --host 0.0.0.0 --port $PORT
     ```

3. Deploy:
   - Go to Railway dashboard
   - Click "New Project" → "Deploy from GitHub repo"
   - Or: "Deploy from local directory" (if not using git)
   - Select your Python server folder
   - Railway auto-detects Python and deploys

4. Get your URL:
   - After deployment, go to Settings → Generate Domain
   - You'll get: https://your-app-name.up.railway.app

5. Set environment variables:
   - In Railway dashboard → Variables
   - Add: OPENAI_API_KEY=your-key-here
   - Add any other secrets

6. Update Android app's NetworkModule.kt:
   ```kotlin
   val PRODUCTION_SERVER_IP = "your-app-name.up.railway.app"
   val SERVER_PORT = ""
   
   val serverUrl = if (useLocalhost) {
       "http://localhost:8020"
   } else {
       "https://$PRODUCTION_SERVER_IP"  // Railway uses HTTPS
   }
   ```

7. Rebuild APK once - URL never changes!

## ✅ RAILWAY ADVANTAGES:
- Permanent URL (doesn't change)
- Automatic HTTPS
- Auto-restarts if crashes
- Free tier is generous
- Easy to update (just push code)


# ============================================================
# OPTION 3: RENDER (SIMILAR TO RAILWAY)
# ============================================================
# Best for: Production, free tier
# Cost: FREE tier available (with some limitations)
# Setup Time: 15 minutes

## STEPS:

1. Create account at: https://render.com

2. Prepare your project (same as Railway above)

3. Deploy:
   - Dashboard → New → Web Service
   - Connect GitHub or upload code
   - Build Command: `pip install -r requirements.txt`
   - Start Command: `uvicorn app:app --host 0.0.0.0 --port $PORT`

4. Get URL: https://your-app-name.onrender.com

5. Update Android app (same as Railway)

## ⚠️ RENDER FREE TIER:
- Service spins down after 15 minutes of inactivity
- First request after inactivity takes ~30 seconds (cold start)
- Good for testing, not ideal for production


# ============================================================
# OPTION 4: FLY.IO (DEVELOPER FRIENDLY)
# ============================================================
# Best for: Low-latency global deployment
# Cost: FREE tier available
# Setup Time: 20 minutes

Similar to Railway/Render but uses Docker.
Good if you want more control.


# ============================================================
# OPTION 5: HEROKU
# ============================================================
# No longer has a free tier (minimum $7/month)
# Not recommended unless you're already familiar with it


# ============================================================
# RECOMMENDED SETUP FOR YOUR USE CASE:
# ============================================================

## FOR TESTING/DEMOS (Share with a few people for a short time):
→ Use NGROK (Option 1)
   - Fastest to set up
   - Just run ngrok, update app, share APK
   - Free tier is fine for small groups

## FOR PRODUCTION (Permanent app, many users):
→ Use RAILWAY (Option 2)
   - Easy setup
   - Permanent URL (no need to rebuild APK)
   - Good free tier
   - Scales automatically

## MY RECOMMENDATION FOR YOU:
Start with NGROK today to test if everything works, then move to Railway for permanent deployment.


# ============================================================
# STEP-BY-STEP: NGROK QUICK START (5 MIN)
# ============================================================

1. Download ngrok: https://ngrok.com/download

2. Open CMD and authenticate:
   ```cmd
   ngrok config add-authtoken YOUR_TOKEN
   ```

3. Start your Python server:
   ```cmd
   cd C:\path\to\your\python\server
   python -m uvicorn app:app --host 0.0.0.0 --port 8020 --reload
   ```

4. Open NEW CMD window and start ngrok:
   ```cmd
   ngrok http 8020
   ```

5. Copy the HTTPS URL (e.g., https://abc123.ngrok.io)

6. Update NetworkModule.kt in Android Studio:
   - Find line: `val PRODUCTION_SERVER_IP = "10.177.165.92"`
   - Change to: `val PRODUCTION_SERVER_IP = "abc123.ngrok.io"`
   - Find line: `val SERVER_PORT = "8020"`
   - Change to: `val SERVER_PORT = ""`
   - Find line: `"http://$PRODUCTION_SERVER_IP:$SERVER_PORT"`
   - Change to: `if (SERVER_PORT.isEmpty()) "https://$PRODUCTION_SERVER_IP" else "http://$PRODUCTION_SERVER_IP:$SERVER_PORT"`

7. Rebuild APK (Build → Build APK)

8. Share APK with anyone - they can use it from anywhere!

9. Keep both terminals open (Python server + ngrok) while people are using it


# ============================================================
# IMPORTANT NOTES:
# ============================================================

1. SECURITY:
   - Your OpenAI API key will be used by the server
   - Make sure your Python server validates requests if needed
   - Consider adding rate limiting to prevent abuse

2. COSTS:
   - Free tiers are fine for testing
   - OpenAI API costs depend on usage
   - Monitor your usage on OpenAI dashboard

3. TESTING:
   - After deploying, test with your phone using mobile data (not Wi-Fi)
   - This confirms it works outside your network

4. UPDATING:
   - NGROK: URL changes on restart → need to rebuild APK
   - Railway/Render: URL stays same → just update server code, no APK rebuild needed


# ============================================================
# TROUBLESHOOTING:
# ============================================================

Q: App says "Connection refused"
A: Make sure:
   - Server is running
   - Ngrok/Railway is running
   - You updated the URL in NetworkModule.kt
   - You rebuilt the APK after changing the URL

Q: Ngrok URL changed and app stopped working
A: Ngrok free tier gets a new URL each restart
   - Either: Update NetworkModule.kt with new URL and rebuild
   - Or: Upgrade to ngrok Pro for fixed domain

Q: Railway/Render app is slow
A: Free tier spins down after inactivity
   - First request wakes it up (takes 30 sec)
   - Consider upgrading to paid tier for always-on

Q: Getting OpenAI API errors
A: Make sure you set OPENAI_API_KEY environment variable on Railway/Render


# ============================================================
# NEXT STEPS:
# ============================================================

1. Choose your platform (I recommend starting with ngrok)
2. Follow the steps above
3. Update NetworkModule.kt with the public URL
4. Rebuild APK
5. Test with your phone on mobile data
6. Share the APK!

Need help with any specific step? Let me know!

