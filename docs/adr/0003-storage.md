# ADR-0003: Storage — Room

**Date**: 2026-07-21  
**Status**: accepted  
**Deciders**: SkiSnow product architecture + user (2026-07-21)

## Context

Need durable local storage for `SkiDay`, bulk `TrackPoint` inserts, session history offline forever, optional weather cache. Existing stack is Android + Koin + pure Kotlin domain. Multiplatform is not a near-term goal.

## Decision

Use **Room** (SQLite) in `data-session` (and weather cache tables as needed). Domain models stay free of Room annotations; mappers live in data layer. Optional **SQLCipher** later if encryption becomes a hard requirement — not v0.

## Alternatives Considered

### Alternative 1: SQLDelight
- **Pros**: Shared multiplatform schema; nice Kotlin API  
- **Cons**: Extra toolchain; KMP not committed; Room is default Android bulk pattern  
- **Why not**: Prefer boring Android path until KMP is real

### Alternative 2: DataStore / files only
- **Pros**: Simple  
- **Cons**: Poor bulk point queries, history, migrations  
- **Why not**: Track logs are relational/time-series heavy

### Alternative 3: Realm
- **Pros**: Reactive objects  
- **Cons**: Heavier dependency; less standard in this repo’s ECC Android defaults  
- **Why not**: Room fits clean arch + Koin

## Consequences

### Positive
- Mature migrations, bulk insert, Flow queries  
- Fits module split and offline history invariant  
- Weather cache co-located or adjacent tables

### Negative
- Android-only data module (acceptable)  
- Annotation processors / KSP config in Gradle

### Risks
- Large days (100k+ points) need batching & indexes — design early on `(sessionId, time)`  
- Migration mistakes — schema review in first Room PR

## Links
- Product rule: history never paywalled (local DB is source of truth)  
- Related: ADR-0005 `data-session`
