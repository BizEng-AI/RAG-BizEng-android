"""
FIXED ROLEPLAY REFEREE - AGGRESSIVE ERROR DETECTION
Replace your roleplay_referee.py with this version

This version:
1. Detects profanity and inappropriate language
2. More aggressive with error detection
3. Better at catching unprofessional register
4. Provides specific, actionable feedback
"""
from __future__ import annotations
import re
from typing import Optional, Dict, Any, List
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

class RoleplayReferee:
    """Analyzes student messages and provides feedback"""

    def __init__(self):
        self.error_types = ["grammar", "register", "vocabulary", "pragmatic"]

        # Profanity and inappropriate words list
        self.profanity_list = [
            "fuck", "shit", "damn", "hell", "bitch", "ass", "bastard",
            "crap", "piss", "dick", "cock", "pussy", "fag", "slut",
            "whore", "motherfucker", "asshole", "bullshit"
        ]

        # Casual/unprofessional phrases
        self.casual_phrases = {
            "gonna": "going to",
            "wanna": "want to",
            "gotta": "have to",
            "dunno": "don't know",
            "yeah": "yes",
            "nah": "no",
            "hey": "hello",
            "sup": "what's up",
            "k": "okay",
            "lol": "(remove)",
            "lmao": "(remove)",
            "wtf": "(inappropriate)",
            "omg": "oh my",
            "btw": "by the way",
            "idk": "I don't know"
        }

    def evaluate_response(
        self,
        student_message: str,
        scenario_context: str,
        stage_objective: str,
        ai_role: str
    ) -> Optional[Dict[str, Any]]:
        """
        Analyze student's message and return ONE priority correction if needed.
        Returns None if message is good, or dict with correction details.
        """
        message_lower = student_message.lower().strip()

        # PRIORITY 1: Check for profanity/inappropriate language
        profanity_check = self._check_profanity(student_message)
        if profanity_check:
            print(f"[referee] 🚨 PROFANITY DETECTED: {profanity_check}", flush=True)
            return profanity_check

        # PRIORITY 2: Check for extremely casual language
        casual_check = self._check_casual_language(student_message)
        if casual_check:
            print(f"[referee] ⚠️ CASUAL LANGUAGE: {casual_check}", flush=True)
            return casual_check

        # PRIORITY 3: Quick checks
        if len(message_lower) < 3:
            return {
                "error_type": "pragmatic",
                "original": student_message,
                "corrected": "(Please provide a more complete response)",
                "explanation": "Your response is too short. Try to express your thoughts more fully.",
                "priority": "high"
            }

        # PRIORITY 4: Use LLM for deeper analysis
        analysis = self._llm_analyze(student_message, scenario_context, stage_objective, ai_role)
        return analysis

    def _check_profanity(self, message: str) -> Optional[Dict[str, Any]]:
        """Check for profanity and inappropriate language"""
        message_lower = message.lower()

        # Remove common punctuation that might hide profanity
        cleaned = re.sub(r'[*@#$%!]', '', message_lower)
        words = re.findall(r'\b\w+\b', cleaned)

        for word in words:
            for profanity in self.profanity_list:
                # Check exact match or if profanity is part of word (e.g., "fucking")
                if word == profanity or profanity in word:
                    return {
                        "error_type": "pragmatic",
                        "original": message,
                        "corrected": "(This language is inappropriate for business settings)",
                        "explanation": (
                            "This language is completely unacceptable in professional business communication. "
                            "Always maintain respectful, professional language even when frustrated or upset."
                        ),
                        "priority": "critical"
                    }

        return None

    def _check_casual_language(self, message: str) -> Optional[Dict[str, Any]]:
        """Check for casual/informal language"""
        message_lower = message.lower()

        for casual, formal in self.casual_phrases.items():
            # Use word boundaries to match whole words
            pattern = r'\b' + re.escape(casual) + r'\b'
            if re.search(pattern, message_lower):
                return {
                    "error_type": "register",
                    "original": casual,
                    "corrected": formal if formal != "(remove)" and formal != "(inappropriate)" else "",
                    "explanation": (
                        f"'{casual}' is too casual for business settings. "
                        f"Use '{formal}' instead." if formal not in ["(remove)", "(inappropriate)"]
                        else f"'{casual}' should not be used in professional communication."
                    ),
                    "priority": "high"
                }

        return None

    def _llm_analyze(
        self,
        student_message: str,
        scenario_context: str,
        stage_objective: str,
        ai_role: str
    ) -> Optional[Dict[str, Any]]:
        """Use LLM to analyze for errors"""

        system_prompt = """You are a STRICT Business English tutor evaluating student responses in a roleplay scenario.

Analyze the student's message for errors in these categories:
1. GRAMMAR - incorrect tenses, subject-verb agreement, articles, etc.
2. REGISTER - too casual/formal for business context (e.g., "Hey" vs "Hello", contractions in formal settings)
3. VOCABULARY - incorrect word choice, unnatural phrasing
4. PRAGMATIC - culturally inappropriate, unclear intent, poor structure for business communication

IMPORTANT RULES:
- BE STRICT: Even minor issues should be flagged if they affect professionalism
- Only flag ONE error - the most important one
- If profanity or extremely casual language is present, ALWAYS flag it as PRAGMATIC/high priority
- If the message is perfectly acceptable for business, return "NO_ERROR"
- Consider the scenario context and stage objective

Return your analysis in this exact format:
ERROR_TYPE: [grammar|register|vocabulary|pragmatic|NO_ERROR]
ORIGINAL: [the problematic phrase or word]
CORRECTED: [how to fix it]
EXPLANATION: [brief explanation in one sentence]
PRIORITY: [critical|high|medium|low]"""

        user_prompt = f"""SCENARIO CONTEXT: {scenario_context}
AI ROLE: {ai_role}
STAGE OBJECTIVE: {stage_objective}

STUDENT'S MESSAGE: "{student_message}"

Analyze this message and provide feedback. Be strict about professionalism."""

        try:
            # Use Azure deployment name if using Azure, otherwise use CHAT_MODEL
            chat_model = AZURE_OPENAI_CHAT_DEPLOYMENT if USE_AZURE else CHAT_MODEL

            response = oai.chat.completions.create(
                model=chat_model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                max_completion_tokens=200,
                temperature=0.2  # Lower temperature for more consistent evaluation
            )

            analysis_text = response.choices[0].message.content.strip()

            # Parse the response
            if "NO_ERROR" in analysis_text:
                return None

            # Extract fields using regex
            error_type_match = re.search(r"ERROR_TYPE:\s*(\w+)", analysis_text, re.IGNORECASE)
            original_match = re.search(r"ORIGINAL:\s*(.+?)(?=\n|CORRECTED:)", analysis_text, re.IGNORECASE | re.DOTALL)
            corrected_match = re.search(r"CORRECTED:\s*(.+?)(?=\n|EXPLANATION:)", analysis_text, re.IGNORECASE | re.DOTALL)
            explanation_match = re.search(r"EXPLANATION:\s*(.+?)(?=\n|PRIORITY:|$)", analysis_text, re.IGNORECASE | re.DOTALL)
            priority_match = re.search(r"PRIORITY:\s*(\w+)", analysis_text, re.IGNORECASE)

            if error_type_match and error_type_match.group(1).lower() != "no_error":
                return {
                    "error_type": error_type_match.group(1).lower() if error_type_match else "grammar",
                    "original": original_match.group(1).strip() if original_match else student_message,
                    "corrected": corrected_match.group(1).strip() if corrected_match else "",
                    "explanation": explanation_match.group(1).strip() if explanation_match else "Consider this correction.",
                    "priority": priority_match.group(1).lower() if priority_match else "medium"
                }

            return None

        except Exception as e:
            print(f"[referee] Error analyzing message: {e}", flush=True)
            return None

    def check_stage_completion(
        self,
        student_message: str,
        advance_criteria: str,
        stage_keywords: List[str]
    ) -> bool:
        """
        Check if student's response meets the criteria to advance to next stage.
        Returns True if student should advance.
        """
        message_lower = student_message.lower()

        # Simple keyword-based check + length check
        if len(student_message.strip()) < 10:
            return False

        # Check if message is relevant to stage (contains some keywords)
        keyword_matches = sum(1 for kw in stage_keywords if kw.lower() in message_lower)

        # If advance criteria mentions specific things, check for them
        criteria_lower = advance_criteria.lower()

        # Common patterns
        if "introduce" in criteria_lower and any(word in message_lower for word in ["my name", "i'm", "i am", "hello", "hi"]):
            return True

        if "question" in criteria_lower and any(word in message_lower for word in ["?", "what", "how", "when", "where", "why", "could you", "can you"]):
            return True

        if "thanks" in criteria_lower or "thank" in criteria_lower and any(word in message_lower for word in ["thank", "appreciate", "grateful"]):
            return True

        if "solution" in criteria_lower or "offer" in criteria_lower and any(word in message_lower for word in ["we can", "i can", "would", "offer", "provide", "solution"]):
            return True

        if "experience" in criteria_lower and any(word in message_lower for word in ["experience", "worked", "role", "position", "responsible", "managed"]):
            return True

        # If message is substantial and has some keyword overlap, allow advancement
        if len(student_message.split()) >= 15 and keyword_matches >= 1:
            return True

        # Fallback: if message is reasonably long and seems on-topic
        if len(student_message.split()) >= 20:
            return True

        return False

    def generate_hint(
        self,
        stage_hints: List[str],
        hints_used: int,
        student_message: str
    ) -> str:
        """
        Generate a helpful hint for the student.
        Returns progressively more specific hints.
        """
        if hints_used >= len(stage_hints):
            # Out of pre-defined hints, generate a custom one
            return "Try to think about what a professional would say in this situation. Focus on being clear and polite."

        return stage_hints[hints_used]

    def create_mini_drill(
        self,
        error_type: str,
        original: str,
        corrected: str
    ) -> str:
        """
        Create a quick practice exercise based on the error.
        Returns a mini-drill prompt.
        """
        drills = {
            "grammar": f"Quick practice: Try rephrasing this sentence correctly: '{original}'",
            "register": f"Let's practice formality: How would you say this in a more professional way? '{original}'",
            "vocabulary": f"Vocabulary check: What's a better word or phrase than '{original}' in business English?",
            "pragmatic": f"Think about it: How could you rephrase this to be clearer in a business setting?"
        }

        return drills.get(error_type, "Try expressing that idea in a different way.")

# Singleton instance
referee = RoleplayReferee()

