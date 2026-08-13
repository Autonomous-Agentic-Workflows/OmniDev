# OmniDev — DevGate

Unified Developer Gateway for Android, integrating multiple AI providers:

- **Gemini** — Google Gemini API (API key based)
- **Vertex AI** — Google Cloud enterprise AI (gCloud ADC, IP-protected)
- **Claude** — Anthropic Claude reasoning engine
- **Ollama** — Local LLM inference (llama3, gemma2, mistral, codellama)
- **Hermes** — Bridge to vertex_orchestrator backend (CrewAI + AutoGen + Aider)

## Architecture

Built with Kotlin, Jetpack Compose, Material 3, Room database, and Retrofit.

- MVVM architecture with `DevGateViewModel` + `DevGateRepository`
- `ProviderRouter` dispatches chat requests across all 5 providers
- Room database persists git repos, commits, snippets, CLI history, Jules tasks, and provider chat history
- Network security config allows cleartext to local Ollama/Hermes servers

## Building

1. Install Android Studio with JDK 17+
2. Open this repository
3. Configure API keys in `.env` (copy from `.env.example`)
4. Build debug APK: `./gradlew assembleDebug`

## CI/CD

GitHub Actions workflow at `.github/workflows/ci-cd.yml` runs:
- Lint & static analysis
- Unit tests
- Debug APK build
- Trivy security scan
- License compliance check

## License

MIT — see [LICENSE](LICENSE)