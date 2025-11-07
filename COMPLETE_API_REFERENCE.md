# 📱 COMPLETE API REFERENCE - Android ↔ Server Integration
**Generated:** October 25, 2025  
**Server Port:** 8020  
**Base URL:** `https://YOUR_NGROK_URL` or `http://localhost:8020`

---

## 🔐 AZURE CREDENTIALS IN USE

### 1️⃣ Azure OpenAI - Chat Service (Sweden Central)
```
Endpoint: https://sanja-mh654t02-swedencentral.cognitiveservices.azure.com/
API Key: DuQJzDcQmb9siNeUiiUsgaENRgewWHd9FLM4dJd0FJdQe9SAulRhJQQJ99BJACfhMk5XJ3w3AAAAACOGDSnb
API Version: 2024-12-01-preview
Deployment Name: gpt-35-turbo
Region: swedencentral
```

### 2️⃣ Azure OpenAI - Embeddings Service (UAE North)
```
Endpoint: https://sanja-mh6697hv-uaenorth.cognitiveservices.azure.com/
API Key: 9fyw2LxxdqRgay7cAuK84FXP7TwWMm1HC2QOMy5u5oeKKVt8lyTdJQQJ99BJACF24PCXJ3w3AAAAACOGMrGx
API Version: 2024-02-15-preview
Deployment Name: text-embedding-3-small
Region: uaenorth
```

### 3️⃣ Azure Speech Service (East Asia)
```
API Key: CbZ50wqN8vOc9BwwgUZak4sKkHqtUZSjj31bayNGIVaIn47214zRJQQJ99BJAC3pKaRXJ3w3AAAYACOGKoCE
Region: eastasia
```

---

## 📋 ALL SERVER ENDPOINTS

### ✅ HEALTH & DEBUG ENDPOINTS

#### 1. Health Check
```
GET /health
```
**Response:**
```json
{"status": "nowwww"}
```

#### 2. Version Info
```
GET /version
```
**Response:**
```json
{
  "version": "0.1.0",
  "env": "dev",
  "debug": true
}
```

#### 3. Debug Embedding Test
```
POST /debug/embed
Content-Type: application/json

{
  "text": "Test string"
}
```
**Response:**
```json
{"dim": 1536}
```
**Uses:** Azure Embeddings (UAE North)

#### 4. Debug Vector Search
```
GET /debug/search?q=business+meeting&k=5
```
**Response:**
```json
{
  "items": [
    {
      "score": 0.85,
      "src": "unit_1_page_3",
      "unit": "1",
      "snippet": "..."
    }
  ]
}
```
**Uses:** Azure Embeddings (UAE North)

---

### 💬 CHAT ENDPOINTS

#### 5. Free Chat Mode
```
POST /chat
Content-Type: application/json

{
  "messages": [
    {"role": "user", "content": "How do I write professional emails?"}
  ],
  "k": 5,
  "maxContextChars": 6000,
  "unit": null
}
```
**Response:**
```json
{
  "answer": "To write professional emails...",
  "sources": []
}
```
**Uses:** Azure Chat (Sweden Central) - gpt-35-turbo

**Android DTO:**
```kotlin
data class ChatReqDto(
    val messages: List<ChatMsgDto>,
    val k: Int = 6,
    val maxContextChars: Int = 6000,
    val unit: String? = null
)

data class ChatRespDto(
    @SerialName("message") val message: String? = null,
    @SerialName("answer") val answer: String? = null,
    val sources: List<String> = emptyList()
)
```

---

### 📚 RAG / ASK ENDPOINTS

#### 6. RAG-Based Q&A
```
POST /ask
Content-Type: application/json

{
  "query": "What are stages of a business meeting?",
  "k": 5,
  "max_context_chars": 6000,
  "unit": null
}
```
**Response:**
```json
{
  "answer": "Based on the materials...",
  "sources": ["unit_1_page_3", "unit_2_page_5"]
}
```
**Uses:** 
- Azure Embeddings (UAE North) - for vector search
- Azure Chat (Sweden Central) - for answer generation

**Android DTO:**
```kotlin
data class AskReq(
    val query: String,
    val k: Int = 6,
    val maxContextChars: Int = 1200,
    val unit: String? = null
)

data class AskResp(
    val answer: String,
    val sources: List<String>
)
```

---

### 🎭 ROLEPLAY ENDPOINTS

