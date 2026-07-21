## Summary
- Add Session detail screen reachable from History rows
- Add elevation profile chart (domain `StatsCalculator.elevationProfile` + Compose Canvas)
- NavHost: Session -> Detail/{session_id}
- Runnable on device; navigation parameter wired into Koin ViewModel

## Linked plan step
- [x] Roadmap item: `docs/roadmap/full-app-plan.md` PR#2
- Closes #1

## Type
- [x] feat
- [x] test

## Changes
- `domain`: `StatsCalculator.elevationProfile()` + downsample; unit tests
- `presentation`: `SessionDetailScreen`, `SessionDetailViewModel`, NavHost, history rows clickable
- `gradle/libs.versions.toml`: nav-compose + material-icons

## Test plan
- [x] `./gradlew :domain:test`
- [x] `./gradlew :app:compileDebugKotlin`
- [x] Device smoke `scripts/device-smoke.ps1` (session Start/Pause/Resume/Stop still PASS)
- [ ] Manual: tap a History row -> detail screen -> elevation chart

## Security
- [x] N/A (no permission/FGS changes)

## Merge checklist
- [x] No secrets committed
- [x] AGENTS.md no change needed
- [x] Roadmap checkbox updated to done