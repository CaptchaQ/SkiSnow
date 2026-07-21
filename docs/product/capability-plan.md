# SkiSnow — Capability Plan

**Date:** 2026-07-21  
**Skill structure:** product-capability  
**Sources:** `skisnow-intent.md`, `product-brief.md`, `competitive-research.md`  
**Status:** P0 contract filled; P2+ may remain open. LOCATION lane still closed.

---

## CAPABILITY

Alpine skier / freerider on Android can **record a GPS ski day**, see a **live 2D track** on a map, review **speed / distance / vertical / elevation profile**, keep **offline history forever**, and optionally load **mountain-relevant weather** for the current location — without accounts or paywalled history.

---

## CONSTRAINTS

### Fixed business / product rules
- Local-first: recording + history must work without network and without account.
- Never lock local session history behind paywall or forced cloud.
- Do not sell track/location data.
- No LOCATION permissions or FGS in tree until ADRs accepted + security-review for first tracking PR.
- Weather failure must not block recording.

### Scope boundaries
- Android only for v0–v1.
- Basemap: OSM / MapLibre styles — not proprietary global resort trail catalog.
- Avalanche: deep-link / secondary layer only (P3), not primary authority.

### Invariants
- A `SkiDay` in `SAVED` has immutable `startedAt`/`endedAt` and append-only track points (edits = future feature).
- Stats are pure functions of track points (+ optional segments when P2 exists).
- Domain module has **zero** Android dependencies.
- `presentation → domain`; `data-* → domain`; never `domain → data` or `domain → presentation`.

### Trust boundaries
- Actor: single skier on-device.
- GPS/stream stays on device in v0.
- Weather API is untrusted network; validate/parse; cache in Room.

### Data ownership
- Device-local ownership of all sessions and track points.
- Weather cache is disposable; may be purged.

### Lifecycle transitions (session)
See states below. Only one active recording session at a time.

### Rollout / migration
- Bootstrap `SessionId` / `createSession()` cut over to full model (this plan).
- Room schema migrations when storage ADR accepted (not in domain-only scaffold).

### Failure and recovery
| Failure | Behavior |
|---------|----------|
| GPS gap / poor accuracy | Continue session; flag points with `accuracyM`; UI chip optional |
| Process kill mid-record | Recover incomplete session on next launch → resume or mark Failed/Saved incomplete |
| Weather fetch fail | Show stale cache + error chip; recording unaffected |
| Storage full | Fail save with user-visible error; do not silently drop day |

---

## IMPLEMENTATION CONTRACT

### Actors
- **Skier** (sole user on device). No multi-tenant backend, no operator console v0.

### Surfaces (Compose)
| Surface | Phase | Notes |
|---------|-------|-------|
| Active session (start/pause/stop) | P0 | Large hit targets; pocket UX |
| Live map (2D) | P0 | Position + polyline |
| Session stats + elevation profile | P0 | During/after session |
| History list + detail | P0 | Offline |
| Weather chip / panel | P0/P1 | Current location |
| Offline map pack manager | P1 | |
| Heatmap / timeline | P1 | |
| Slope / 3D layers | P2 | |

Mockups optional via Stitch; implementation is Compose.

### States and transitions

```
Idle → Recording → Paused → Recording
                 ↘ Stopping → Saved
                 ↘ Stopping → Failed
Paused → Stopping → Saved | Failed
```

| State | Meaning |
|-------|---------|
| `Idle` | No active session |
| `Recording` | Accepting TrackPoints; FGS when implemented |
| `Paused` | Session open; not accepting points |
| `Stopping` | Finalizing stats + persist |
| `Saved` | Terminal success |
| `Failed` | Terminal failure (persist error / unrecoverable) |

### Domain model (minimal, locked)

