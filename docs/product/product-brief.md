# SkiSnow — Product Brief

**Date:** 2026-07-21  
**Modes:** product-lens Mode 1 (diagnostic) + Mode 4 (ICE prioritization)  
**Inputs:** user product ask + `docs/product/competitive-research.md`  
**Go/no-go:** **GO** — phased Android local-first ski day app (track + map + mountain weather). Do **not** open LOCATION/map code until ADR acceptance + first tracking PR process.

---

## 1. Who

**Primary:** freerider + resort skier on **Android**, phone often in pocket, cold hands, intermittent connectivity on mountain.

**Not:** iOS-first users, hardware coaching buyers (Carv), resort operators, multi-tenant social network.

---

## 2. Pain

| Pain | Frequency | Today’s workaround |
|------|-----------|-------------------|
| Fragmented stack: tracker + map + weather | Every ski day | 2–3 apps (Slopes/Ski Tracks + OpenSnow + onX/Gaia) |
| Weak freeride terrain context in pure trackers | Freeride / side-country days | Separate map app with slope angle |
| Weather not ski-specific (city models) | Trip planning + morning of | OpenSnow / bergfex / webcams |
| History locked behind paywall | After multi-season use | Export scramble / switch apps (Ski Tracks lesson) |
| Cold hands UI | On lift / wind | Oversized controls, auto-record, minimal taps |

---

## 3. Why now

- MapLibre OSS offline + `HillshadeLayer` DEM path without Google Maps lock-in  
- Open-Meteo free mountain-relevant variables for v1 weather bootstrap  
- Competitor monetization backlash creates room for **local-first trust** positioning  
- Existing SkiSnow Android clean-arch skeleton + skill pipeline ready for gated implementation  

---

## 4. 10-star vision

One app: live **2D/3D** map with track overlay, **slope-angle** freeride layers, **auto runs**, offline tiles for chosen region, **mountain weather at elevation**, session history forever on device, export, optional friends later.

---

## 5. MVP (prove thesis)

Smallest product that proves «pocket GPS ski day + map + elev + basic mountain weather works offline-first»:

1. Pocket GPS **day recorder** (Foreground Service when lane open)  
2. Live **2D map** polyline + user position  
3. Core stats: max/avg speed, vertical drop, distance, duration  
4. **Session history** (local, never paywalled)  
5. **Elevation profile** chart  
6. Basic **mountain weather** for current location (hourly snow/wind/freeze/temp)

**Success metric:** completed recorded ski-days with ≥1 valid descent; retention of history offline (open app later without network → sessions still readable).

---

## 6. Anti-goals (v0–v1)

- Social feed / public leaderboards  
- Resort commerce, tickets, lodging  
- iOS (unless later KMP)  
- Coaching hardware (Carv-class)  
- Full avalanche **authority** product (deep-link only, P3)  
- Global proprietary trail-map catalog like Slopes (licensing)  
- Backend accounts / cloud sync as requirement for core value  
- Selling track/location data  

---

## 7. ICE prioritization (locked proposal)

Score = Impact × Confidence ÷ Effort (1–5 each). Ranking matches delivery buckets.

| Pri | Capability | I | C | E | ICE | Why |
|-----|------------|---|---|---|-----|-----|
| **P0** | Session record (FGS), track points, stats, history | 5 | 5 | 3 | 8.3 | Core value of Slopes/Ski Tracks without paywall lock |
| **P0** | Live 2D map + polyline + user position | 5 | 4 | 3 | 6.7 | User asked routes on map |
| **P0** | Elevation profile + vertical drop | 4 | 5 | 2 | 10.0 | User asked elevation deltas; cheap after track points |
| **P1** | Speed heatmap / timeline replay | 3 | 4 | 3 | 4.0 | Differentiator (Slopes Premium pattern) |
| **P1** | Mountain weather (hourly snow, wind, freeze, temp@elev) | 4 | 4 | 2 | 8.0 | User asked ski weather; Open-Meteo bootstrap |
| **P1** | Offline basemap package for selected region | 4 | 3 | 3 | 4.0 | Pocket + freeride connectivity |
| **P2** | 3D terrain path (DEM hillshade → pitch later) | 3 | 3 | 4 | 2.3 | User asked 3D; MapLibre hillshade first |
| **P2** | Slope-angle layer (freeride) | 4 | 3 | 4 | 3.0 | bergfex Pro / onX-class |
| **P2** | Auto run vs lift segmentation | 4 | 3 | 4 | 3.0 | Table stakes; algorithm risk |
| **P3** | Avalanche bulletin deep-link / region layer | 3 | 4 | 2 | 6.0 | Safety UX, not liability primary source |
| **P3** | GPX/FIT export, friends live share | 3 | 4 | 3 | 4.0 | Interop / social later |
| **P3** | Resort official trail maps | 3 | 2 | 5 | 1.2 | Licensing heavy — partner/later |

**Phasing rule:** ship tracking+2D before true 3D freeride layers. If user later demands 3D-first, pull hillshade earlier but keep tracking P0.

---

## 8. Risks

| Risk | Mitigation |
|------|------------|
| GPS altitude noise → bad vertical | Smoothing + accuracy gates; document in stats-spec |
| MapLibre 3D insufficient | Phase A hillshade only; spike Cesium/Filament later |
| Open-Meteo ≠ OpenSnow quality | Port behind `WeatherRepository`; swap provider |
| Battery drain / Play policy FGS | security-review + FGS type before LOCATION lane |
| Scope creep (Slopes catalog) | Anti-goals; OSM/MapLibre basemap only v1 |

---

## 9. Recommendation

**Build.** Next durable artifacts:

1. Rewrite `docs/product/skisnow-intent.md`  
2. Full `docs/product/capability-plan.md`  
3. ADR drafts 0001–0006 (Proposed)  
4. Domain ports/models only — **no** LOCATION permissions  

**Handoff:** product-capability → architecture-decision-records → (after user accepts ADRs) search-first + orch-add-feature + security-review for FGS.
