@echo off
echo ═══════════════════════════════════════════════════════════
echo Adding Windows Firewall Rule for Port 8020
echo ═══════════════════════════════════════════════════════════
echo.
echo This must be run as Administrator!
echo.

REM Check if running as admin
net session >nul 2>&1
if %errorLevel% NEQ 0 (
    echo ✗ ERROR: Not running as Administrator!
    echo.
    echo Right-click this file and select "Run as Administrator"
    echo.
    pause
    exit /b 1
)

echo Adding firewall rule...
netsh advfirewall firewall add rule name="Python Server 8020" dir=in action=allow protocol=TCP localport=8020

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ SUCCESS! Firewall rule added.
    echo.
    echo Port 8020 is now allowed through Windows Firewall.
    echo Your Android device can now connect to this computer on port 8020.
) else (
    echo.
    echo ✗ FAILED to add firewall rule.
    echo.
    echo Error code: %ERRORLEVEL%
)

echo.
echo ═══════════════════════════════════════════════════════════
echo.
pause

