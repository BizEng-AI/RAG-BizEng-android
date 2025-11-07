# CHAT 500 ERROR - COMPLETE FIX GUIDE

## 🔍 Problem Analysis

The `/chat` endpoint is throwing 500 errors. Based on investigation:

### ✅ Android Side is CORRECT
```kotlin
// app/src/main/java/com/example/myapplication/data/remote/dto/ChatMsgDto.kt
data class ChatMsgDto(val role: String, val content: String)

// ChatVm.kt - Sending correctly:
ChatMsgDto(role = "user", content = text)  // ✓ CORRECT
```

### ❌ Server Side Likely Missing Validation

The server needs:
1. Proper Pydantic models for request/response
2. Role validation (only "user", "assistant", "system")
3. Better error handling and logging

---

## 🔧 COMPLETE SERVER FIX

### Step 1: Add These Models to Your Server (app.py or main.py)

Add this near the top of your server file, after imports:

```python
from pydantic import BaseModel, validator
from typing import List, Optional

class ChatMsgDto(BaseModel):
    """Single chat message - matches Android exactly"""
    role: str
    content: str
    
    @validator('role')
    def validate_role(cls, v):
        valid_roles = ['user', 'assistant', 'system']
        if v not in valid_roles:
            raise ValueError(f'Invalid role: {v}. Must be one of: {valid_roles}')
        return v

class ChatReqDto(BaseModel):
    """Chat request from Android"""
    messages: List[ChatMsgDto]
    k: int = 6
    maxContextChars: int = 6000
    unit: Optional[str] = None

class ChatRespDto(BaseModel):
    """Chat response to Android"""
    answer: str
    sources: List[str] = []
```

### Step 2: Replace Your `/chat` Endpoint

**For Azure OpenAI (Recommended):**

```python
@app.post("/chat", response_model=ChatRespDto)
async def chat(req: ChatReqDto):
    """
    Free chat mode - conversational AI without RAG grounding
    Uses Azure OpenAI for natural business English learning assistance
    """
    try:
        print(f"[/chat] Received {len(req.messages)} messages", flush=True)
        
        # Convert to OpenAI format
        messages = [{"role": msg.role, "content": msg.content} for msg in req.messages]
        
        # Add system message if not present
        if not messages or messages[0].get("role") != "system":
            messages.insert(0, {
                "role": "system",
                "content": (
                    "You are a helpful business English learning assistant. "
                    "Help students improve their business communication skills, "
                    "explain business vocabulary and phrases, provide examples of professional language, "
                    "and answer questions about business English. "
                    "Be encouraging, clear, and educational in your responses."
                )
            })
        
        print(f"[/chat] Calling Azure OpenAI with {len(messages)} messages", flush=True)
        
        # Call Azure OpenAI (use the same client as your roleplay endpoints)
        response = oai.chat.completions.create(
            model=AZURE_OPENAI_CHAT_DEPLOYMENT,  # Your Azure deployment name
            messages=messages,
            temperature=0.7,
            max_tokens=500
        )
        
        ai_message = response.choices[0].message.content
        print(f"[/chat] ✓ Response generated: {ai_message[:100]}...", flush=True)
        
        return ChatRespDto(answer=ai_message, sources=[])
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"[/chat] ERROR: {e}", flush=True)
        raise HTTPException(status_code=500, detail=f"Chat failed: {str(e)}")
```

### Step 3: Verify Azure Configuration

Make sure these are set in your server file:

```python
# At the top of your file
import os
from openai import AzureOpenAI

# Azure OpenAI Configuration
AZURE_OPENAI_KEY = os.getenv("AZURE_OPENAI_KEY")
AZURE_OPENAI_ENDPOINT = os.getenv("AZURE_OPENAI_ENDPOINT")
AZURE_OPENAI_API_VERSION = "2024-02-15-preview"
AZURE_OPENAI_CHAT_DEPLOYMENT = os.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT", "gpt-4o-mini")

# Initialize Azure OpenAI client
oai = AzureOpenAI(
    api_key=AZURE_OPENAI_KEY,
    api_version=AZURE_OPENAI_API_VERSION,
    azure_endpoint=AZURE_OPENAI_ENDPOINT
)
```

---

## 🧪 Testing

### 1. Test the endpoint manually:

```bash
cd C:\Users\sanja\rag-biz-english\android
python test_endpoints.py
```

Look for the chat test results. If it fails, check the server terminal for error details.

### 2. Check Server Logs

When you run the app and it hits the chat endpoint, your server should print:
```
[/chat] Received 2 messages
[/chat] Calling Azure OpenAI with 3 messages
[/chat] ✓ Response generated: Hello! I'm here to help...
```

If you see an error instead, the logs will tell you exactly what's wrong.

---

## 🚨 Common 500 Error Causes & Fixes

### Error: "Invalid role: person"
**Cause:** Android is sending wrong role value  
**Fix:** Already fixed - Android uses "user" ✓

### Error: "Model not found" or "Deployment not found"
**Cause:** Wrong Azure deployment name  
**Fix:** Check `AZURE_OPENAI_CHAT_DEPLOYMENT` env variable matches your Azure deployment

### Error: "Unauthorized" or "Invalid API key"
**Cause:** Missing or wrong Azure API key  
**Fix:** Check `AZURE_OPENAI_KEY` in your environment variables

### Error: "'ChatReqDto' is not defined"
**Cause:** Missing Pydantic models in server  
**Fix:** Add the models from Step 1 above

### Error: "'oai' is not defined"
**Cause:** Azure OpenAI client not initialized  
**Fix:** Add the Azure client initialization from Step 3

---

## 📋 Quick Checklist

- [ ] Added `ChatMsgDto`, `ChatReqDto`, `ChatRespDto` models to server
- [ ] Replaced `/chat` endpoint with fixed version
- [ ] Azure OpenAI client is initialized (`oai` variable)
- [ ] Environment variables are set (AZURE_OPENAI_KEY, AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_CHAT_DEPLOYMENT)
- [ ] Server restarted after changes
- [ ] Tested with `python test_endpoints.py`
- [ ] Checked server logs show no errors

---

## 🎯 Expected Behavior

**Android sends:**
```json
{
  "messages": [
    {"role": "user", "content": "What is business English?"}
  ]
}
```

**Server responds:**
```json
{
  "answer": "Business English is a specialized form of English used in professional...",
  "sources": []
}
```

**Server logs:**
```
[/chat] Received 1 messages
[/chat] Calling Azure OpenAI with 2 messages
[/chat] ✓ Response generated: Business English is a specialized...
```

---

## 📝 Where to Find Your Server File

Your main server file should be at:
- `C:\Users\sanja\rag-biz-english\app.py` OR
- `C:\Users\sanja\rag-biz-english\main.py` OR
- `C:\Users\sanja\rag-biz-english\server.py`

Add the fixes to whichever file you use to run the server with uvicorn.

---

## 🆘 Still Getting 500 Errors?

1. **Check the server terminal** - The full error and stack trace will be there
2. **Run the test script** - `python test_endpoints.py` shows the exact error
3. **Share the logs** - Copy the error from server terminal for specific help

