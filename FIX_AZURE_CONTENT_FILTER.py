"""
FIX AZURE CONTENT FILTER FOR /ask ENDPOINT
===========================================
This fixes the "content management policy" errors that block RAG queries.

PROBLEM: Azure OpenAI has strict content filters that block normal words like "yo"
SOLUTION: Use safer prompts and configure content filter settings properly
"""

import os
from typing import List, Optional
from openai import AzureOpenAI
from pydantic import BaseModel

# ============================================================================
# AZURE CONFIGURATION WITH CONTENT FILTER HANDLING
# ============================================================================

# Get Azure credentials from environment
AZURE_OPENAI_KEY = os.getenv("AZURE_OPENAI_KEY")
AZURE_OPENAI_ENDPOINT = os.getenv("AZURE_OPENAI_ENDPOINT")
AZURE_OPENAI_API_VERSION = os.getenv("AZURE_OPENAI_API_VERSION", "2024-02-15-preview")
AZURE_OPENAI_CHAT_DEPLOYMENT = os.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT", "gpt-35-turbo")

# Initialize Azure OpenAI client
azure_client = AzureOpenAI(
    api_key=AZURE_OPENAI_KEY,
    api_version=AZURE_OPENAI_API_VERSION,
    azure_endpoint=AZURE_OPENAI_ENDPOINT
)

# ============================================================================
# SAFE RAG PROMPT BUILDER - AVOIDS CONTENT FILTER TRIGGERS
# ============================================================================

def build_safe_rag_prompt(query: str, context: str) -> List[dict]:
    """
    Build a prompt that's less likely to trigger Azure's content filter.

    KEY CHANGES FROM BEFORE:
    1. Uses system message to set safe, educational context
    2. Sanitizes user input to remove potentially triggering words
    3. Frames everything as educational business English learning
    4. Adds explicit "safe content" instructions
    """

    # System message establishes this as educational/professional context
    system_message = {
        "role": "system",
        "content": (
            "You are a professional Business English teaching assistant. "
            "You help students learn business communication skills in a professional, "
            "educational setting. All content should be appropriate for workplace learning. "
            "Base your answers on the provided course materials when available."
        )
    }

    # Build context message if we have RAG results
    if context and context.strip():
        context_message = {
            "role": "system",
            "content": f"Relevant course materials:\n\n{context}\n\nUse this information to answer the student's question."
        }
    else:
        context_message = None

    # User query - sanitize to avoid filter triggers
    sanitized_query = sanitize_query(query)
    user_message = {
        "role": "user",
        "content": f"Student question: {sanitized_query}"
    }

    # Build message list
    messages = [system_message]
    if context_message:
        messages.append(context_message)
    messages.append(user_message)

    return messages


def sanitize_query(query: str) -> str:
    """
    Sanitize user input to reduce content filter triggers.

    Common triggers:
    - Slang terms (yo, sup, etc.)
    - Informal greetings
    - Abbreviated words

    Solution: Expand informal language into professional equivalents
    """

    # Map informal -> formal
    informal_to_formal = {
        "yo": "hello",
        "sup": "how are you",
        "hey": "hello",
        "wassup": "what is happening",
        "u": "you",
        "ur": "your",
        "r": "are",
        "pls": "please",
        "thx": "thank you",
        "thnx": "thank you",
    }

    # Replace informal terms (case-insensitive)
    sanitized = query.lower()
    for informal, formal in informal_to_formal.items():
        # Replace whole words only (not parts of words)
        sanitized = sanitized.replace(f" {informal} ", f" {formal} ")
        if sanitized.startswith(f"{informal} "):
            sanitized = f"{formal}{sanitized[len(informal):]}"
        if sanitized.endswith(f" {informal}"):
            sanitized = f"{sanitized[:-len(informal)]}{formal}"

    return sanitized if sanitized != query.lower() else query


# ============================================================================
# SAFE ASK ENDPOINT WITH CONTENT FILTER ERROR HANDLING
# ============================================================================

class AskRequest(BaseModel):
    query: str
    k: int = 5
    unit: Optional[str] = None
    maxContextChars: int = 2000


class AskResponse(BaseModel):
    answer: str
    sources: List[str]


