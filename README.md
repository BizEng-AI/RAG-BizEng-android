# 🚀 Android Client - Business English RAG Application

**Organization:** BizEng-AI  
**Repository:** Android Client  
**Purpose:** Mobile application for business English learning with RAG (Retrieval-Augmented Generation), chat, roleplay, and pronunciation features

---

## 📱 Features

### 1. **Chat & RAG Integration** 💬
- **Free Chat (`/chat`)** - Conversational practice with AI
- **Grounded Q&A (`/ask`)** - Questions answered using course materials (RAG)
- Full conversation history support
- Real-time message streaming

### 2. **Roleplay Practice** 🎭
- Interactive business scenarios
- AI referee provides feedback
- Score-based progression
- Real-time conversation with role-specific context

### 3. **Pronunciation Assessment** 🎤
- WAV file upload for pronunciation analysis
- Phoneme-level scoring
- IPA (International Phonetic Alphabet) transcription
- Detailed feedback with improvement tips
- Fluency, accuracy, and completeness scoring

### 4. **Voice Features** 🔊
- Speech-to-text (STT) for input
- Text-to-speech (TTS) for pronunciation guidance
- Real-time audio processing

---

## 🏗️ Architecture

```
app/src/main/java/com/example/myapplication/
├── App.kt                          # Main application class
├── MainActivity.kt                 # Entry point
├── core/                           # Utilities & infrastructure
│   ├── network/                    # HTTP client setup
│   └── util/                       # Helper functions
├── data/                           # Data layer
│   ├── remote/                     # API clients (Ktor/Retrofit)
│   │   ├── ChatApi.kt
│   │   ├── AskApi.kt
│   │   ├── RoleplayApi.kt
│   │   └── PronunciationApi.kt
│   ├── dto/                        # Data transfer objects
│   └── repository/                 # Repository implementations
├── di/                             # Dependency injection (Hilt)
│   ├── NetworkModule.kt            # HTTP client & base URL
│   ├── ApiModule.kt                # API providers
│   └── RepositoryModule.kt         # Repository bindings
├── domain/                         # Domain layer
│   ├── models/                     # Business logic models
│   └── repository/                 # Repository interfaces
└── uiPack/                         # Presentation layer (Jetpack Compose)
    ├── navigation/                 # Navigation routing
    ├── chat/                       # Chat feature
    ├── roleplay/                   # Roleplay feature
    ├── pronunciation/              # Pronunciation feature
    └── shared/                     # Shared UI components
```

---

## 🔌 API Integration

### Base URL
Configured via `local.properties`:
```properties
SERVER_URL=https://your-server-url.com
```

### Endpoints Used

| Endpoint | Method | Purpose | DTO |
|----------|--------|---------|-----|
| `/chat` | POST | Free conversation | ChatReqDto → ChatRespDto |
| `/ask` | POST | RAG-based Q&A | AskReqDto → AskRespDto |
| `/roleplay/start` | POST | Start roleplay | RoleplayReqDto → RoleplayRespDto |
| `/roleplay/turn` | POST | Roleplay turn | RoleplayTurnReqDto → RoleplayTurnRespDto |
| `/pronunciation/assess` | POST (multipart) | Assess pronunciation | WAV file → PronunciationResultDto |

---

## 🔄 Data Flow

### Chat Feature
```
User Input (UI)
    ↓
ChatViewModel: sendMessage()
    ↓
RagRepository: chatFree(messages)
    ↓
ChatApi: POST /chat {messages: [...]}
    ↓
Response Processing: ChatRespDto
    ↓
UI Update: Display answer
```

### Pronunciation Feature
```
User Speaks (Voice Recording)
    ↓
RecorderService: Capture audio as WAV
    ↓
PronunciationViewModel: assessPronunciation()
    ↓
PronunciationApi: POST /pronunciation/assess (multipart)
    ↓
Response Processing: PronunciationResultDto
    ↓
UI Update: Show scores, feedback, phonemes, IPA
```

---

## 📊 Key DTOs