#### 7. Start Roleplay Session
```
POST /roleplay/start
Content-Type: application/json

{
  "scenario_id": "job_interview",
  "student_name": "John Doe",
  "use_rag": true
}
```
**Response:**
```json
{
  "session_id": "uuid-string",
  "scenario_title": "Job Interview",
  "scenario_description": "Practice interviewing for a job position",
  "context": "You are interviewing for a marketing position...",
  "student_role": "Job Candidate",
  "ai_role": "Hiring Manager",
  "current_stage": "opening",
  "initial_message": "Good morning! Thank you for coming in today..."
}
```
**Uses:** Azure Chat (Sweden Central) - gpt-35-turbo

**Android DTO:**
```kotlin
data class RoleplayStartReqDto(
    @SerialName("scenario_id") val scenarioId: String,
    @SerialName("student_name") val studentName: String = "Student",
    @SerialName("use_rag") val useRag: Boolean = true
)

data class RoleplayStartRespDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("scenario_title") val scenarioTitle: String,
    @SerialName("scenario_description") val scenarioDescription: String,
    val context: String,
    @SerialName("student_role") val studentRole: String,
    @SerialName("ai_role") val aiRole: String,
    @SerialName("current_stage") val currentStage: String = "opening",
    @SerialName("initial_message") val initialMessage: String? = null
)
```

**Available Scenarios:**
- `job_interview` - Job Interview
- `client_meeting` - Client Meeting  
- `customer_complaint` - Customer Complaint
- `team_meeting` - Team Meeting
- `business_phone_call` - Business Phone Call

#### 8. Submit Roleplay Turn
```
POST /roleplay/turn
Content-Type: application/json

{
  "session_id": "uuid-from-start",
  "message": "Hello, I'm excited for this opportunity"
}
```
**Response (No Errors):**
```json
{
  "ai_message": "That's wonderful! Tell me about your background.",
  "correction": {
    "has_errors": false,
    "errors": [],
    "feedback": null
  },
  "current_stage": "opening",
  "is_completed": false,
  "feedback": null
}
```

**Response (With Errors):**
```json
{
  "ai_message": "I see. Could you elaborate on that?",
  "correction": {
    "has_errors": true,
    "errors": [
      {
        "type": "register",
        "incorrect": "gonna",
        "correct": "going to",
        "explanation": "Use 'going to' in professional settings"
      }
    ],
    "feedback": "Priority: high. Keep practicing!"
  },
  "current_stage": "opening",
  "is_completed": false,
  "feedback": null
}
```
**Uses:** 
- Azure Chat (Sweden Central) - for AI response and error analysis

**Android DTO:**
```kotlin
data class RoleplayTurnReqDto(
    @SerialName("session_id") val sessionId: String,
    val message: String
)

data class ErrorDetailDto(
    val type: String,
    val incorrect: String,
    val correct: String,
    val explanation: String
)

data class CorrectionDto(
    @SerialName("has_errors") val hasErrors: Boolean = false,
    val errors: List<ErrorDetailDto>? = null,
    val feedback: String? = null
)

data class RoleplayTurnRespDto(
    @SerialName("ai_message") val aiMessage: String,
    val correction: CorrectionDto? = null,
    @SerialName("current_stage") val currentStage: String = "development",
    @SerialName("is_completed") val isCompleted: Boolean = false,
    val feedback: String? = null
)
```

**Error Types:**
- `grammar` - Grammar mistakes (tenses, articles, etc.)
- `register` - Too casual/formal for business context
- `vocabulary` - Wrong word choice
- `pragmatic` - Culturally inappropriate or unclear

#### 9. List Active Sessions
```
GET /roleplay/sessions
```
**Response:**
```json
{
  "active_sessions": 2,
  "sessions": [
    {
      "id": "abc12345...",
      "scenario": "job_interview",
      "turns": 5,
      "stage": "development"
    }
  ]
}
```

#### 10. Delete Session
```
DELETE /roleplay/session/{session_id}
```
**Response:**
```json
{"status": "deleted"}
```

---

### 🎤 PRONUNCIATION ENDPOINTS

