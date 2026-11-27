# OmniDev — DevGate

Android app that acts as a unified developer gateway. The in-app name is **DevGate**. It routes chat and related developer tasks across several AI providers and keeps local history in a Room database.

Providers implemented under `app/src/main/java/com/example/devgate/data/api/`:

- **Gemini** — Google Gemini API (API key)
- **Vertex AI** — Google Cloud Vertex AI
- **Claude** — Anthropic Claude
- **Ollama** — local LLM HTTP API
- **Hermes** — bridge client toward a vertex_orchestrator-style backend (CrewAI + AutoGen + Aider)

The UI includes screens for the dashboard, Git, Gemini CLI, Gemma, Spark, Jules, provider chat, and settings (`app/src/main/java/com/example/devgate/ui/screens/`).

## Quick start

1. Install Android Studio with JDK 17+.
2. Open this repository (Gradle project, `settings.gradle.kts` + `app/` module).
3. Copy `.env.example` to `.env` and set `GEMINI_API_KEY` (leave it unset or commented if you do not want the key packaged into the APK, as noted in `.env.example`).
4. Build a debug APK:

   ```bash
   ./gradlew assembleDebug
   ```

## Configuration

| Name | Purpose |
| --- | --- |
| `GEMINI_API_KEY` | Gemini API access. Documented in `.env.example`. Do not commit the real value. |

Other provider credentials (Claude, Vertex, GitHub, local Ollama/Hermes URLs) are configured in the app settings / provider clients. Keep those values out of git. The network security config allows cleartext to local Ollama and Hermes servers so a device can talk to `localhost` or a LAN endpoint; do not use cleartext against production hosts.

## Project layout

```
app/src/main/java/com/example/devgate/
  data/api/           Provider clients and ProviderRouter
  data/local/         Room database, DAO, entities
  data/models/        Shared data models
  repository/         DevGateRepository
  ui/screens/         Compose screens
  ui/components/      Shared UI
  ui/viewmodel/       DevGateViewModel
app/src/main/java/com/example/ui/theme/
app/src/main/res/     Android resources
.github/workflows/    CI
.env.example          Environment variable template
```

Architecture is MVVM: `DevGateViewModel` + `DevGateRepository`. `ProviderRouter` dispatches chat requests across the five providers. Room stores git repos, commits, snippets, CLI history, Jules tasks, and provider chat history.

## Usage

- Run the app from Android Studio or install the debug APK from `./gradlew assembleDebug`.
- Choose a provider in the UI and configure that provider before sending chat.
- Local Ollama or Hermes must already be running on a URL the device can reach.

## Contributing and testing

GitHub Actions (`.github/workflows/ci-cd.yml`) runs lint and static analysis, unit tests, a debug APK build, a Trivy scan, and a license check.

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Open a pull request against `main`. Do not commit `.env`, API keys, or keystore files.

## License

MIT — see [LICENSE](LICENSE).
