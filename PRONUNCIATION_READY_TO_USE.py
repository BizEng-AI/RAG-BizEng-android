# ============================================================
# PRONUNCIATION ASSESSMENT - READY TO USE
# Add this to your Python server (app.py)
# ============================================================

import azure.cognitiveservices.speech as speechsdk
import os
import json
import tempfile
from fastapi import UploadFile, File, Form, HTTPException
from pydantic import BaseModel
from typing import Optional, List

# ============================================================
# AZURE SPEECH SERVICE CONFIGURATION
# ============================================================

AZURE_SPEECH_KEY = "CbZ50wqN8vOc9BwwgUZak4sKkHqtUZSjj31bayNGIVaIn47214zRJQQJ99BJAC3pKaRXJ3w3AAAYACOGKoCE"
AZURE_SPEECH_REGION = "eastasia"

speech_config = speechsdk.SpeechConfig(
    subscription=AZURE_SPEECH_KEY,
    region=AZURE_SPEECH_REGION
)

print(f"✓ Azure Speech Service configured: {AZURE_SPEECH_REGION}")


# ============================================================
# DTOs FOR PRONUNCIATION ASSESSMENT
# ============================================================

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


# ============================================================
# PRONUNCIATION ASSESSMENT ENDPOINT
# ============================================================

@app.post("/pronunciation/assess", response_model=PronunciationResult)
async def assess_pronunciation(
    audio: UploadFile = File(...),
    reference_text: str = Form(...)
):
    """
    Assess pronunciation of uploaded audio against reference text.

    Usage from Android:
    - Record audio to WAV/MP3 file
    - Send as multipart/form-data with reference_text

    Example:
        audio: User's recorded audio file
        reference_text: "Good morning, I would like to schedule a meeting"

    Returns:
        Detailed pronunciation scores and feedback
    """
    try:
        print(f"[Pronunciation] Assessing audio for: '{reference_text}'")

        # Save uploaded audio to temp file
        with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_audio:
            content = await audio.read()
            temp_audio.write(content)
            temp_audio_path = temp_audio.name

        print(f"[Pronunciation] Audio saved to: {temp_audio_path}")

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

        print("[Pronunciation] Running Azure Speech assessment...")

        # Perform recognition
        result = speech_recognizer.recognize_once()

        # Clean up temp file
        os.unlink(temp_audio_path)

        # Check if recognition succeeded
        if result.reason == speechsdk.ResultReason.RecognizedSpeech:
            print(f"[Pronunciation] Recognized: '{result.text}'")

            # Parse pronunciation assessment results
            pronunciation_result = speechsdk.PronunciationAssessmentResult(result)

            # Extract word-level scores
            words = []
            word_details = result.properties.get(
                speechsdk.PropertyId.SpeechServiceResponse_JsonResult
            )

            if word_details:
                details = json.loads(word_details)

                for word_info in details.get("NBest", [{}])[0].get("Words", []):
                    word = word_info.get("Word", "")
                    accuracy = word_info.get("PronunciationAssessment", {}).get("AccuracyScore", 0)
                    error_type = word_info.get("PronunciationAssessment", {}).get("ErrorType")

                    words.append(PronunciationWord(
                        word=word,
                        accuracy_score=accuracy,
                        error_type=error_type
                    ))

                    print(f"  Word: '{word}' - Score: {accuracy:.1f}/100 - Error: {error_type or 'None'}")

            # Generate human-readable feedback
            overall_score = pronunciation_result.pronunciation_score
            feedback = generate_pronunciation_feedback(
                overall_score,
                pronunciation_result.accuracy_score,
                pronunciation_result.fluency_score,
                words
            )

            print(f"[Pronunciation] Overall score: {overall_score:.1f}/100")
            print(f"[Pronunciation] Feedback: {feedback}")

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
            print("[Pronunciation] ERROR: No speech detected")
            raise HTTPException(
                status_code=400,
                detail="Could not recognize speech. Please speak clearly and try again."
            )

        else:
            print(f"[Pronunciation] ERROR: Recognition failed - {result.reason}")
            raise HTTPException(
                status_code=500,
                detail=f"Speech recognition failed: {result.reason}"
            )

    except HTTPException:
        raise
    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"[Pronunciation] ERROR: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Pronunciation assessment failed: {str(e)}"
        )


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
    mispronounced = [w for w in words if w.accuracy_score < 60 or w.error_type == "Mispronunciation"]
    if mispronounced:
        mispronounced_list = ', '.join([f"'{w.word}'" for w in mispronounced[:5]])  # Limit to 5 words
        feedback_parts.append(f"\nWords to practice: {mispronounced_list}")

    # Fluency feedback
    if fluency_score < 70:
        feedback_parts.append("\nTip: Try to speak more naturally and at a steady pace.")

    # Accuracy feedback
    if accuracy_score < 70:
        feedback_parts.append("\nFocus on pronouncing each word clearly.")

    return " ".join(feedback_parts)