#### 11. Pronunciation Assessment (Detailed)
```
POST /pronunciation/assess
Content-Type: multipart/form-data

audio: <WAV file>
reference_text: "Good morning, I would like to schedule a meeting"
```
**Response:**
```json
{
  "transcript": "Good morning I would like to schedule a meeting",
  "accuracy_score": 85.5,
  "fluency_score": 78.2,
  "completeness_score": 95.0,
  "pronunciation_score": 82.3,
  "words": [
    {
      "word": "Good",
      "accuracy_score": 95.0,
      "error_type": null
    },
    {
      "word": "morning",
      "accuracy_score": 55.0,
      "error_type": "Mispronunciation"
    }
  ],
  "feedback": "Good pronunciation! Keep practicing. Words to practice: 'morning'."
}
```
**Uses:** Azure Speech Service (East Asia)

**Android DTO:**
```kotlin
data class PronunciationResultDto(
    val transcript: String,
    val accuracy_score: Float,
    val fluency_score: Float,
    val completeness_score: Float,
    val pronunciation_score: Float,
    val words: List<PronunciationWordDto>,
    val feedback: String
)

data class PronunciationWordDto(
    val word: String,
    val accuracy_score: Float,
    val error_type: String? = null
)
```

#### 12. Quick Pronunciation Check
```
POST /pronunciation/quick-check
Content-Type: multipart/form-data

audio: <WAV file>
reference_text: "Hello"
```
**Response:**
```json
{
  "score": 82.3,
  "feedback": "Good pronunciation!",
  "transcript": "Hello",
  "needs_practice": false,
  "mispronounced_words": []
}
```
**Uses:** Azure Speech Service (East Asia)

#### 13. Test Pronunciation Service
```
GET /pronunciation/test
```
**Response:**
```json
{
  "status": "ok",
  "region": "eastasia",
  "service": "Azure Speech Service",
  "features": [
    "Pronunciation Assessment",
    "Word-level scoring",
    "Fluency analysis",
    "Accuracy measurement"
  ]
}
```

---

### 🎙️ SPEECH ENDPOINTS

#### 14. Speech-to-Text (STT)
```
POST /stt
Content-Type: multipart/form-data

file: <audio file>
```
**Response:**
```json
{
  "text": "Hello, this is a test of speech to text"
}
```
**Uses:** OpenAI Whisper (whisper-1) - NOT Azure

**Android DTO:**
```kotlin
data class STTResponse(
    val text: String
)
```

#### 15. Text-to-Speech (TTS)
```
POST /tts
Content-Type: application/x-www-form-urlencoded

text=Hello, this is a test
```
**Response:** Audio/MP3 file
**Content-Type:** audio/mpeg

**Uses:** OpenAI TTS (tts-1) - NOT Azure

---

## 📊 MODEL USAGE SUMMARY

| Endpoint | Azure Service | Model/Deployment | Region |
|----------|--------------|------------------|--------|
| `/chat` | Azure OpenAI | gpt-35-turbo | Sweden Central |
| `/ask` (embeddings) | Azure OpenAI | text-embedding-3-small | UAE North |
| `/ask` (chat) | Azure OpenAI | gpt-35-turbo | Sweden Central |
| `/roleplay/*` | Azure OpenAI | gpt-35-turbo | Sweden Central |
| `/pronunciation/*` | Azure Speech | N/A | East Asia |
| `/stt` | OpenAI | whisper-1 | N/A |
| `/tts` | OpenAI | tts-1 | N/A |

---

## 🔧 ANDROID INTEGRATION CODE

### NetworkModule.kt Configuration
```kotlin
@Provides @Singleton
fun provideBaseUrl(): String {
    val useLocalhost = false  // Set to false for APK distribution
    
    val PRODUCTION_SERVER_IP = "your-ngrok-url.ngrok-free.dev"
    val SERVER_PORT = ""
    val USE_HTTPS = true
    
    val serverUrl = if (useLocalhost) {
        "http://localhost:8020"
    } else {
        if (SERVER_PORT.isEmpty()) {
            val protocol = if (USE_HTTPS) "https" else "http"
            "$protocol://$PRODUCTION_SERVER_IP"
        } else {
            "http://$PRODUCTION_SERVER_IP:$SERVER_PORT"
        }
    }
    
    return serverUrl
}
```

