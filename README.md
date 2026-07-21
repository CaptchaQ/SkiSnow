# SkiSnow

<p align="center">
  <strong>Alpine ski & freeride tracker for Android</strong><br/>
  Live map · GPS day record · elevation & speed · mountain weather<br/>
  <em>Local-first history — never paywalled</em>
</p>

<p align="center">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white"/>
  <img alt="Compose" src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12-4285F4?logo=jetpackcompose&logoColor=white"/>
  <img alt="MapLibre" src="https://img.shields.io/badge/Map-MapLibre-00B4D8"/>
  <img alt="License" src="https://img.shields.io/badge/License-MIT-green"/>
</p>

---

## What it is

SkiSnow is a **native Android** app for resort skiers and freeriders:

| | |
|--|--|
| **Track** | Pocket GPS session with Foreground Service, Start / Pause / Stop |
| **Map** | Live 2D MapLibre polyline + user position (3D/hillshade planned) |
| **Stats** | Distance, max/avg speed, vertical drop, elevation profile (detail next) |
| **Weather** | Open-Meteo mountain vars (temp, snow, wind, freezing level) |
| **History** | Offline Room storage — **never locked behind a subscription** |

Inspired by lessons from Slopes / Ski Tracks / bergfex / OpenSnow / onX — see [competitive research](docs/product/competitive-research.md).

---

## Screens (current → planned)

| Now (slice-0) | Next |
|---------------|------|
| Session + live map + weather chip | Session detail + elevation chart |
| History list | Settings (units), offline packs |
| | Heatmap, hillshade, slope angle, GPX |

Full roadmap: **[docs/roadmap/full-app-plan.md](docs/roadmap/full-app-plan.md)**

---

## Stack

| Layer | Choice | ADR |
|-------|--------|-----|
| Language | Kotlin | — |
| UI | Jetpack Compose | — |
| Architecture | Clean modules (`app`, `domain`, `data`, `presentation`) | [0005](docs/adr/0005-module-graph.md) |
| DI | Koin | — |
| Storage | Room | [0003](docs/adr/0003-storage.md) |
| Map | MapLibre Native | [0001](docs/adr/0001-map-sdk.md) |
| Location | Fused + FGS | [0002](docs/adr/0002-location.md) |
| Weather | Open-Meteo | [0004](docs/adr/0004-weather.md) |
| 3D path | Hillshade first | [0006](docs/adr/0006-3d-terrain.md) |

**Pins:** AGP `8.7.2`, Kotlin `2.0.21`, Compose BOM `2024.12.01`, `minSdk 26`, `targetSdk` 35, `applicationId` `com.skisnow.app`.

---

## Quick start

### Requirements
- JDK 17+ (Temurin 21 OK)
- Android SDK platform 35
- Android Studio or cmdline-tools

### Build

```powershell
# local.properties (gitignored) — Android Studio creates this on open:
# sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

./gradlew :domain:test
./gradlew :app:assembleDebug
```

### Device smoke (USB debugging)

```powershell
./scripts/device-smoke.ps1
```

Grants location/notifications, installs debug APK, drives Start → Pause → Resume → Stop via UI Automator.

### Security (FGS)

See [docs/security/tracking-fgs-checklist.md](docs/security/tracking-fgs-checklist.md).

---

## Product docs

| Doc | Role |
|-----|------|
| [product-brief.md](docs/product/product-brief.md) | Who / MVP / ICE |
| [skisnow-intent.md](docs/product/skisnow-intent.md) | Phased capabilities |
| [capability-plan.md](docs/product/capability-plan.md) | Engineering contract |
| [competitive-research.md](docs/product/competitive-research.md) | Market matrix |
| [stats-spec.md](docs/product/stats-spec.md) | Distance / vertical algorithms |
| [full-app-plan.md](docs/roadmap/full-app-plan.md) | **PR pipeline + screens** |
| [docs/adr/](docs/adr/) | Architecture decisions |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Work follows the roadmap PR table — one vertical slice per PR, tests green before merge.

Agent harness notes: [AGENTS.md](AGENTS.md), [CLAUDE.md](CLAUDE.md).

### Project-local skills
83 vendored skills (ECC + Stitch + Emil) under `.claude/skills/` and `.agents/skills/`. Refresh: `./scripts/vendor-all-skills.ps1`.

---

## License

[MIT](LICENSE)