### Chat Request
```kotlin
@Serializable
data class ChatReqDto(
    val messages: List<ChatMessage>
)

@Serializable
data class ChatMessage(
    val role: String,      // "user", "assistant", or "system"
    val content: String
)
```

### Pronunciation Response
```kotlin
@Serializable
data class PronunciationResultDto(
    val transcript: String,
    val accuracy_score: Float,
    val fluency_score: Float,
    val completeness_score: Float,
    val pronunciation_score: Float,
    val words: List<PronunciationWordDto>,
    val feedback: String,
    val detailed_feedback: List<String>
)

@Serializable
data class PronunciationWordDto(
    val word: String,
    val accuracy_score: Float,
    val error_type: String?,
    val feedback: String?,
    val ipa_expected: String?,    // IPA transcription
    val phonemes: List<PronunciationPhonemeDto>?
)

@Serializable
data class PronunciationPhonemeDto(
    val phoneme: String,  // IPA symbol
    val score: Float
)
```

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **HTTP Client:** Ktor
- **Serialization:** kotlinx.serialization
- **Dependency Injection:** Hilt
- **State Management:** StateFlow, ViewModel
- **Audio:** MediaRecorder, TextToSpeech, STT
- **Build System:** Gradle (Kotlin DSL)

---

## 📋 Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+ (API level)
- Gradle 8.0+

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/BizEng-AI/android.git
   cd android
   ```

2. Create `local.properties`:
   ```properties
   sdk.dir=/path/to/android/sdk
   SERVER_URL=https://your-server-url.com
   ```

3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

4. Deploy to device/emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🧪 Testing

### Manual Testing Checklist
- [ ] Chat feature sends/receives messages correctly
- [ ] Grounded mode toggles between `/chat` and `/ask`
- [ ] Roleplay starts and accepts turns
- [ ] Pronunciation assessment completes with feedback
- [ ] IPA transcription displays correctly
- [ ] Voice input/output works on target device

### Running Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest   # Integration tests
```

---

## 🐛 Debugging

### Enable Logging
The app includes HTTP logging interceptors. Check Logcat for:
```
CHAT - Main chat flow logs
NETWORK - HTTP request/response details
ROLEPLAY - Roleplay feature logs
PRONUNCIATION - Pronunciation logs
```

### Common Issues

**500 Error in Chat:**
- Ensure `role` field is exactly "user", "assistant", or "system"
- Don't mix `/chat` and `/ask` - they're different endpoints
- Verify content filter isn't blocking your message

**Pronunciation Returns Empty:**
- Check that WAV file is properly encoded
- Verify `reference_text` is provided
- Check Azure content filter isn't blocking

**IPA Symbols Not Rendering:**
- Ensure device supports Unicode
- Consider adding Noto Sans font to project
- Test with common IPA: θ, ð, ʃ, ŋ, ɔː

---

## 📚 Documentation

- See `CLIENT_ARCHITECTURE.md` for detailed module breakdown
- See `COMPLETE_API_REFERENCE.md` for all endpoint specs
- See pronunciation guides for IPA symbol reference

---

## 🔐 Configuration

### Server Connection
- Base URL set in `local.properties` (development) or BuildConfig (production)
- HTTPS required for production
- Supports ngrok tunnels for local development

### Security Notes
- Sensitive data (tokens, API keys) stored securely
- Never commit `local.properties` with real credentials
- Use environment variables for CI/CD

---

## 📝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit pull request to `main`

---

## 📄 License

This project is proprietary to BizEng-AI.

---

## 👥 Team

**BizEng-AI Organization**
- Backend Team
- Android Development Team
- QA Team

---

## 🚀 Version History

| Version | Date | Notes |
|---------|------|-------|
| 1.0.0 | 2025-11-07 | Initial release with Chat, Roleplay, Pronunciation |
| | | Full IPA transcription support |
| | | Phoneme-level analysis |

---

**Last Updated:** November 7, 2025  
**Repository:** https://github.com/BizEng-AI/android

