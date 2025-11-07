# ============================================================
# ADD PRONUNCIATION ASSESSMENT TO YOUR PYTHON SERVER
# Azure Speech Service Implementation
# ============================================================

# STEP 1: Install Azure Speech SDK
# ============================================================
# Run in your Python server folder:
# pip install azure-cognitiveservices-speech

# Add to requirements.txt:
"""
azure-cognitiveservices-speech==1.38.0
"""


# STEP 2: Get Azure Speech Service Credentials
# ============================================================
# 1. Go to: https://portal.azure.com
# 2. Create "Speech Service" resource (FREE tier available!)
# 3. Get your:
#    - Speech Key (from Keys and Endpoint section)
#    - Region (e.g., "eastus", "westeurope")
# 4. Add to your .env file:
"""
AZURE_SPEECH_KEY=your_key_here
AZURE_SPEECH_REGION=eastus
"""


# STEP 3: Add Pronunciation Assessment Endpoint
# ============================================================
# Add this to your app.py (or main.py):

import azure.cognitiveservices.speech as speechsdk
import os
from fastapi import UploadFile, File, Form
from pydantic import BaseModel
from typing import Optional, List

# Initialize Azure Speech Config (add near top of file)
AZURE_SPEECH_KEY = os.getenv("AZURE_SPEECH_KEY")
AZURE_SPEECH_REGION = os.getenv("AZURE_SPEECH_REGION", "eastus")

speech_config = speechsdk.SpeechConfig(
    subscription=AZURE_SPEECH_KEY,
    region=AZURE_SPEECH_REGION
)


# DTOs for pronunciation assessment
class PronunciationWord(BaseModel):
    word: str
    accuracy_score: float  # 0-100
    error_type: Optional[str] = None  # "Mispronunciation", "Omission", "Insertion", or None

class PronunciationResult(BaseModel):
    transcript: str  # What the user actually said
    accuracy_score: float  # 0-100 overall accuracy
    fluency_score: float  # 0-100 how natural it sounds
    completeness_score: float  # 0-100 did they say everything
    pronunciation_score: float  # 0-100 overall pronunciation
    words: List[PronunciationWord]  # Individual word scores
    feedback: str  # Human-readable feedback


@app.post("/pronunciation/assess", response_model=PronunciationResult)
async def assess_pronunciation(
    audio: UploadFile = File(...),
    reference_text: str = Form(...)  # The text they should be saying
):
    """
    Assess pronunciation of uploaded audio against reference text.

    Args:
        audio: Audio file (WAV, MP3, etc.)
        reference_text: The text the user should be pronouncing

    Returns:
        Detailed pronunciation assessment with scores and feedback
    """
    try:
        # Save uploaded audio to temp file
        import tempfile
        with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_audio:
            content = await audio.read()
            temp_audio.write(content)
            temp_audio_path = temp_audio.name

        # Configure audio input
        audio_config = speechsdk.AudioConfig(filename=temp_audio_path)

        # Configure pronunciation assessment
        pronunciation_config = speechsdk.PronunciationAssessmentConfig(
            reference_text=reference_text,
            grading_system=speechsdk.PronunciationAssessmentGradingSystem.HundredMark,
            granularity=speechsdk.PronunciationAssessmentGranularity.Word,
            enable_miscue=True  # Detect if they said wrong words
        )

        # Create speech recognizer
        speech_recognizer = speechsdk.SpeechRecognizer(
            speech_config=speech_config,
            audio_config=audio_config
        )

        # Apply pronunciation assessment
        pronunciation_config.apply_to(speech_recognizer)

        # Perform recognition
        result = speech_recognizer.recognize_once()

        # Clean up temp file
        os.unlink(temp_audio_path)

        # Check if recognition succeeded
        if result.reason == speechsdk.ResultReason.RecognizedSpeech:
            # Parse pronunciation assessment results
            pronunciation_result = speechsdk.PronunciationAssessmentResult(result)

            # Extract word-level scores
            words = []
            word_details = result.properties.get(
                speechsdk.PropertyId.SpeechServiceResponse_JsonResult
            )

            if word_details:
                import json
                details = json.loads(word_details)

                for word_info in details.get("NBest", [{}])[0].get("Words", []):
                    words.append(PronunciationWord(
                        word=word_info.get("Word", ""),
                        accuracy_score=word_info.get("PronunciationAssessment", {}).get("AccuracyScore", 0),
                        error_type=word_info.get("PronunciationAssessment", {}).get("ErrorType")
                    ))

            # Generate human-readable feedback
            overall_score = pronunciation_result.pronunciation_score
            feedback = generate_pronunciation_feedback(
                overall_score,
                pronunciation_result.accuracy_score,
                pronunciation_result.fluency_score,
                words
            )

            return PronunciationResult(
                transcript=result.text,
                accuracy_score=pronunciation_result.accuracy_score,
                fluency_score=pronunciation_result.fluency_score,
                completeness_score=pronunciation_result.completeness_score,
                pronunciation_score=pronunciation_result.pronunciation_score,
                words=words,
                feedback=feedback
            )

        elif result.reason == speechsdk.ResultReason.NoMatch:
            raise HTTPException(status_code=400, detail="Could not recognize speech. Please speak clearly and try again.")

        else:
            raise HTTPException(status_code=500, detail=f"Speech recognition failed: {result.reason}")

    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Pronunciation assessment failed: {str(e)}")