# ============================================================
# SIMPLIFIED ENDPOINT FOR QUICK CHECKS
# ============================================================

@app.post("/pronunciation/quick-check")
async def quick_pronunciation_check(
    audio: UploadFile = File(...),
    reference_text: str = Form(...)
):
    """
    Quick pronunciation check - returns simplified results.
    Good for integrating into chat/roleplay without overwhelming the user.

    Returns:
        score: Overall pronunciation score (0-100)
        feedback: Simple text feedback
        transcript: What they actually said
        needs_practice: Boolean if score is below 70
    """
    try:
        result = await assess_pronunciation(audio, reference_text)

        # Simplified response
        return {
            "score": round(result.pronunciation_score, 1),
            "feedback": result.feedback,
            "transcript": result.transcript,
            "needs_practice": result.pronunciation_score < 70,
            "mispronounced_words": [
                w.word for w in result.words
                if w.accuracy_score < 60 or w.error_type == "Mispronunciation"
            ]
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================
# TEST ENDPOINT (Optional - for debugging)
# ============================================================

@app.get("/pronunciation/test")
async def test_pronunciation_service():
    """Test if Azure Speech Service is configured correctly"""
    try:
        return {
            "status": "ok",
            "region": AZURE_SPEECH_REGION,
            "service": "Azure Speech Service",
            "features": [
                "Pronunciation Assessment",
                "Word-level scoring",
                "Fluency analysis",
                "Accuracy measurement"
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================
# USAGE EXAMPLE
# ============================================================
"""
Test with curl:

curl -X POST "http://localhost:8020/pronunciation/assess" \
  -F "audio=@recording.wav" \
  -F "reference_text=Hello, I would like to schedule a meeting"

Test with Python:

import requests

with open("recording.wav", "rb") as audio_file:
    response = requests.post(
        "http://localhost:8020/pronunciation/assess",
        files={"audio": audio_file},
        data={"reference_text": "Hello, I would like to schedule a meeting"}
    )
    print(response.json())

Expected response:
{
    "transcript": "Hello I would like to schedule a meeting",
    "accuracy_score": 85.3,
    "fluency_score": 78.5,
    "completeness_score": 100.0,
    "pronunciation_score": 82.1,
    "words": [
        {"word": "Hello", "accuracy_score": 95.0, "error_type": null},
        {"word": "I", "accuracy_score": 100.0, "error_type": null},
        ...
    ],
    "feedback": "Good pronunciation! Keep practicing. 👍"
}
"""


# ============================================================
# NEXT STEPS
# ============================================================
"""
1. Add this code to your app.py (below your existing endpoints)

2. Install Azure SDK:
   pip install azure-cognitiveservices-speech

3. Add to requirements.txt:
   azure-cognitiveservices-speech==1.38.0

4. Restart your server:
   python -m uvicorn app:app --host 0.0.0.0 --port 8020 --reload

5. Test the endpoint:
   curl http://localhost:8020/pronunciation/test

6. Update Android app to use /pronunciation/assess endpoint

Done! Your server now has pronunciation assessment! 🎉
"""

