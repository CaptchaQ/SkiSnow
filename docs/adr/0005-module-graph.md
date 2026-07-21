# ADR-0005: Module graph expansion

**Date**: 2026-07-21  
**Status**: accepted  
**Deciders**: SkiSnow product architecture + user (2026-07-21)

## Context

Bootstrap modules: `app → presentation, domain, data`; `presentation → domain`; `data → domain`; `domain` pure Kotlin. Product will grow session, map, weather, history features. Need feature packaging without breaking dependency rules or putting Android types in domain.

## Decision

Keep clean-architecture dependency direction. Expand **conceptually** (Gradle modules may be introduced incrementally) as:

```
app
presentation          # Compose screens, map Compose interop
  feature-session
  feature-map
  feature-history
  feature-weather
domain                # models, use cases, ports (pure Kotlin)
data
  data-location       # Fused + FGS (when lane open)
  data-session        # Room
  data-map            # MapLibre styles, offline packs
  data-weather        # Open-Meteo client + cache
```

**Rules (unchanged):**
- `presentation/*` → `domain` only (not data)
- `data-*` → `domain` only
- `domain` → nothing Android / no data
- `app` wires Koin modules

Until split is needed, packages under existing `presentation` / `data` may mirror these names without new Gradle modules.

## Alternatives Considered

### Alternative 1: Keep single `data` forever
- **Pros**: Less Gradle  
- **Cons**: Location/map/weather tangle; harder parallel work  
- **Why not:** Plan allows package-first then module split

### Alternative 2: Feature modules that depend on data
- **Pros**: Vertical slices  
- **Cons**: Risks domain leakage and cyclic deps  
- **Why not:** Prefer ports in domain; features talk to use cases

## Consequences

### Positive
- Clear ownership for map vs session vs weather  
- Matches capability ports  
- Parallelizable implementation later

### Negative
- More modules → longer configuration if split too early  
- Need discipline on package boundaries before Gradle split

### Risks
- Premature module explosion — mitigate: packages first, Gradle when binary boundary hurts

## Links
- ADRs 0001–0004 map to data-*  
- Domain ports in capability-plan