def generate_pronunciation_feedback(
    overall_score: float,
    accuracy_score: float,
    fluency_score: float,
    words: List[PronunciationWord]
) -> str:
    """Generate human-readable feedback based on scores"""

    feedback_parts = []

    # Overall assessment
    if overall_score >= 90:
        feedback_parts.append("Excellent pronunciation! 🌟")
    elif overall_score >= 75:
        feedback_parts.append("Good pronunciation! Keep practicing. 👍")
    elif overall_score >= 60:
        feedback_parts.append("Fair pronunciation. Focus on the specific words below. 📚")
    else:
        feedback_parts.append("Needs improvement. Practice the words highlighted below. 💪")

    # Identify mispronounced words
    mispronounced = [w for w in words if w.accuracy_score < 60]
    if mispronounced:
        feedback_parts.append(f"\nWords to practice: {', '.join([w.word for w in mispronounced])}")

    # Fluency feedback
    if fluency_score < 70:
        feedback_parts.append("\nTip: Try to speak more naturally and at a steady pace.")

    return " ".join(feedback_parts)


# ALTERNATIVE: Simpler endpoint for roleplay integration
@app.post("/pronunciation/quick-check")
async def quick_pronunciation_check(
    audio: UploadFile = File(...),
    reference_text: str = Form(...)
):
    """
    Quick pronunciation check - returns just a score and simple feedback.
    Good for integrating into roleplay conversations.
    """
    try:
        result = await assess_pronunciation(audio, reference_text)

        # Simplified response
        return {
            "score": result.pronunciation_score,
            "feedback": result.feedback,
            "transcript": result.transcript,
            "needs_practice": result.pronunciation_score < 70
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================
# STEP 4: Test the Endpoint
# ============================================================
"""
Using curl (from command line):

curl -X POST "http://localhost:8020/pronunciation/assess" \
  -F "audio=@test_audio.wav" \
  -F "reference_text=Hello, how are you today?"

Or using Python requests:

import requests

with open("test_audio.wav", "rb") as audio_file:
    response = requests.post(
        "http://localhost:8020/pronunciation/assess",
        files={"audio": audio_file},
        data={"reference_text": "Hello, how are you today?"}
    )
    print(response.json())
"""


# ============================================================
# STEP 5: Pricing Estimate
# ============================================================
"""
Azure Speech Service Pricing (as of 2024):
- Pronunciation Assessment: $1 per 1,000 assessments
- Free tier: 5 hours per month (about 300 assessments)

For 100 users doing 10 assessments each per month:
- 1,000 assessments = $1/month

Very affordable!
"""


# ============================================================
# OPTIONAL: Add to Roleplay Endpoint
# ============================================================
"""
You can integrate pronunciation feedback directly into roleplay responses:

@app.post("/roleplay/turn-with-audio")
async def roleplay_turn_with_audio(
    session_id: str = Form(...),
    audio: UploadFile = File(...),
    reference_text: Optional[str] = Form(None)  # Expected response
):
    # 1. Convert audio to text
    # 2. Assess pronunciation if reference text provided
    # 3. Generate roleplay response
    # 4. Include pronunciation feedback in correction field

    pronunciation_feedback = None
    if reference_text:
        pronunciation_result = await assess_pronunciation(audio, reference_text)
        if pronunciation_result.pronunciation_score < 70:
            pronunciation_feedback = f"Pronunciation tip: {pronunciation_result.feedback}"

    # ... rest of roleplay logic ...

    return {
        "ai_message": "...",
        "correction": pronunciation_feedback,
        # ...
    }
"""


# ============================================================
# SUMMARY
# ============================================================
"""
1. Install: pip install azure-cognitiveservices-speech
2. Get Azure Speech Service credentials (FREE tier available)
3. Add the endpoint code above to your app.py
4. Test with curl or from Android app
5. Costs ~$1 per 1,000 assessments (very cheap!)

The Android app will:
1. Record audio when user taps mic
2. Send audio file + reference text to /pronunciation/assess
3. Display scores and feedback to user
4. Highlight mispronounced words

Want me to update the Android app to use this endpoint?
"""