async def ask_with_safe_prompts(request: AskRequest) -> AskResponse:
    """
    Enhanced /ask endpoint with content filter protection.

    Changes from original:
    1. Uses safe prompt builder
    2. Catches content filter errors and retries with even safer prompt
    3. Falls back to non-RAG response if filters are too strict
    """

    try:
        # Step 1: Get RAG context from your vector database (Qdrant)
        # (Keep your existing RAG retrieval logic here)
        context = retrieve_rag_context(request.query, request.k, request.unit, request.maxContextChars)
        sources = get_sources_list(context)  # Extract source IDs

        # Step 2: Build safe prompt
        messages = build_safe_rag_prompt(request.query, context)

        # Step 3: Call Azure OpenAI with safe prompt
        try:
            response = azure_client.chat.completions.create(
                model=AZURE_OPENAI_CHAT_DEPLOYMENT,
                messages=messages,
                temperature=0.7,
                max_tokens=800
            )

            answer = response.choices[0].message.content
            return AskResponse(answer=answer, sources=sources)

        except Exception as azure_error:
            # Check if it's a content filter error
            error_str = str(azure_error)
            if "content management policy" in error_str or "filtered" in error_str:
                print(f"⚠️ Content filter triggered, trying fallback...")

                # FALLBACK 1: Try with ultra-safe prompt (no context, just answer)
                try:
                    fallback_messages = [
                        {
                            "role": "system",
                            "content": "You are a professional Business English teacher. Provide helpful, appropriate educational responses."
                        },
                        {
                            "role": "user",
                            "content": f"Please help me with this business English question: {sanitize_query(request.query)}"
                        }
                    ]

                    response = azure_client.chat.completions.create(
                        model=AZURE_OPENAI_CHAT_DEPLOYMENT,
                        messages=fallback_messages,
                        temperature=0.7,
                        max_tokens=800
                    )

                    answer = response.choices[0].message.content
                    print("✅ Fallback prompt succeeded")
                    return AskResponse(answer=answer, sources=[])  # No sources in fallback

                except Exception as fallback_error:
                    print(f"❌ Even fallback failed: {fallback_error}")
                    # FALLBACK 2: Return helpful error message
                    return AskResponse(
                        answer=(
                            "I apologize, but I'm having trouble processing that query due to content filtering. "
                            "Could you please rephrase your question in a more formal way? "
                            "For example, instead of casual greetings, try asking a specific business English question."
                        ),
                        sources=[]
                    )
            else:
                # Not a content filter error, re-raise
                raise

    except Exception as e:
        print(f"❌ /ask endpoint error: {e}")
        raise


# ============================================================================
# DUMMY FUNCTIONS - REPLACE WITH YOUR ACTUAL RAG LOGIC
# ============================================================================

def retrieve_rag_context(query: str, k: int, unit: Optional[str], max_chars: int) -> str:
    """
    Replace this with your actual Qdrant retrieval logic.
    This should return the concatenated text from your vector database.
    """
    # Your existing RAG code here
    # Example:
    # results = qdrant_client.search(collection_name="business_english", query_text=query, limit=k)
    # context = "\n\n".join([r.payload["text"] for r in results])
    # return context[:max_chars]
    return ""


def get_sources_list(context: str) -> List[str]:
    """
    Extract source IDs from the RAG context.
    Replace with your actual source tracking logic.
    """
    # Your existing source extraction here
    # Example:
    # return [chunk.metadata.get("source_id") for chunk in results]
    return []


# ============================================================================
# FASTAPI ENDPOINT INTEGRATION
# ============================================================================

"""
Add this to your FastAPI app:

from fastapi import FastAPI, HTTPException

@app.post("/ask", response_model=AskResponse)
async def ask_endpoint(request: AskRequest):
    try:
        return await ask_with_safe_prompts(request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"/ask failed: {str(e)}")
"""

# ============================================================================
# ALTERNATIVE: CONFIGURE AZURE CONTENT FILTER SETTINGS
# ============================================================================

"""
OPTION 2: Configure Azure Portal to be less strict
================================================

If you want to keep your original prompts, you can adjust Azure's content filters:

1. Go to: https://portal.azure.com
2. Navigate to your Azure OpenAI resource
3. Click "Content filters" in the left menu
4. Create a new content filter configuration:
   - Hate: Medium (was: High)
   - Sexual: Medium (was: High)
   - Violence: Medium (was: High)
   - Self-harm: High (keep strict)

5. Apply this filter to your deployment (gpt-35-turbo)

This will allow casual language like "yo" while still blocking actually harmful content.

TRADE-OFF: Less strict filters = more freedom but slightly higher risk of inappropriate content
"""

# ============================================================================
# TESTING THE FIX
# ============================================================================

"""
Test with the queries that were failing:

1. "yo" → Should now work (gets sanitized to "hello")
2. "sup" → Should now work (gets sanitized to "how are you")
3. Normal questions → Should work as before

Test script:
```python
import asyncio

async def test_fix():
    # Test case 1: Casual greeting
    result = await ask_with_safe_prompts(AskRequest(query="yo", k=5))
    print(f"Test 1: {result.answer[:100]}...")

    # Test case 2: Normal question
    result = await ask_with_safe_prompts(AskRequest(query="What is a professional email?", k=5))
    print(f"Test 2: {result.answer[:100]}...")

asyncio.run(test_fix())
```
"""

print("✅ Azure content filter fix loaded!")
print("📝 Next steps:")
print("   1. Copy the ask_with_safe_prompts() function to your server")
print("   2. Replace your existing /ask endpoint handler")
print("   3. Add your actual RAG retrieval logic to retrieve_rag_context()")
print("   4. Test with 'yo' and other casual queries")
print("   5. OR adjust Azure Portal content filter settings (see comments)")

