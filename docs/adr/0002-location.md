# ADR-0002: Location — Fused Location Provider + Foreground Service

**Date**: 2026-07-21  
**Status**: accepted  
**Deciders**: SkiSnow product architecture + user (2026-07-21)

## Context

MVP requires continuous GPS while skiing with phone often in pocket. Android background limits require a Foreground Service (FGS) for reliable tracking. Accuracy vs battery tradeoff is central; domain must stay pure (ports only).

## Decision

While session status is `Recording`, use **Google Play Services Fused Location Provider** (high accuracy) behind domain port `LocationTracker`, driven from an app-layer **Foreground Service** with persistent notification. Pause stops updates; stop tears down FGS.

**Lane gate:** ADR accepted. Manifest LOCATION + FGS may be added only in a PR that also passes `security-review` (service type, notification stop action, runtime permission UX).

## Alternatives Considered

### Alternative 1: LocationManager only (framework GPS)
- **Pros**: No Play Services dependency  
- **Cons**: More battery/accuracy work; weaker fusion with network/sensors  
- **Why not**: Industry default on Android ski trackers is fused; devices without Play Services are non-goal for v0

### Alternative 2: Background location (ACCESS_BACKGROUND_LOCATION) always
- **Pros**: Track with screen off without FGS in older models  
- **Cons**: Play policy scrutiny, user trust damage, not needed if FGS is correct  
- **Why not**: Prefer FGS while recording; revisit only with product+security pass

### Alternative 3: Wear OS primary
- **Pros**: Wrist UX  
- **Cons**: Scope explosion  
- **Why not**: Phone-first MVP

## Consequences

### Positive
- Reliable pocket tracking pattern  
- Domain port keeps `data-location` swappable  
- Clear lifecycle tied to session states

### Negative
- Play Services dependency in `data-location`  
- Notification UX and battery complaints possible (bergfex-class)

### Risks
- Incorrect FGS type → Play rejection — mitigate with security-review checklist  
- GPS altitude noise — mitigate via `stats-spec.md` smoothing

## Links
- Capability states: Idle → Recording → Paused → Stopping  
- Related: ADR-0005 `data-location`
