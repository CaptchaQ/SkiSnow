# Contributing to SkiSnow

## Workflow
1. Read `docs/roadmap/full-app-plan.md` and pick the **next open PR step**.
2. Branch from `main`: `feat/…` or `fix/…`.
3. Implement with tests (domain JUnit minimum).
4. Open a PR using the template; fill test plan.
5. Merge only when checks pass; update roadmap checklist.

## Local setup
- JDK 17+ (Temurin 21 OK)
- Android SDK 35
- `local.properties` → `sdk.dir=…`
- Android Studio recommended for UI

```powershell
./gradlew :domain:test :app:assembleDebug
# optional device smoke (USB debugging):
./scripts/device-smoke.ps1
```

## Architecture
See ADRs in `docs/adr/`. Domain stays pure Kotlin. LOCATION/FGS changes need `docs/security/` note.

## Product rules
- Local-first history never paywalled
- No selling track data
- Weather fail must not block recording
