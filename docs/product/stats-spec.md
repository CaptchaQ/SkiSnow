# SkiSnow — Stats Specification

**Status:** proposed defaults (2026-07-21)  
**Owner:** domain `StatsCalculator`  
**Units:** SI in domain (m, m/s, Instant); UI converts.

All thresholds marked **proposed default** — tune with field data; keep pure & testable.

---

## Inputs

Ordered `List<TrackPoint>` for one `SkiDay`. Optional `List<Segment>` when P2 auto-segmentation exists.

### Accuracy gate (proposed default)
- Drop or mark non-stat points where `accuracyM == null` **or** `accuracyM > 25.0`.
- Still store raw points in repository; stats use filtered view.

### Moving gate (proposed default)
- Point is **moving** if filtered `speedMps >= 0.5` (≈ 1.8 km/h).
- Below threshold: contributes to duration clock but not avg moving speed distance weight.

---

## Distance

- Sum haversine distance between consecutive **accuracy-gated** points.
- Skip pairs with time delta `> 120s` without interpolating (GPS gap) — **proposed default**.
- Result: `distanceM: Double`.

---

## Duration

- `endedAt - startedAt` if both set; else `lastPoint.time - firstPoint.time`.
- Pause intervals: when session lifecycle supports pause, exclude paused wall time from **moving duration** if tracked; v0 may use wall clock only until pause metadata lands — **open**, prefer exclude paused time when `Paused` state exists.

---

## Vertical drop

### Without segments (P0)
1. Build altitude series from points with non-null `altitudeM` after accuracy gate.
2. Smooth with **5-point median** window (**proposed default**).
3. Sum **negative** successive deltas: `verticalDropM = Σ max(0, alt[i] - alt[i+1])` after smoothing.
4. Optional noise floor: ignore |delta| `< 1.0 m` (**proposed default**).

### With DESCENT segments (P2)
- Apply the same sum only on indices covered by `SegmentType.DESCENT`.
- Lift segments must not inflate vertical drop.

---

## Max speed

- `maxSpeedMps = max(speedMps)` over accuracy-gated points with non-null speed.
- Prefer DESCENT-only when segments exist (P2).
- Optional spike filter: reject speed if `|a|` from prev sample implies unrealistic accel — **TBD**, not in v0.

---

## Average moving speed

- `avgMovingSpeedMps = sum(distance of moving pairs) / sum(time of moving pairs)`.
- Exclude non-moving and gap pairs.

---

## Elevation profile (chart series)

- Output: list of `(distanceAlongTrackM, altitudeM)` using smoothed altitudes and cumulative haversine.
- Downsample for UI if `n > 500` points — presentation concern; domain may return full series.

---

## Session validity (metric)

A ski-day counts as **valid completed day** for product metric if:
- status `Saved`, and
- `distanceM >= 200` **or** `verticalDropM >= 50` (**proposed default**), and
- duration `>= 5 minutes`.

---

## Non-goals for stats v0

- Calorie / effort estimates  
- Trail difficulty classification  
- Resort-specific run names  
- Heart-rate / Health Connect (later optional)

---

## Test contracts (when implementing)

Unit tests must cover: empty list → zeros; single point; gap > 120s; accuracy outliers; pure descent altitude staircase; lift up then down (P2).
