# Android Client Architecture (RAG + Chat + Roleplay + Pronunciation)

Last updated: 2025-10-29

This document summarizes the client-side structure, data flow, and key DTOs/endpoints so you can share a complete picture of the app with other tools or teammates.

---

## High-level Modules

- app/src/main/java/com/example/myapplication/
  - App.kt, MainActivity.kt
  - core/ (network, util)
  - data/ (remote APIs, DTOs, repository implementation)
  - di/ (Hilt modules: base URL, HttpClient, API providers, repository)
  - domain/ (models, repository interface, use cases)
  - uiPack/ (feature UI + VMs): chat, roleplay, pronunciation, navigation
  - voice/ (Speech-to-text, Text-to-speech, recording)

---

## Dependency Graph (simplified)

UI (Compose) → ViewModel (Hilt) → Domain Repository (interface) → Data Repository Impl → Remote APIs (Ktor)

- Base URL and HttpClient are provided via Hilt (NetworkModule, ApiModule).
- DTOs use kotlinx.serialization; APIs decode JSON manually for better logs.

---

## Navigation

- uiPack/navigation/MainNavigation.kt
  - Bottom tabs: Chat, Roleplay, Pronunciation
  - Each tab obtains its ViewModel via hiltViewModel()

---

## Chat feature (/chat for free chat; /ask for grounded RAG)

ViewModel: uiPack/chat/ChatVm.kt
UI: uiPack/chat/ChatUI.kt
API: data/remote/ChatApi.kt and data/remote/AskApi.kt
DTOs: data/remote/dto/ChatReqDto.kt, ChatMsgDto.kt, ChatRespDto.kt, AskDtos.kt
Repository methods: RagRepository.chatFree(), RagRepository.askGrounded()

- ChatVm has a switch state.grounded:
  - grounded=true → calls repo.askGrounded(query), hitting /ask (RAG)
  - grounded=false → builds messages history and calls repo.chatFree(messages), hitting /chat
- For /chat:
  - Request: { "messages": [{"role":"user|assistant|system","content":"..."}, ...] }
  - Response: { "answer": "...", "sources": [] }
- For /ask (RAG):
  - Request: { "query": "...", "k": number, "unit": string?, "max_context_chars": number }
  - Response: { "answer": "...", "sources": ["..."] }
- Streaming: ChatApi.streamChat supports SSE at /chat/stream and emits deltas via onDelta callback.

Common pitfalls (handled/logged):
- Wrong role field name/value causes 500 on server; only user/assistant/system are valid.
- Don’t send AskReqDto to /chat; use ChatReqDto with List<ChatMsgDto>.

---

## Roleplay feature

ViewModel: uiPack/roleplay/RoleplayVm.kt
API: data/remote/RoleplayApi.kt
DTOs: data/remote/dto/RoleplayMessageDto.kt and related
Repository: startRoleplay(), submitRoleplayTurn()

- startRoleplay(scenarioId, useRag) POST /roleplay/start
- submitTurn(sessionId, message) POST /roleplay/turn

---

## Pronunciation feature (Enhanced IPA + phoneme-level)

ViewModel: uiPack/pronunciation/PronunciationVm.kt
UI: uiPack/pronunciation/PronunciationUI.kt (enhanced components)
API: data/remote/PronunciationApi.kt
DTOs: data/remote/dto/PronunciationDto.kt

Endpoints:
- POST /pronunciation/assess (multipart/form-data: audio WAV + reference_text)
- POST /pronunciation/quick-check (multipart/form-data)
- GET /pronunciation/test (optional health)

Response PronunciationResultDto contains:
- transcript, pronunciationScore, accuracyScore, fluencyScore, completenessScore
- words: List<PronunciationWordDto> with ipaExpected, ipaActual?, phonemes[]
- feedback, detailedFeedback?

UI shows:
- Overall score and breakdown
- Detailed feedback (collapsible)
- Word-by-word cards with IPA and phoneme chips

---

## DI wiring (Hilt)

- di/NetworkModule.kt → provideBaseUrl(), provideHttpClient()
- di/ApiModule.kt → provide AskApi, ChatApi, RoleplayApi, PronunciationApi
- di/RepoModule.kt → provide RagRepository (RagRepositoryImpl)

---

## Domain layer

- domain/repository/RagRepository.kt defines the contract
- data/repository/RagRepositoryImpl.kt implements via APIs

---

## Voice layer

- voice/SpeechToTextController: mic input → text
- voice/TextToSpeechController: speak assistant messages/phrases
- voice/AudioRecorder: record WAV files for pronunciation

---

## Logging & Troubleshooting

- NETWORK_CONFIG: logs current base URL and mode (dev/prod)
- CHAT/CHAT_API: logs message flow and endpoints (/chat vs /ask)
- PRONUNCIATION_API & PRONUNCIATION: detailed lifecycle logs
- ROLEPLAY_API: start/turn request logs

To enable verbose HTTP logging for Ktor, plug a logging engine or wrap requests with additional Log.d entries (already present in APIs). If you use OkHttp-based clients elsewhere, add HttpLoggingInterceptor at BODY level.

---

## Build notes

- Kotlinx Serialization is used across DTOs; field names must match server JSON (use @SerialName where needed).
- Compose experimental APIs are gated via @OptIn annotations (e.g., ExperimentalLayoutApi for FlowRow).
- For APK distribution, NetworkModule.useLocalhost=false and baseUrl is set to your ngrok or public server URL.

---

## Known integration gotchas

- Chat roles must be exactly: user, assistant, system
- Don’t send AskReqDto to /chat; don’t send ChatReqDto to /ask
- For pronunciation, ensure audio file is non-empty (>1KB) before upload
- When grounded=true, Azure content filters can block certain prompts; server now handles remediations. Toggle off (free chat) to test /chat format.

---

## Quick data shapes (DTOs)

- ChatMsgDto: { role: String, content: String }
- ChatReqDto: { messages: List<ChatMsgDto> }
- ChatRespDto: { answer: String, sources: List<String> }
- AskReqDto: { query: String, k: Int, max_context_chars: Int, unit: String? }
- AskRespDto: { answer: String, sources: List<String> }
- PronunciationResultDto: transcript, pronunciation_score, accuracy_score, fluency_score, completeness_score, feedback, detailed_feedback?, words[{ word, accuracy_score, error_type?, feedback?, ipa_expected?, ipa_actual?, phonemes[{ phoneme, score }] }]

---

## End-to-end flows

- Chat (grounded=false): ChatUI → ChatVm.buildHistory → RagRepository.chatFree → ChatApi.chat(POST /chat) → answer shown & TTS option
- Chat (grounded=true): ChatUI → ChatVm → RagRepository.askGrounded → AskApi.ask(POST /ask) → answer shown & TTS option
- Roleplay: RoleplayUI → RoleplayVm.start/turn → RagRepository.startRoleplay/submitTurn → RoleplayApi
- Pronunciation: PronunciationUI → record WAV → PronunciationVm.assessRecording → RagRepository.assessPronunciation → PronunciationApi (multipart) → UI renders enhanced results

---

This file is meant as a shareable map of the client. If you want a diagram (Mermaid), we can add one next.

