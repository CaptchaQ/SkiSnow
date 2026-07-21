# ADR-0004: Weather source v1 — Open-Meteo

**Date**: 2026-07-21  
**Status**: accepted  
**Deciders**: SkiSnow product architecture + user (2026-07-21)

## Context

P0/P1 need mountain-relevant weather (snowfall, snow depth, freezing level, wind, temperature), not city-only UX. OpenSnow-class editorial forecasts are out of scope for v1. Need free bootstrap without API key if possible; domain port must allow provider swap.

## Decision

**v1 provider:** [Open-Meteo](https://open-meteo.com/en/docs) Forecast API (no key for non-commercial free tier) behind `WeatherRepository`. Cache responses in Room. Map variables at minimum: temperature, snowfall, snow_depth, wind speed/gusts, freezing_level_height.

Do **not** claim OpenSnow-level forecast quality in marketing.

## Alternatives Considered

### Alternative 1: OpenWeather / commercial mountain APIs
- **Pros**: SLAs, support  
- **Cons**: Keys, cost, ToS; early bootstrap friction  
- **Why not**: Defer until product monetization or quality gap proven

### Alternative 2: Scrape OpenSnow / resort pages
- **Pros**: Editorial quality  
- **Cons**: Fragile, ToS, liability  
- **Why not**: Forbidden approach

### Alternative 3: No weather in MVP
- **Pros**: Smaller scope  
- **Cons**: User explicitly asked ski weather; Open-Meteo effort is low  
- **Why not**: Include thin weather as P0/P1 enrichment

## Consequences

### Positive
- Fast path to mountain variables  
- Port-based swap keeps options open  
- Recording independent of weather failures

### Negative
- Not editorial “Daily Snow”  
- Free tier terms must be re-checked if commercializing

### Risks
- Accuracy at complex terrain — set UX expectations; allow provider upgrade  
- Network on mountain — stale cache + error chip

## Links
- Competitive: OpenSnow positioning vs Open-Meteo vars  
- Related: ADR-0005 `data-weather`