```
SkiDay
  id: SessionId
  startedAt: Instant
  endedAt: Instant?
  status: SessionStatus

TrackPoint
  time: Instant
  lat: Double
  lon: Double
  altitudeM: Double?
  speedMps: Double?
  accuracyM: Double?
  source: LocationSource  // GPS | NETWORK | FUSED | UNKNOWN

Segment (optional P2)
  type: DESCENT | LIFT | FLAT | UNKNOWN
  startIndex / endIndex into TrackPoint list

SessionStats
  duration: Duration
  distanceM: Double
  verticalDropM: Double
  maxSpeedMps: Double
  avgMovingSpeedMps: Double

WeatherSnapshot
  location: lat/lon
  fetchedAt: Instant
  hourly: List<HourlyWeather>  // temp, snowfall, snowDepth, wind, freezingLevel

MapRegionPack (P1)
  id, bounds, offlineReady: Boolean
```

### Ports (domain interfaces)

| Port | Responsibility |
|------|----------------|
| `LocationTracker` | `Flow<TrackPoint>` while recording; no Android types |
| `SessionRepository` | create/update/observe sessions, append points, list history |
| `StatsCalculator` | pure stats (+ elevation series) from points |
| `WeatherRepository` | fetch/cache `WeatherSnapshot` |
| `OfflineMapPackRepository` | P1 offline packs |
| Map camera / overlay | presentation adapters (not domain pure ports if UI-bound) |

### Use cases (P0 names)
- `StartSession`
- `PauseSession` / `ResumeSession`
- `StopSession`
- `ObserveActiveSession`
- `ObserveSessionHistory`
- `GetSessionDetail`
- `RefreshWeather` (optional network)

### Stats algorithms
See `docs/product/stats-spec.md` (proposed defaults: accuracy gate, haversine distance, smoothed vertical drop, max filtered speed).

### Security / policy
- Runtime location permissions only when LOCATION lane opens.
- FGS with correct service type + persistent notification.
- No background unrestricted location in v0 without explicit product+security pass.
- Export (P3) is user-initiated.

### Observability
- Local debug logs for session lifecycle transitions.
- No third-party analytics of track points in v0 (product rule).

---

## NON-GOALS

- Social feed, friends live share (deferred P3)
- Coaching AI / hardware sensors
- Resort commerce
- iOS
- Backend accounts required for tracking
- Proprietary global trail-map catalog
- Avalanche forecast as primary source
- Paywalled local history

---

## OPEN QUESTIONS

| # | Question | Blocks? | Notes |
|---|----------|---------|-------|
| 1 | ADR-0001…0006 | **Resolved** | **Accepted** 2026-07-21 |
| 2 | Units | **Resolved** | SI domain; UI switcher, default metric |
| 3 | Persona / first slice | **Resolved** | Resort+freeride; session+live 2D map; Start+Pause |
| 4 | Weather bar | **Resolved** | Open-Meteo thin chip in first release |
| 5 | Region focus | **Resolved** | Global OSM/MapLibre basemap |
| 6 | MinSdk 26 | Soft | Keep scaffold 26 unless device constraint appears |
| 7 | FGS notification copy / stop action | **Yes** first tracking PR | Decide in security-review + UX copy |
| 8 | Open-Meteo commercial terms if monetize | Soft | Port allows provider swap |
| 9 | Auto-segment thresholds | P2 | |
| 10 | DEM tile source for hillshade/slope | P2 | MapTiler / AWS / self-host TBD |
| 11 | GPX vs FIT export priority | P3 | |

---

## HANDOFF

- **Status:** ADRs accepted; product prefs locked; **ready for first implementation slice**.
- **First slice:** session record (FGS + fused) + live 2D MapLibre polyline + pause/stop + Room persistence + weather chip (Open-Meteo).
- **Next lanes:** `security-review` (FGS/permissions) → `search-first` / `documentation-lookup` (MapLibre Compose interop, Fused, Room) → `orch-add-feature` / `tdd-workflow`.
- **Still not in slice-0:** slope-angle, auto run/lift, offline packs (P1), true 3D, social, export.
- **Related:** `docs/product/stats-spec.md`, `docs/adr/*` (accepted).
