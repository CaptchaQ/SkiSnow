# SkiSnow — Competitive Research

**Date:** 2026-07-21  
**Scope:** ski/freeride trackers, maps, mountain weather — facts for product decisions.  
**Labels:** **sourced** = store/article/API docs; **inference** = derived from evidence; **recommendation** = SkiSnow product call.

---

## 1. Landscape summary

Рынок разделён на **трекеры** (Slopes, Ski Tracks), **all-in-one Alps** (bergfex), **ski weather** (OpenSnow, OnTheSnow), **backcountry maps** (onX, Gaia, CalTopo). Ни один Android-продукт одновременно не закрывает: local-first tracking без paywall-lock истории + freeride terrain (slope angle/offline DEM) + mountain weather + 2D/3D карта — без тяжёлого licensing resort trail maps.

**sourced:** OnTheSnow «The Best Skiing Apps» (Aug 2025 / updated 2025-10), OnTheSnow backcountry apps (Feb 2026), Google Play listings (Slopes, Ski Tracks, bergfex, OpenSnow) accessed 2026-07-21.

---

## 2. Core competitors

### 2.1 Slopes (Breakpoint Studio)

| | |
|--|--|
| Role | Best-in-class resort tracker |
| Platforms | Android + iOS |
| Monetization | Free core (ad-free); Premium ~$29.99/yr (family higher) |
| Store | [Play](https://play.google.com/store/apps/details?id=com.consumedbycode.slopes) · 4.7★ · 500K+ |

**Features (sourced — Play listing):**
- Smart recording: auto detect uphill / lifts / runs; multi activity types
- Stats: speed, vertical, run times, season/lifetime
- Friends live location (opt-in, privacy-focused)
- Interactive resort maps 2000+ (Premium), 2D/3D, offline trail maps (Premium)
- Speed heatmaps + day timeline (Premium)
- Health Connect; snow conditions chatter
- Claims never sells data; accounts optional

**User signals (sourced — Play reviews):**
- Terrain slope degree useful (May 2026 review)
- Android resume/finish glitches, cross-device stat drift (2025 review)

**Takeaway (inference):** Auto run/lift = table stakes. Free core tracking is the right monetization lesson vs Ski Tracks. Premium maps/heatmaps are differentiators, not MVP blockers.

---

### 2.2 Ski Tracks (Core Coders / Fitness & Sports apps)

| | |
|--|--|
| Role | Classic GPS ski tracker |
| Monetization | Subscription weekly/yearly (aggressive) |
| Store | [Play](https://play.google.com/store/apps/details?id=com.corecoders.skitracks) · ~3.8–3.9★ · 500K+ |

**Features (sourced — Play listing + OnTheSnow):**
- GPS speed, distance, altitude, vertical
- Auto run recorder
- Maps of saved routes
- Season history, photos, music, wearables
- OnTheSnow: run/lift analysis, **3D map support**, advanced mapping

**Pain (sourced — Play reviews Mar–Apr 2026):**
- Subscription backlash (~$50–70/yr cited by users)
- **History locked** behind paywall after years of use (8 seasons / multi-year history)
- Pause without service → cannot resume; trial UX friction

**Takeaway (recommendation):** Local-first history must **never** lock behind paywall. This is a primary product differentiator and trust invariant.

---

### 2.3 bergfex

| | |
|--|--|
| Role | Alps all-in-one: track + resort info + weather |
| Monetization | Free + ads + Pro IAP |
| Store | [Play](https://play.google.com/store/apps/details?id=com.bergfex.mobile.android) · 4.3★ · 1M+ |

**Features (sourced — Play listing):**
- Auto descent recording; speed/distance/elevation; ski diary
- Winter piste maps, snow reports, lift status, tickets
- Weather forecasts, snow maps, webcams
- **Pro:** snow maps 6h, avalanche map, **slope-steepness layer**, 200+ weather stations, offline training videos
- Battery note: background GPS drains battery

**Takeaway (inference):** Closest feature-combo to SkiSnow vision for Alps. Slope-steepness + weather + track = freeride-adjacent. Heavy content/ops (lifts, tickets) out of scope for SkiSnow v0–v1.

---

### 2.4 OpenSnow

| | |
|--|--|
| Role | Ski weather specialist |
| Monetization | Free tier + Premium trial → paid |
| Store | [Play](https://play.google.com/store/apps/details?id=com.opensnow.android) · 100K+ |

**Features (sourced — Play listing):**
- 15-day snow forecast/report, Daily Snow expert write-ups
- Mountain-focused maps: snow depth, season snowfall, radar
- 3D & offline maps; avalanche forecasts; webcams
- PEAKS proprietary mountain forecast claim

**Takeaway (inference):** Weather for skiers ≠ city weather. SkiSnow cannot match editorial PEAKS in v1; must ship **mountain-relevant variables** (snowfall, snow depth, freezing level, wind, elev-aware temp) via open API and leave room to swap provider.

---

### 2.5 onX Backcountry

| | |
|--|--|
| Role | Freeride/backcountry maps & navigation |
| Monetization | Free basic; Premium ~$30+/yr |
| Sources | [OnTheSnow backcountry 2026](https://www.onthesnow.com/news/best-apps-backcountry-skiers/) |

**Features (sourced):**
- Offline GPS maps
- **Slope-angle layer**, Avalanche Terrain Exposure Scale
- Activity modes (ski, hike, climb, MTB), route library
- Not a ski-stats tracker first

**Takeaway (inference):** Freeride needs DEM + slope angle + offline, not only resort polylines. SkiSnow freeride lane = map layers, not full onX clone.

---

### 2.6 OnTheSnow app

| | |
|--|--|
| Role | Conditions network |
| Monetization | Free |
| Source | [OnTheSnow best apps](https://www.onthesnow.com/news/the-best-skiing-apps/) |

**Features (sourced):** snow reports 2000+ resorts, webcams, forecasts, user reports. Not a day tracker.

---

### 2.7 Adjacent (non-core)

| App | Niche | Note |
|-----|-------|------|
| Gaia GPS / CalTopo | Backcountry planning | Slope shading, multi basemaps — **sourced** OnTheSnow 2026 |
| Outmap / FATMAP legacy | 3D terrain | FATMAP shut after Strava; Outmap as successor — **unverified detail** if primary SnowBrains 403; cite secondary only |
| Snonav | On-slope navigation | iOS-first turn-by-turn — **sourced** OnTheSnow |
| Maprika | Custom piste map GPS | Android; photo map → GPS — **sourced** |
| Peakfinder | Peak ID | Camera/peak names |
| Carv | Boot sensors + coaching | Hardware + $249/yr membership — anti-goal for SkiSnow |
| Epic / Ikon apps | Pass holders | Tracking + trail maps inside pass ecosystem |

---

## 3. Feature matrix

Legend: **Y** = present / core; **P** = premium or partial; **N** = no / not primary; **?** = not verified in sources used.

| Capability | Slopes | Ski Tracks | bergfex | OpenSnow | onX BC | OnTheSnow |
|------------|:------:|:----------:|:-------:|:--------:|:------:|:---------:|
| Tracking (GPS day) | Y | Y | Y | N | Y (nav) | N |
| Auto run/lift | Y | Y | Y | N | N | N |
| Live map 2D | Y | Y | Y | maps | Y | N |
| 3D terrain | P maps | Y (store/OTS) | ? | Y maps | Y | N |
| Offline maps | P | ? | P | Y/P | Y | N |
| Vertical/speed stats | Y | Y | Y | N | limited | N |
| Elevation profile | Y | Y | Y | N | Y routes | N |
| Heatmaps / timeline | P | ? | ? | N | N | N |
| Resort trail map | P catalog | limited | Y Alps | trail maps | N resort | reports |
| Freeride / slope angle | P (review: slope°) | N | P Pro | avalanche maps | Y | N |
| Avalanche / safety | conditions | N | P map | Y forecasts | Y ATES | N |
| Weather / snow | conditions | N | Y | **core** | layers | **core** |
| Social | friends/opt-in | share | friends | N | N | community reports |
| Export (GPX) | ? | ? | ? | N | Y typical outdoor | N |
| Battery/background | FGS-class | FGS-class | explicit drain note | N | offline-first | N |
| Monetization pain | Premium maps OK | **history paywall** | ads+Pro | trial→Premium | subscription | free |

**Evidence types:** matrix cells from store listings + OnTheSnow articles = **sourced** where Y/P stated in listing; `?` = not confirmed in sources above.

---

## 4. Pain insights (must capture)

1. **Local history must stay free offline** — Ski Tracks 2026 reviews: multi-season history locked after subscription push. **sourced** Play reviews. **recommendation:** hard product rule.
2. **Auto run/lift detection is table stakes** — Slopes free smart recording. **sourced.** P2 for SkiSnow (MVP can be whole-day track first).
3. **Cold hands / pocket tracking / battery** — bergfex battery warning; users leave phone in pocket. **sourced** + **inference.** → Foreground Service + simple one-tap UI + accurate but gated GPS.
4. **Freeride ≠ resort polylines** — onX/Gaia/bergfex Pro: slope angle + offline DEM. **sourced.**
5. **Ski weather ≠ city weather** — OpenSnow positioning; Open-Meteo exposes `snowfall`, `snow_depth`, `freezing_level_height`, wind, multi-height temp. **sourced** [Open-Meteo docs](https://open-meteo.com/en/docs).
6. **Fragmented stack** — users run tracker + weather + map apps. **inference** from category split. SkiSnow thesis: one local-first day app.

---

## 5. What SkiSnow should take (recommendations)

Aligned with product brief P0–P3:

| Priority | Capability | Competitor lesson |
|----------|------------|-------------------|
| **P0** | Session record (FGS), track points, stats, history | Slopes free core; anti-Ski-Tracks paywall |
| **P0** | Live 2D map + polyline + user position | All trackers |
| **P0** | Elevation profile + vertical drop | User ask + all trackers |
| **P1** | Speed heatmap / timeline replay | Slopes Premium |
| **P1** | Mountain weather (hourly snow, wind, freeze level) | OpenSnow vars via Open-Meteo bootstrap |
| **P1** | Offline basemap package (region) | onX / OpenSnow offline; freeride connectivity |
| **P2** | 3D path: hillshade DEM first, true 3D later | MapLibre `HillshadeLayer` **sourced** API docs |
| **P2** | Slope-angle layer | bergfex Pro / onX |
| **P2** | Auto run vs lift segmentation | Slopes / Ski Tracks / bergfex |
| **P3** | Avalanche bulletin deep-link (not primary authority) | Avalanche Forecasts / bergfex / OpenSnow |
| **P3** | GPX/FIT export, friends live share | Interop / Slopes social later |
| **P3** | Official resort trail maps | Licensing heavy — defer |

### Explicit anti-goals from competitive scan
- Carv-class hardware coaching  
- Global proprietary trail-map catalog like Slopes (data licensing)  
- Becoming OpenSnow editorial weather  
- History/paywall hostage model  

---

## 6. Stack-relevant evidence

| Topic | Evidence | Label |
|-------|----------|-------|
| MapLibre hillshade DEM | [HillshadeLayer Android API](https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.style.layers/-hillshade-layer/index.html) — Terrain RGB / Terrarium | **sourced** |
| MapLibre full 3D terrain | Community discussion; not assumed ready for MVP | **inference** → phased 2D→3D ADR |
| Open-Meteo ski vars | snowfall, snow_depth, freezing_level_height, wind, no API key free non-commercial | **sourced** |
| FGS / battery | Industry pattern; bergfex drain note | **sourced** + **inference** |

---

## 7. Sources

1. [OnTheSnow — Best Skiing Apps](https://www.onthesnow.com/news/the-best-skiing-apps/) (2025-08, updated 2025-10)  
2. [OnTheSnow — Best Apps for Backcountry Skiers](https://www.onthesnow.com/news/best-apps-backcountry-skiers/) (2026-02)  
3. [Slopes — Google Play](https://play.google.com/store/apps/details?id=com.consumedbycode.slopes)  
4. [Ski Tracks — Google Play](https://play.google.com/store/apps/details?id=com.corecoders.skitracks)  
5. [bergfex — Google Play](https://play.google.com/store/apps/details?id=com.bergfex.mobile.android)  
6. [OpenSnow — Google Play](https://play.google.com/store/apps/details?id=com.opensnow.android)  
7. [Open-Meteo Forecast API docs](https://open-meteo.com/en/docs)  
8. [MapLibre Native Android HillshadeLayer](https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.style.layers/-hillshade-layer/index.html)  

---

## 8. Open research gaps (not blocking product brief)

- Exact GPX export availability per app (mark ? until tested)  
- Outmap / FATMAP successor detail (primary article 403 at plan time)  
- Ski Tracks current free vs paid feature matrix after ownership/monetization changes  
- MapLibre Android production readiness for terrain pitch/exaggeration beyond hillshade  

**Next artifact:** `docs/product/product-brief.md`
