"""
FastAPI router for roleplay endpoints with session management.
Matches Android app's expected request/response format exactly.
"""
from __future__ import annotations

import uuid
from typing import Dict, Optional, List
from datetime import datetime

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from roleplay_referee import referee
from roleplay_engine import RoleplayEngine

router = APIRouter(prefix="/roleplay", tags=["roleplay"])

# In-memory session storage (use Redis/database in production)
sessions: Dict[str, dict] = {}

# ============================================================================
# REQUEST/RESPONSE MODELS - MATCH ANDROID EXACTLY
# ============================================================================

class RoleplayStartReq(BaseModel):
    scenario_id: str
    student_name: str = "Student"
    use_rag: bool = True

class RoleplayStartResp(BaseModel):
    session_id: str
    scenario_title: str
    scenario_description: str
    context: str
    student_role: str
    ai_role: str
    current_stage: str
    initial_message: Optional[str] = None

class RoleplayTurnReq(BaseModel):
    session_id: str
    message: str  # ← Android sends "message", not "student_message"

class ErrorDetail(BaseModel):
    type: str           # "grammar", "register", "vocabulary", "pragmatic"
    incorrect: str      # What they said wrong
    correct: str        # Correct version
    explanation: str    # Why it's wrong

class Correction(BaseModel):
    has_errors: bool = False           # ← Android expects this Boolean
    errors: Optional[List[ErrorDetail]] = None  # ← Android expects this array
    feedback: Optional[str] = None

class RoleplayTurnResp(BaseModel):
    ai_message: str
    correction: Optional[Correction] = None
    current_stage: str = "development"
    is_completed: bool = False
    feedback: Optional[str] = None

# ============================================================================
# ENDPOINTS
# ============================================================================

@router.post("/start", response_model=RoleplayStartResp)
async def start_roleplay(req: RoleplayStartReq) -> RoleplayStartResp:
    """
    Start a new roleplay session.
    Creates session, initializes engine, returns AI's opening message.
    """
    try:
        print(f"[roleplay/start] scenario={req.scenario_id} use_rag={req.use_rag}", flush=True)

        # Create new session ID
        session_id = str(uuid.uuid4())

        # Initialize roleplay engine
        engine = RoleplayEngine(
            scenario_id=req.scenario_id,
            use_rag=req.use_rag
        )

        # Get scenario details
        scenario = engine.get_scenario()

        # Generate opening message
        initial_message = engine.generate_opening_message()

        # Store session
        sessions[session_id] = {
            "engine": engine,
            "created_at": datetime.now().isoformat(),
            "scenario_id": req.scenario_id,
            "student_name": req.student_name,
            "use_rag": req.use_rag,
            "turn_count": 0,
            "current_stage": "opening"
        }

        print(f"[roleplay/start] ✓ Session created: {session_id}", flush=True)

        return RoleplayStartResp(
            session_id=session_id,
            scenario_title=scenario["title"],
            scenario_description=scenario["description"],
            context=scenario["context"],
            student_role=scenario["student_role"],
            ai_role=scenario["ai_role"],
            current_stage="opening",
            initial_message=initial_message
        )

    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"[roleplay/start] ERROR: {e}", flush=True)
        raise HTTPException(status_code=500, detail=f"Failed to start roleplay: {str(e)}")


@router.post("/turn", response_model=RoleplayTurnResp)
async def submit_turn(req: RoleplayTurnReq) -> RoleplayTurnResp:
    """
    Submit a student message and get AI response with correction.

    CRITICAL: Returns correction in the format Android expects:
    {
        "has_errors": bool,
        "errors": [{"type": "", "incorrect": "", "correct": "", "explanation": ""}],
        "feedback": ""
    }
    """
    try:
        print(f"[roleplay/turn] session={req.session_id[:8]}... msg='{req.message[:50]}...'", flush=True)

        # Get session
        session = sessions.get(req.session_id)
        if not session:
            raise HTTPException(status_code=404, detail="Session not found. Please start a new roleplay.")

        engine: RoleplayEngine = session["engine"]
        session["turn_count"] += 1

        # === STEP 1: Evaluate student's message ===
        scenario = engine.get_scenario()
        current_stage_info = engine.get_current_stage()

        evaluation = referee.evaluate_response(
            student_message=req.message,
            scenario_context=scenario["context"],
            stage_objective=current_stage_info.get("objective", ""),
            ai_role=scenario["ai_role"]
        )

        # Convert referee's format to Android's format
        correction = None
        if evaluation:
            # Referee found an error - convert to Android format
            correction = Correction(
                has_errors=True,
                errors=[
                    ErrorDetail(
                        type=evaluation.get("error_type", "grammar"),
                        incorrect=evaluation.get("original", ""),
                        correct=evaluation.get("corrected", ""),
                        explanation=evaluation.get("explanation", "")
                    )
                ],
                feedback=f"Priority: {evaluation.get('priority', 'medium')}. Keep practicing!"
            )
            print(f"[roleplay/turn] ⚠️ Error detected: {evaluation['error_type']}", flush=True)
        else:
            # No errors - still send correction object but with has_errors=false
            correction = Correction(
                has_errors=False,
                errors=[],
                feedback=None
            )
            print(f"[roleplay/turn] ✓ No errors detected", flush=True)

        # === STEP 2: Generate AI response (stay in character) ===
        # Pass error type so AI can respond appropriately to profanity/inappropriate language
        error_type = evaluation.get("error_type") if evaluation else None
        ai_response = engine.generate_response(
            student_message=req.message,
            correction_made=evaluation is not None,
            error_type=error_type
        )

        # === STEP 3: Check if should advance stage ===
        if referee.check_stage_completion(
            student_message=req.message,
            advance_criteria=current_stage_info.get("advance_criteria", ""),
            stage_keywords=current_stage_info.get("keywords", [])
        ):
            advanced = engine.advance_stage()
            if advanced:
                print(f"[roleplay/turn] ✓ Advanced to stage: {engine.current_stage}", flush=True)

        # Update session
        session["current_stage"] = engine.current_stage
        is_completed = engine.is_completed()

        print(f"[roleplay/turn] ✓ Response generated, stage={engine.current_stage}, completed={is_completed}", flush=True)

        return RoleplayTurnResp(
            ai_message=ai_response,
            correction=correction,  # ← Now in correct format!
            current_stage=engine.current_stage,
            is_completed=is_completed,
            feedback=None
        )

    except HTTPException:
        raise
    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"[roleplay/turn] ERROR: {e}", flush=True)
        raise HTTPException(status_code=500, detail=f"Failed to process turn: {str(e)}")


@router.get("/sessions")
async def list_sessions():
    """Debug endpoint to see active sessions"""
    return {
        "active_sessions": len(sessions),
        "sessions": [
            {
                "id": sid[:8] + "...",
                "scenario": s["scenario_id"],
                "turns": s["turn_count"],
                "stage": s["current_stage"]
            }
            for sid, s in sessions.items()
        ]
    }


@router.delete("/session/{session_id}")
async def delete_session(session_id: str):
    """Delete a session (cleanup)"""
    if session_id in sessions:
        del sessions[session_id]
        return {"status": "deleted"}
    raise HTTPException(status_code=404, detail="Session not found")

