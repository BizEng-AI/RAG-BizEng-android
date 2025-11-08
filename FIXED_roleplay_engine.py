     * "That kind of language won't be tolerated in a professional environment."
3. Redirect the conversation back on track briefly
4. Keep it under 2-3 sentences
5. Be firm but not hostile - maintain professionalism

Do NOT ignore the inappropriate language. Address it directly and professionally."""

        elif correction_made and error_type == "register":
            print(f"[engine] ⚠️ Register error detected - AI will note casual language", flush=True)
            system_prompt = f"""You are {self.scenario['ai_role']} in a {self.scenario['title']} roleplay.

Context: {self.scenario['context']}
Current Stage: {stage_info['name']} - {stage_info['objective']}

NOTE: The student used somewhat casual/informal language.

Your response should:
1. Stay in character as {self.scenario['ai_role']}
2. Respond naturally to their message
3. Optionally model more formal language in your response
4. Keep it under 3 sentences
5. Guide the conversation forward

The specific language correction is shown separately - just respond naturally."""

        else:
            # No error or minor grammar/vocabulary error - respond normally
            system_prompt = f"""You are {self.scenario['ai_role']} in a {self.scenario['title']} roleplay.

Context: {self.scenario['context']}
Current Stage: {stage_info['name']} - {stage_info['objective']}

IMPORTANT:
- Stay in character as {self.scenario['ai_role']}
- Respond naturally to what the student says
- Do NOT mention specific grammar/vocabulary corrections (those are handled separately)
- Keep responses under 3 sentences
- Guide the conversation towards the stage objective

Respond naturally to the student's message."""

        messages = [{"role": "system", "content": system_prompt}] + self.conversation_history[-6:]

        chat_model = AZURE_OPENAI_CHAT_DEPLOYMENT if USE_AZURE else CHAT_MODEL

        try:
            response = oai.chat.completions.create(
                model=chat_model,
                messages=messages,
                max_completion_tokens=150,
                temperature=0.7
            )

            ai_message = response.choices[0].message.content.strip()

            # Add to history
            self.conversation_history.append({
                "role": "assistant",
                "content": ai_message
            })

            print(f"[engine] Generated response: {ai_message[:100]}...", flush=True)
            return ai_message

        except Exception as e:
            print(f"[engine] Error generating response: {e}", flush=True)
            # Fallback response
            fallback = "Could you please rephrase that in a professional manner?"
            self.conversation_history.append({
                "role": "assistant",
                "content": fallback
            })
            return fallback
"""
COMPLETE FIXED roleplay_engine.py
Replace your current roleplay_engine.py with this file

FIXES:
1. AI now responds appropriately to profanity/inappropriate language
2. AI stays in character while addressing errors
3. Separates error handling (profanity) from normal conversation flow
"""
from __future__ import annotations

from typing import Dict, List, Optional
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
    print(f"[engine] ✅ Using Azure OpenAI", flush=True)
else:
    oai = OpenAI(api_key=OPENAI_API_KEY)
    print(f"[engine] ✅ Using OpenAI (fallback)", flush=True)


# ============================================================================
# SCENARIO DEFINITIONS
# ============================================================================

SCENARIOS = {
    "job_interview": {
        "title": "Job Interview",
        "description": "Practice interviewing for a business position",
        "context": "You are interviewing for a Marketing Manager position at a technology company.",
        "student_role": "Job Candidate",
        "ai_role": "Hiring Manager",
        "stages": [
            {
                "name": "opening",
                "objective": "Introduce yourself professionally",
                "keywords": ["name", "experience", "background"],
                "advance_criteria": "Student introduces themselves",
                "hints": ["Tell me about yourself and your experience"]
            },
            {
                "name": "development",
                "objective": "Discuss your qualifications and experience",
                "keywords": ["skills", "achievement", "project", "responsibility"],
                "advance_criteria": "Student describes relevant experience",
                "hints": ["What are your key strengths?", "Tell me about a successful project"]
            },
            {
                "name": "closing",
                "objective": "Ask questions and wrap up professionally",
                "keywords": ["question", "when", "next", "contact"],
                "advance_criteria": "Student asks a question or thanks interviewer",
                "hints": ["Do you have any questions for me?"]
            }
        ]
    },
    "client_meeting": {
        "title": "Client Meeting",
        "description": "Meet with a potential client to discuss services",
        "context": "You are meeting with a potential client to discuss your company's consulting services.",
        "student_role": "Sales Consultant",
        "ai_role": "Potential Client",
        "stages": [
            {
                "name": "opening",
                "objective": "Build rapport and understand client needs",
                "keywords": ["nice", "meet", "pleasure", "need", "looking"],
                "advance_criteria": "Student greets and asks about needs",
                "hints": ["How can I help you today?"]
            },
            {
                "name": "development",
                "objective": "Present solutions and address concerns",
                "keywords": ["offer", "solution", "provide", "help", "service"],
                "advance_criteria": "Student presents a solution",
                "hints": ["What solutions do you offer?"]
            },
            {
                "name": "closing",
                "objective": "Agree on next steps",
                "keywords": ["follow", "next", "contact", "schedule", "send"],
                "advance_criteria": "Student proposes next steps",
                "hints": ["What are the next steps?"]
            }
        ]
    },
    "customer_complaint": {
        "title": "Customer Complaint",
        "description": "Handle an upset customer professionally",
        "context": "A customer is unhappy with your product/service and needs resolution.",
        "student_role": "Customer Service Representative",
        "ai_role": "Upset Customer",
        "stages": [
            {
                "name": "opening",
                "objective": "Listen and show empathy",
                "keywords": ["sorry", "understand", "apologize", "hear"],
                "advance_criteria": "Student acknowledges the issue",
                "hints": ["I apologize for the inconvenience"]
            },
            {
                "name": "development",
                "objective": "Offer solutions",
                "keywords": ["solution", "fix", "resolve", "can", "will"],
                "advance_criteria": "Student proposes a solution",
                "hints": ["Here's what we can do..."]
            },
            {
                "name": "closing",
                "objective": "Ensure satisfaction",
                "keywords": ["satisfied", "else", "help", "thank"],
                "advance_criteria": "Student confirms resolution",
                "hints": ["Is there anything else I can help with?"]
            }
        ]
    },
    "team_meeting": {
        "title": "Team Meeting",
        "description": "Lead or participate in a team discussion",
        "context": "You are in a team meeting to discuss project progress and next steps.",
        "student_role": "Team Member",
        "ai_role": "Team Leader",
        "stages": [
            {
                "name": "opening",
                "objective": "Share your update",
                "keywords": ["update", "progress", "working", "completed"],
                "advance_criteria": "Student provides status update",
                "hints": ["What's your status on the project?"]
            },
            {
                "name": "development",
                "objective": "Discuss challenges or ideas",
                "keywords": ["challenge", "issue", "idea", "suggest", "think"],
                "advance_criteria": "Student mentions a challenge or idea",
                "hints": ["Any challenges or ideas?"]
            },
            {
                "name": "closing",
                "objective": "Agree on action items",
                "keywords": ["will", "next", "action", "plan", "by"],
                "advance_criteria": "Student commits to action",
                "hints": ["What will you work on next?"]
            }
        ]
    },
    "business_phone_call": {
        "title": "Business Phone Call",
        "description": "Make or receive a professional phone call",
        "context": "You are calling a business contact to schedule a meeting.",
        "student_role": "Caller",
        "ai_role": "Business Contact",
        "stages": [
            {
                "name": "opening",
                "objective": "Greet and state purpose",
                "keywords": ["calling", "reason", "regarding", "about"],
                "advance_criteria": "Student states reason for call",
                "hints": ["I'm calling regarding..."]
            },
            {
                "name": "development",
                "objective": "Discuss details",
                "keywords": ["when", "time", "available", "schedule", "meet"],
                "advance_criteria": "Student discusses scheduling",
                "hints": ["When would be convenient for you?"]
            },
            {
                "name": "closing",
                "objective": "Confirm and end professionally",
                "keywords": ["confirm", "thank", "look forward", "goodbye"],
                "advance_criteria": "Student confirms and thanks",
                "hints": ["Thank you for your time"]
            }
        ]
    }
}


