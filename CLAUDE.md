# SkiSnow — Claude / OMP essentials

Полные инструкции: `AGENTS.md`.

## Язык
С пользователем — **русский**. Код/skills — English identifiers.

## Skills подключены в OMP?
**Да.** 83 skills в:
- `.claude/skills/<name>/SKILL.md` (provider `claude`)
- `.agents/skills/<name>/SKILL.md` (provider `agents`)

Метаданные (name+description) в system prompt. Тело: `read skill://<name>` **до** работы по зоне skill.  
Не импровизировать, если skill есть.

## Skill-first (перед ответом/кодом)
На вопрос/задачу пользователя — **сначала** рекомендовать 1–3 skill’а (зачем + порядок), затем `read skill://…` и делать.  
Нет match → сказать явно + fallback (`search-first` / `documentation-lookup` / `ecc-guide`).  
Trivial — можно без recommend.

## Живой AGENTS.md
Агент **MUST** обновлять корневой `AGENTS.md` по ходу (durable truth: ADR, modules, permissions, skill routing). Не session-notes. После material change — кратко сказать пользователю; зеркалить essentials сюда.

## Stack (ADR accepted 2026-07-21)
Android, Kotlin, Jetpack Compose, clean modules, Koin.  
LOCATION lane **open**; slice-0 (session+map+FGS+weather chip) **implemented**. Checklist: `docs/security/tracking-fgs-checklist.md`.

## Preference
1. ECC Android/Kotlin (код, TDD, orch, security)  
2. Emil motion → **адапт к Compose** (не rewrite skill)  
3. Stitch — mockups/DESIGN.md only (не RN/React)

## Product
- `docs/product/competitive-research.md`
- `docs/product/product-brief.md`
- `docs/product/skisnow-intent.md`
- `docs/product/capability-plan.md`
- `docs/product/stats-spec.md`
- `docs/adr/` (0001–0006 **accepted**)

### Locked prefs
Resort+freeride; first slice session+live 2D map; Start+Pause; weather chip; global basemap; units default metric.

## Modules
`app` → presentation/domain/data; domain pure Kotlin; ports under `com.skisnow.domain.port`.

## Next pipeline
Device smoke (`assembleDebug` + real GPS) → edge tests → P1 offline packs / heatmap.

## Vendor
`scripts/vendor-all-skills.ps1` — не править skill bodies.