### Retrofit/Ktor API Interfaces
```kotlin
// ChatApi.kt
class ChatApi(private val baseUrl: String) {
    suspend fun chat(req: ChatReqDto): ChatRespDto {
        val resp = client.post("$baseUrl/chat") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return Json.decodeFromString<ChatRespDto>(resp.bodyAsText())
    }
}

// RoleplayApi.kt
class RoleplayApi(private val baseUrl: String) {
    suspend fun startRoleplay(scenarioId: String, useRag: Boolean): RoleplayStartRespDto {
        val req = RoleplayStartReqDto(scenarioId, "Student", useRag)
        val resp = client.post("$baseUrl/roleplay/start") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return Json.decodeFromString<RoleplayStartRespDto>(resp.bodyAsText())
    }
    
    suspend fun submitTurn(sessionId: String, message: String): RoleplayTurnRespDto {
        val req = RoleplayTurnReqDto(sessionId, message)
        val resp = client.post("$baseUrl/roleplay/turn") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return Json.decodeFromString<RoleplayTurnRespDto>(resp.bodyAsText())
    }
}

// PronunciationApi.kt
class PronunciationApi(private val client: HttpClient, private val baseUrl: String) {
    suspend fun assessPronunciation(audioFile: File, referenceText: String): PronunciationResultDto {
        val response = client.submitFormWithBinaryData(
            url = "$baseUrl/pronunciation/assess",
            formData = formData {
                append("reference_text", referenceText)
                append("audio", audioFile.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "audio/wav")
                    append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                })
            }
        )
        return Json.decodeFromString(response.body<String>())
    }
}
```

---

## 🧪 TESTING COMMANDS

### Test All Endpoints
```bash
# Health check
curl http://localhost:8020/health

# Version
curl http://localhost:8020/version

# Chat
curl -X POST http://localhost:8020/chat \
  -H "Content-Type: application/json" \
  -d '{"messages":[{"role":"user","content":"Hello"}]}'

# Ask
curl -X POST http://localhost:8020/ask \
  -H "Content-Type: application/json" \
  -d '{"query":"What is business English?","k":5}'

# Start roleplay
curl -X POST http://localhost:8020/roleplay/start \
  -H "Content-Type: application/json" \
  -d '{"scenario_id":"job_interview","student_name":"Test","use_rag":true}'

# Submit turn (use session_id from above)
curl -X POST http://localhost:8020/roleplay/turn \
  -H "Content-Type: application/json" \
  -d '{"session_id":"YOUR_SESSION_ID","message":"Hello"}'

# List sessions
curl http://localhost:8020/roleplay/sessions

# Pronunciation test
curl http://localhost:8020/pronunciation/test

# Test embedding
curl -X POST http://localhost:8020/debug/embed \
  -H "Content-Type: application/json" \
  -d '{"text":"test"}'

# Search test
curl "http://localhost:8020/debug/search?q=business&k=3"
```

---

## ⚠️ COMMON ERRORS & SOLUTIONS

| Error | Cause | Solution |
|-------|-------|----------|
| HTTP 500: Model not found | Wrong deployment name | Use `gpt-35-turbo` not `gpt-3.5-turbo` |
| HTTP 404: Endpoint not found | Missing roleplay_api.py | Copy roleplay_api.py and roleplay_engine.py to server folder |
| HTTP 404: Ngrok offline | Ngrok expired (2hr limit) | Restart ngrok, update Android URL |
| Correction not showing | Format mismatch | ✅ Fixed - server converts to Android format |
| ENOENT file error | Cache dir not created | ✅ Fixed - Android creates cache dir |
| Empty response | Server crash | Check server logs, verify Azure keys |

---

## 🚀 DEPLOYMENT CHECKLIST

### Server Side:
- [ ] All Azure credentials in settings.py
- [ ] roleplay_api.py copied to server folder
- [ ] roleplay_engine.py copied to server folder
- [ ] Server running: `uvicorn app:app --host 0.0.0.0 --port 8020 --reload`
- [ ] Ngrok running: `ngrok http 8020`
- [ ] All endpoints tested with curl

### Android Side:
- [ ] NetworkModule.kt updated with ngrok URL
- [ ] All DTOs match server response format
- [ ] Azure Speech Service key matches server
- [ ] Build successful: `gradlew.bat assembleDebug`
- [ ] APK tested on device

---

## 📞 SUPPORT CONTACTS

**Azure Services:** https://portal.azure.com  
**Ngrok Dashboard:** https://dashboard.ngrok.com  
**Server Logs:** Check terminal where `uvicorn` is running  
**Android Logs:** `adb logcat | findstr "CHAT|ROLEPLAY|PRONUNCIATION"`

---

**Last Updated:** October 25, 2025  
**API Version:** 4.0  
**Status:** ✅ Production Ready

