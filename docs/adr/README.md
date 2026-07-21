# Architecture Decision Records

SkiSnow ADR index. **Accepted** 2026-07-21 (user product decisions).

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [0001](0001-map-sdk.md) | Map SDK — MapLibre Native Android | accepted | 2026-07-21 |
| [0002](0002-location.md) | Location — Fused + Foreground Service | accepted | 2026-07-21 |
| [0003](0003-storage.md) | Storage — Room | accepted | 2026-07-21 |
| [0004](0004-weather.md) | Weather — Open-Meteo v1 | accepted | 2026-07-21 |
| [0005](0005-module-graph.md) | Module graph expansion | accepted | 2026-07-21 |
| [0006](0006-3d-terrain.md) | 2D→3D terrain path | accepted | 2026-07-21 |

Template: [template.md](template.md)

Product: `docs/product/product-brief.md`, `capability-plan.md`.

### Locked product prefs (2026-07-21)
- Persona: resort + freeride equally (P0 shared tracker+2D; freeride layers P2)
- First slice: session recording **with** live 2D map
- Record UX: Start + Pause (+ Stop)
- Weather: Open-Meteo thin chip in first release
- Region: global OSM/MapLibre, no exclusive geo
- Units: SI in domain; UI switcher, **default metric**
