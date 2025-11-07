# 🔧 SERVER FIX INSTRUCTIONS

## Problem
AI says "I see, please continue" when student uses profanity, instead of addressing it.

## Solution
Replace 2 files on your server.

---

## 📁 FILES TO REPLACE

### File 1: roleplay_engine.py
**Location:** `C:\Users\sanja\rag-biz-english\roleplay_engine.py`
**Replace with:** `FIXED_roleplay_engine.py` (in android folder)

### File 2: roleplay_api.py  
**Location:** `C:\Users\sanja\rag-biz-english\roleplay_api.py`
**Replace with:** `FIXED_roleplay_api.py` (in android folder)

---

## 🚀 Quick Fix Commands

```cmd
cd C:\Users\sanja\rag-biz-english

REM Backup current files
copy roleplay_engine.py roleplay_engine.py.backup
copy roleplay_api.py roleplay_api.py.backup

REM Copy fixed files from android folder
copy android\FIXED_roleplay_engine.py roleplay_engine.py
copy android\FIXED_roleplay_api.py roleplay_api.py

REM Restart server
REM Press Ctrl+C to stop current server, then:
uvicorn app:app --host 0.0.0.0 --port 8020 --reload
```

---

## ✅ What's Fixed

### Before:
- Profanity detected ✅
- Red correction box shows ✅
- AI response: "I see, please continue" ❌ (ignores the error!)

### After:
- Profanity detected ✅
- Red correction box shows ✅
- AI response: "That language isn't appropriate for business. Let's keep this professional." ✅

---

## 🎯 Test It

1. Restart server (commands above)
2. Open app (no need to rebuild!)
3. Start roleplay
4. Type "fuck off"
5. Send

**Expected Result:**

**AI Message (gray):**
```
I understand you may be frustrated, but that language 
isn't appropriate for a business setting. Let's keep 
this professional.
```

**Red Box:**
```
❌ Pragmatic: 'fuck off' → ''
   This language is completely unacceptable...
💡 Priority: high. Keep practicing!
```

---

## 🔍 Key Changes

### roleplay_engine.py
- Added `error_type` parameter to `generate_response()`
- AI uses different prompt when `error_type="pragmatic"`
- Explicitly tells AI: "Student used inappropriate language - address it"

### roleplay_api.py
- Passes `error_type` to engine: `engine.generate_response(..., error_type=error_type)`
- Better logging to see what's happening

---

## 🐛 If Still Not Working

Check server logs for:
```
[roleplay/turn] ⚠️ Error detected:
[roleplay/turn]   Type: pragmatic
[engine] 🚨 Pragmatic error detected - AI will address inappropriate language
[engine] Generated response: I understand you may be frustrated...
```

If you see this, it's working correctly!

---

## 📞 Rollback (if needed)

```cmd
cd C:\Users\sanja\rag-biz-english
copy roleplay_engine.py.backup roleplay_engine.py
copy roleplay_api.py.backup roleplay_api.py
```

Then restart server.

---

**THE FIX IS READY - JUST COPY THE 2 FILES AND RESTART SERVER!**

