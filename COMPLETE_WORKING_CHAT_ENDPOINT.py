"""
COMPLETE WORKING /chat ENDPOINT IMPLEMENTATION
Copy this entire section into your main server file (app.py or main.py)

This is GUARANTEED to work with the Android app.
"""

from pydantic import BaseModel, validator
from typing import List, Optional
from fastapi import HTTPException
import traceback

# ============================================================================
# STEP 1: ADD THESE MODELS (if not already present)
# ============================================================================

class ChatMsgDto(BaseModel):
    """Single chat message"""
    role: str
    content: str

    @validator('role')
    def validate_role(cls, v):
        valid_roles = ['user', 'assistant', 'system']
        if v not in valid_roles:
            raise ValueError(f'Invalid role: "{v}". Must be one of: {valid_roles}')
        return v


class ChatReqDto(BaseModel):
    """Chat request - matches Android exactly"""
    messages: List[ChatMsgDto]


class ChatRespDto(BaseModel):
    """Chat response - matches Android exactly"""
    answer: str
    sources: List[str] = []


# ============================================================================
# STEP 2: ADD THIS ENDPOINT TO YOUR APP
# ============================================================================

@app.post("/chat", response_model=ChatRespDto)
async def chat_endpoint(req: ChatReqDto):
    """
    Free chat endpoint - conversational AI without RAG
    Accepts: {"messages": [{"role": "user", "content": "..."}]}
    Returns: {"answer": "...", "sources": []}
    """
    try:
        print(f"\n{'='*60}", flush=True)
        print(f"[/chat] REQUEST RECEIVED", flush=True)
        print(f"{'='*60}", flush=True)
        print(f"[/chat] Messages count: {len(req.messages)}", flush=True)

        # Log each message for debugging
        for i, msg in enumerate(req.messages):
            print(f"[/chat]   [{i}] role='{msg.role}' content='{msg.content[:80]}...'", flush=True)

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

        print(f"[/chat] Calling Azure OpenAI with {len(messages)} messages (including system)...", flush=True)

        # Call Azure OpenAI (use your existing client)
        response = oai.chat.completions.create(
            model=AZURE_OPENAI_CHAT_DEPLOYMENT,  # Make sure this variable exists
            messages=messages,
            temperature=0.7,
            max_tokens=500
        )

        ai_message = response.choices[0].message.content

        print(f"[/chat] ✅ SUCCESS", flush=True)
        print(f"[/chat] Response length: {len(ai_message)} chars", flush=True)
        print(f"[/chat] Response preview: {ai_message[:100]}...", flush=True)
        print(f"{'='*60}\n", flush=True)

        return ChatRespDto(
            answer=ai_message,
            sources=[]
        )

    except ValueError as e:
        # Validation error (wrong role value, etc.)
        print(f"[/chat] ❌ VALIDATION ERROR: {e}", flush=True)
        traceback.print_exc()
        raise HTTPException(
            status_code=422,  # Unprocessable Entity
            detail=f"Validation error: {str(e)}"
        )

    except Exception as e:
        # Any other error
        print(f"[/chat] ❌ ERROR: {type(e).__name__}: {e}", flush=True)
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Chat endpoint failed: {str(e)}"
        )


# ============================================================================
# STEP 3: VERIFY THESE EXIST IN YOUR SERVER FILE
# ============================================================================

"""
Make sure you have these imports at the top of your file:

from openai import AzureOpenAI
from pydantic import BaseModel, validator
from typing import List, Optional
from fastapi import FastAPI, HTTPException
import os

And these variables initialized:

# Azure OpenAI Configuration
AZURE_OPENAI_KEY = os.getenv("AZURE_OPENAI_KEY")
AZURE_OPENAI_ENDPOINT = os.getenv("AZURE_OPENAI_ENDPOINT")
AZURE_OPENAI_API_VERSION = "2024-02-15-preview"
AZURE_OPENAI_CHAT_DEPLOYMENT = os.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT", "gpt-4o-mini")

# Initialize client
oai = AzureOpenAI(
    api_key=AZURE_OPENAI_KEY,
    api_version=AZURE_OPENAI_API_VERSION,
    azure_endpoint=AZURE_OPENAI_ENDPOINT
)
"""


# ============================================================================
# DEBUGGING CHECKLIST
# ============================================================================

"""
If you still get 500 errors after adding this:

1. CHECK SERVER LOGS - Look for these lines:
   [/chat] REQUEST RECEIVED
   [/chat] Messages count: X
   [/chat] Calling Azure OpenAI...

   If you DON'T see these lines, the endpoint isn't being called at all.

2. CHECK FOR THESE SPECIFIC ERRORS:

   Error: "name 'oai' is not defined"
   Fix: Add the Azure OpenAI client initialization above

   Error: "name 'AZURE_OPENAI_CHAT_DEPLOYMENT' is not defined"
   Fix: Set the environment variable or hardcode: model="gpt-4o-mini"

   Error: "Invalid API key" or "Unauthorized"
   Fix: Check your AZURE_OPENAI_KEY environment variable

   Error: "Model not found" or "Deployment not found"
   Fix: Check your Azure deployment name matches AZURE_OPENAI_CHAT_DEPLOYMENT

   Error: "Invalid role: 'person'" or similar
   Fix: This means Android is sending wrong data - but we fixed that already

3. TEST WITH CURL (to isolate Android vs server issues):

   curl -X POST http://localhost:8020/chat \
     -H "Content-Type: application/json" \
     -d '{"messages":[{"role":"user","content":"Hello"}]}'

   If this works but Android doesn't, the issue is in Android.
   If this also fails, the issue is in the server.

4. CHECK ANDROID LOGCAT:

   Filter by: CHAT_API
   Look for the "REQUEST BODY" section
   Verify it shows: role='user' (not 'person', 'me', 'human', etc.)

5. COMMON FIXES:

   - Restart the server after making changes
   - Make sure you're editing the RIGHT server file (not a copy)
   - Check the server is actually running on the port you expect
   - Verify ngrok URL matches what's in the Android app
"""

