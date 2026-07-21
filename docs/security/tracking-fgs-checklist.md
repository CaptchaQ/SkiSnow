# Security checklist — Session tracking FGS (slice 0)

**Date:** 2026-07-21  
**Scope:** first LOCATION + Foreground Service PR

## Applied controls

| Control | Status |
|---------|--------|
| No hardcoded secrets / API keys | PASS — Open-Meteo no key; no tokens in source |
| Runtime location permission before FGS | PASS — UI requests Fine/Coarse; StartSession fails + marks FAILED if tracker cannot start |
| FGS type `location` + `FOREGROUND_SERVICE_LOCATION` | PASS — service + manifest |
| Persistent notification with Stop action | PASS — `ACTION_FINALIZE` → `StopSession(stopTracking=false)` then teardown; UI Stop → `StopSession` → `ACTION_STOP` teardown only (no re-entrancy loop) |
| Service `exported=false` | PASS |
| No `ACCESS_BACKGROUND_LOCATION` | PASS — not requested |
| Track data local-only (Room) | PASS — no upload |
| Weather HTTPS only | PASS — api.open-meteo.com |
| Cleartext traffic disabled | PASS — app manifest |
| Logs: no PII dumps of full tracks | PASS — avoid logging lat/lon lists |

## Residual risks

- Process kill mid-record: incomplete session may remain RECORDING until manual stop/recovery UI (follow-up).
- Open-Meteo free tier ToS if commercializing later.
- Demo MapLibre style is public third-party; replace for production branding.

## Not in this slice

- Background location always-on
- Cloud sync / accounts
- Analytics of GPS
