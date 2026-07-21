# SkiSnow — Product Intent

**Status:** expanded from competitive research + product brief (2026-07-21)  
**Sources:** `docs/product/competitive-research.md`, `docs/product/product-brief.md`, `docs/product/capability-plan.md`

---

## One-liner

Android app for alpine skiing & freeride: **track the ski day**, show route on **2D/3D map** with freeride terrain context, summarize speed/vertical/distance, and surface **mountain weather** — local-first, history never paywalled.

---

## Target user

Skier / freerider / snowboarder on Android; phone often in pocket; cold hands; intermittent connectivity; wants one app instead of tracker + map + weather stack.

---

## Core capabilities (phased)

### P0 — MVP (prove thesis)
1. Start / pause / stop ski-day session with continuous GPS (Foreground Service when LOCATION lane opens).
2. Live **2D map**: user position + track polyline.
3. Per-session stats: duration, distance, max/avg moving speed, vertical drop.
4. Elevation profile chart for the session.
5. Session history list + detail replay on map.
6. Local-first storage; history readable offline forever (no paywall).
7. Basic mountain weather for current location (hourly snow, wind, freezing level, temperature).

### P1
8. Speed heatmap / timeline day replay.
9. Offline basemap package for a selected region.
10. Richer weather cache + region selection UX.

### P2
11. Terrain path: DEM **hillshade** → optional pitch/exaggeration (true 3D camera if MapLibre Android supports needs).
12. Slope-angle freeride layer.
13. Auto run vs lift segmentation.

### P3
14. Avalanche bulletin deep-link / region layer (not primary forecast authority).
15. GPX/FIT export; optional friends live share.
16. Official resort trail maps only via partnership / licensed data (not v1 catalog).

---

## Non-goals (v0–v1)

- Social feed, public leaderboards, coaching AI
- Resort commerce (tickets, lodging)
- iOS (unless later KMP)
- Backend accounts as requirement for core value
- Hardware boot sensors (Carv-class)
- Full avalanche authority product
- Global proprietary trail-map catalog like Slopes
- Selling track / location data
- Locking local history behind subscription

---

## Trust & product rules

1. **Local-first:** core tracking and history work without account or network.
2. **Never paywall local history** (Ski Tracks lesson — see competitive research).
3. Weather is optional network enrichment; stale/cached OK with error chip.
4. LOCATION / map / FGS **lane open** after ADR accept (2026-07-21); first tracking PR still requires `security-review`.

### Locked prefs (user 2026-07-21)
- Persona: resort + freeride equally
- First code slice: session + **live 2D map**
- Record UX: Start + Pause (+ Stop)
- Weather: Open-Meteo thin chip in first release
- Region: global basemap (no exclusive geo)
- Units: UI switcher, **default metric** (domain SI)

---

## Decisions → ADRs (accepted)

| Topic | ADR | Decision |
|-------|-----|----------|
| Map SDK | [0001](../adr/0001-map-sdk.md) | MapLibre Native Android |
| Location | [0002](../adr/0002-location.md) | Fused Location + FGS while Recording |
| Storage | [0003](../adr/0003-storage.md) | Room |
| Weather source | [0004](../adr/0004-weather.md) | Open-Meteo behind port |
| Module graph | [0005](../adr/0005-module-graph.md) | feature packages + data-* |
| 2D→3D path | [0006](../adr/0006-3d-terrain.md) | Hillshade first |

Still open for impl PR: FGS notification copy, minSdk soft-confirm 26, basemap tile provider URL, export format (P3).

---

## Stack default

Native Android, Kotlin, Jetpack Compose, clean architecture modules, coroutines/Flow, **Room**, Koin DI, **MapLibre**, Open-Meteo.

## Design tooling

Optional Google Stitch for screen mockups + DESIGN.md; Compose implementation via ECC Android skills + Emil motion craft (web recipes → Compose).

## Metric

Completed recorded ski-days with ≥1 valid descent; offline retention of history.
