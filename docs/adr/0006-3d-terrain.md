# ADR-0006: 2D → 3D terrain path

**Date**: 2026-07-21  
**Status**: accepted  
**Deciders**: SkiSnow product architecture + user (2026-07-21)

## Context

User wants 3D map / freeride terrain context. Full 3D terrain camera on MapLibre Native Android is not assumed production-ready for MVP. MapLibre already exposes client-side **hillshade** from DEM (`HillshadeLayer`). Blocking MVP on true 3D would delay tracking value.

## Decision

**Phase A (P2 early):** 2D MapLibre map + DEM **hillshade** (and later slope-angle derived layer).  
**Phase B:** terrain exaggeration / pitch / true 3D camera **if** MapLibre Android build meets product needs after a spike.  
**Fallback:** document Cesium/Filament as future research — do **not** block P0 tracking+2D.

MVP ships **2D only** (P0); hillshade is not P0.

## Alternatives Considered

### Alternative 1: 3D-first (block MVP)
- **Pros**: Matches 10-star vision early  
- **Cons**: High risk, delays core tracker thesis  
- **Why not:** Product brief prioritizes completed ski-days

### Alternative 2: Google Maps 3D / Photorealistic
- **Pros**: Flashy  
- **Cons**: Offline/freeride poor fit; lock-in (see ADR-0001)  
- **Why not:** Inconsistent with map strategy

### Alternative 3: Separate 3D engine from day one
- **Pros**: Full control  
- **Cons**: Two map stacks, huge effort  
- **Why not:** Spike only if MapLibre Phase B fails

## Consequences

### Positive
- MVP unblocked  
- Early freeride “terrain feel” via hillshade without full 3D  
- Clear acceptance criteria for Phase B spike

### Negative
- Users expecting FATMAP-class 3D day-one will wait  
- Slope-angle may need extra DEM processing beyond hillshade

### Risks
- Phase B never lands — still ship valuable 2.5D freeride context  
- DEM tile licensing/hosting — decide before P2 implement

## Links
- MapLibre HillshadeLayer docs  
- Competitive: onX/bergfex slope layers; Outmap/FATMAP legacy  
- Related: ADR-0001
