# ============================================================
# FIX FOR roleplay_referee.py
# Copy this code to replace the OpenAI initialization section
# ============================================================

# CURRENT CODE (Lines ~1-20 in roleplay_referee.py):
# -------------------------------------------------------
# from openai import OpenAI
# from settings import OPENAI_API_KEY, CHAT_MODEL
#
# oai = OpenAI(api_key=OPENAI_API_KEY)


# NEW CODE - Replace with this:
# -------------------------------------------------------
from openai import OpenAI, AzureOpenAI
from settings import (
    OPENAI_API_KEY,
    CHAT_MODEL,
    USE_AZURE,
    AZURE_OPENAI_KEY,
    AZURE_OPENAI_ENDPOINT,
    AZURE_OPENAI_API_VERSION,
    AZURE_OPENAI_CHAT_DEPLOYMENT
)

# Initialize OpenAI client (Azure or regular OpenAI)
if USE_AZURE:
    oai = AzureOpenAI(
        api_key=AZURE_OPENAI_KEY,
        api_version=AZURE_OPENAI_API_VERSION,
        azure_endpoint=AZURE_OPENAI_ENDPOINT
    )
    print(f"[referee] ✅ Using Azure OpenAI", flush=True)
else:
    oai = OpenAI(api_key=OPENAI_API_KEY)
    print(f"[referee] ✅ Using OpenAI (fallback)", flush=True)


# ============================================================
# THEN UPDATE THE _llm_analyze METHOD (Line ~85):
# ============================================================

# CURRENT CODE:
# -------------------------------------------------------
# def _llm_analyze(self, prompt: str, schema: dict) -> dict:
#     response = oai.chat.completions.create(
#         model=CHAT_MODEL,  # ← This won't work with Azure!
#         messages=[...],
#         ...
#     )


# NEW CODE - Replace with this:
# -------------------------------------------------------
def _llm_analyze(self, prompt: str, schema: dict) -> dict:
    # Use Azure deployment name if using Azure, otherwise use CHAT_MODEL
    chat_model = AZURE_OPENAI_CHAT_DEPLOYMENT if USE_AZURE else CHAT_MODEL

    response = oai.chat.completions.create(
        model=chat_model,  # ← Now works with both Azure and OpenAI!
        messages=[{"role": "system", "content": "You are a helpful assistant."},
                  {"role": "user", "content": prompt}],
        max_completion_tokens=200,
        temperature=0.3
    )
    # ...rest of the method stays the same


# ============================================================
# SUMMARY OF CHANGES:
# ============================================================
# 1. Import AzureOpenAI and all Azure settings
# 2. Add conditional client initialization (Azure vs OpenAI)
# 3. Update model parameter in _llm_analyze to use deployment name when Azure
#
# That's it! Now roleplay_referee.py will support Azure OpenAI.