class RoleplayEngine:
    """Manages roleplay scenario progression and AI responses"""

    def __init__(self, scenario_id: str, use_rag: bool = True):
        if scenario_id not in SCENARIOS:
            raise ValueError(f"Unknown scenario: {scenario_id}")

        self.scenario_id = scenario_id
        self.scenario = SCENARIOS[scenario_id]
        self.use_rag = use_rag
        self.current_stage = "opening"
        self.stage_index = 0
        self.conversation_history = []

    def get_scenario(self) -> Dict:
        """Get scenario details"""
        return self.scenario

    def get_current_stage(self) -> Dict:
        """Get current stage information"""
        return self.scenario["stages"][self.stage_index]

    def advance_stage(self) -> bool:
        """Move to next stage if available. Returns True if advanced."""
        if self.stage_index < len(self.scenario["stages"]) - 1:
            self.stage_index += 1
            self.current_stage = self.scenario["stages"][self.stage_index]["name"]
            print(f"[engine] Advanced to stage: {self.current_stage}", flush=True)
            return True
        return False

    def is_completed(self) -> bool:
        """Check if roleplay is complete"""
        return self.stage_index >= len(self.scenario["stages"]) - 1

    def generate_opening_message(self) -> str:
        """Generate AI's opening message"""
        chat_model = AZURE_OPENAI_CHAT_DEPLOYMENT if USE_AZURE else CHAT_MODEL

        system_prompt = f"""You are {self.scenario['ai_role']} in a {self.scenario['title']} roleplay.
Context: {self.scenario['context']}

Generate a brief, natural opening message to start the roleplay. Stay in character.
Keep it under 2 sentences."""

        response = oai.chat.completions.create(
            model=chat_model,
            messages=[{"role": "system", "content": system_prompt}],
            max_completion_tokens=100,
            temperature=0.7
        )

        return response.choices[0].message.content.strip()

    def generate_response(self, student_message: str, correction_made: bool = False, error_type: str = None) -> str:
        """
        Generate AI response (staying in character, addressing inappropriate language if needed)

        Args:
            student_message: What the student said
            correction_made: Whether an error was detected
            error_type: Type of error (pragmatic, grammar, register, vocabulary)
        """

        # Add to conversation history
        self.conversation_history.append({
            "role": "user",
            "content": student_message
        })

        # Build system prompt based on error type
        stage_info = self.get_current_stage()

        # CRITICAL: Handle profanity/inappropriate language differently
        if correction_made and error_type == "pragmatic":
            print(f"[engine] 🚨 Pragmatic error detected - AI will address inappropriate language", flush=True)
            system_prompt = f"""You are {self.scenario['ai_role']} in a {self.scenario['title']} roleplay.

Context: {self.scenario['context']}
Current Stage: {stage_info['name']} - {stage_info['objective']}

⚠️ CRITICAL SITUATION: The student just used inappropriate, offensive, or unprofessional language.

Your response MUST:
1. Stay in character as {self.scenario['ai_role']}
2. Address the inappropriate language firmly but professionally
   - Examples:
     * "I understand you may be frustrated, but that language isn't appropriate for a business setting."
     * "Let's keep this professional, please."

