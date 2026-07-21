## Summary
- New `Settings` screen: units (metric/imperial) RadioGroup, Privacy, About
- DataStore `SettingsRepository` (`UserSettings`, `Units`) under a port
- Pure `UnitConverter` (SI in domain; UI converts) with unit tests
- NavHost route `settings`; gear icon from Session; live stats + labels respect units
- DataStore dep added; Koin wired

## Linked plan step
- [x] Roadmap item: `docs/roadmap/full-app-plan.md` PR#3
- Closes #2

## Type
- [x] feat
- [x] test

## Changes
- `domain`: `Units`, `UserSettings`, `UnitConverter`, `SettingsRepository`; `UnitConverterTest`
- `data`: `DataStoreSettingsRepository`, `data/build.gradle.kts` datastore dep, `DataModule` binding
- `presentation`: `SettingsViewModel`, `SettingsScreen`, NavHost `settings`, gear icon, `SessionScreen` units-aware stats + duplicate `auwhere` cleanup

## Test plan
- [x] `./gradlew :domain:test`
- [x] `./gradlew :app:compileDebugKotlin`
- [x] Device smoke `scripts/device-smoke.ps1` — PASS (Start/Pause/Resume/Stop intact)
- [ ] Manual: Settings → Imperial → back → Max label «mph»

## Security
- [x] N/A

## Merge checklist
- [x] No secrets
- [x] Roadmap updated after merge