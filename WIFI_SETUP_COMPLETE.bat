@echo off
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║    WIFI SETUP COMPLETE - FINAL STEPS TO GET APP WORKING  ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo ✓ NetworkModule.kt updated to use: 192.168.1.60:8020
echo.
echo ═══════════════════════════════════════════════════════════
echo WHAT YOU NEED TO DO NOW:
echo ═══════════════════════════════════════════════════════════
echo.
echo [STEP 1] Add Windows Firewall Rule (REQUIRED)
echo ────────────────────────────────────────────────────────────
echo Run this command in Administrator PowerShell/CMD:
echo.
echo   netsh advfirewall firewall add rule name="Python Server 8020" dir=in action=allow protocol=TCP localport=8020
echo.
echo Or just run: fix-firewall-NOW.bat (as Administrator)
echo.
echo.
echo [STEP 2] Start Python Server
echo ────────────────────────────────────────────────────────────
echo The Python server MUST be running and listening on 0.0.0.0:8020
echo.
echo Option A - If you have app.py in the parent folder:
echo   cd C:\Users\sanja\rag-biz-english
echo   python app.py
echo.
echo Option B - If you have a different server file:
echo   Find your Python server file and run it
echo   Make sure it shows: "Running on http://0.0.0.0:8020"
echo.
echo IMPORTANT: Server must bind to 0.0.0.0 (not 127.0.0.1)
echo Example in Python:
echo   app.run(host='0.0.0.0', port=8020)
echo.
echo.
echo [STEP 3] Rebuild the Android APK
echo ────────────────────────────────────────────────────────────
echo Since we changed NetworkModule.kt, you MUST rebuild:
echo.
echo   gradlew.bat assembleDebug
echo.
echo Then reinstall the APK on your device.
echo.
echo.
echo [STEP 4] Test Connection
echo ────────────────────────────────────────────────────────────
echo Before installing APK, verify server is accessible:
echo.
echo Test from this computer:
echo   curl http://192.168.1.60:8020/health
echo.
echo Should return: {"status": "healthy"} or similar
echo.
echo.
echo ═══════════════════════════════════════════════════════════
echo QUICK CHECKLIST:
echo ═══════════════════════════════════════════════════════════
echo.
echo [ ] Firewall rule added for port 8020
echo [ ] Python server running on 0.0.0.0:8020
echo [ ] curl http://192.168.1.60:8020/health works
echo [ ] Both laptop and phone on same WiFi network
echo [ ] APK rebuilt with: gradlew.bat assembleDebug
echo [ ] New APK installed on device
echo.
echo ═══════════════════════════════════════════════════════════
echo TROUBLESHOOTING:
echo ═══════════════════════════════════════════════════════════
echo.
echo If curl to 192.168.1.60:8020 doesn't work:
echo   - Check firewall rule is added (as Administrator!)
echo   - Check Python server is running
echo   - Check server binds to 0.0.0.0 (not localhost)
echo   - Try: curl http://localhost:8020/health first
echo.
echo If app still can't connect:
echo   - Make sure you rebuilt APK after changing NetworkModule.kt
echo   - Make sure new APK is installed
echo   - Check logcat: adb logcat ^| findstr "NETWORK"
echo   - Verify both devices on SAME WiFi network
echo.
echo ═══════════════════════════════════════════════════════════
echo.
pause

