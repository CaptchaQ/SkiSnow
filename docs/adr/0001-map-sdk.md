# ADR-0001: Map SDK — MapLibre Native Android

**Date**: 2026-07-21  
**Status**: accepted  
**Deciders**: SkiSnow product architecture + user (2026-07-21)

## Context

SkiSnow needs live 2D map with track polyline, offline basemap packs (P1), freeride DEM/hillshade path (P2), without Google Maps vendor lock-in or proprietary resort-map licensing for v1. Product requires intermittent connectivity and pocket freeride use.

## Decision

We use **MapLibre Native Android** as the map SDK for basemap, camera, polyline overlays, offline region packs, and DEM-driven `HillshadeLayer`.

## Alternatives Considered

### Alternative 1: Google Maps SDK
- **Pros**: Polished UX, wide docs, easy Compose samples  
- **Cons**: Offline limited/paid; freeride DEM/slope layers awkward; Play Services coupling; ToS/branding  
- **Why not**: Conflicts with offline-first freeride and OSS basemap strategy

### Alternative 2: OSMDroid
- **Pros**: Simple offline raster tiles  
- **Cons**: Weaker vector/style ecosystem; hillshade/3D path thinner; less active vs MapLibre  
- **Why not**: MapLibre covers offline + style layers + hillshade with stronger long-term path

### Alternative 3: Mapbox proprietary SDK
- **Pros**: Mature terrain features historically  
- **Cons**: Pricing/token lock-in; MapLibre is the OSS fork path we prefer  
- **Why not**: Avoid commercial token dependency for core map

## Consequences

### Positive
- OSS vector tiles + offline region support  
- `HillshadeLayer` with Terrain RGB / Terrarium encodings ([API docs](https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.style.layers/-hillshade-layer/index.html))  
- Aligns with freeride/offline without Google Maps lock-in

### Negative
- Compose interop is `AndroidView` / custom — more glue than Google Maps Compose  
- Tile hosting/style ops are our problem (or third-party free tiers)

### Risks
- Full 3D terrain pitch may lag MapLibre GL JS — mitigated by ADR-0006 phased path  
- Need explicit style + DEM source choice before P2

## Links
- Capability: live map P0, offline P1, terrain P2  
- Related: ADR-0005 modules (`data-map`), ADR-0006 3D path
