# SkiSnow — Agent Instructions

Native **Android / Kotlin / Jetpack Compose** приложение трекинга горных лыж (карта + GPS + mountain weather).

Bootstrap: 83 project-local skills + multi-module skeleton.  
**ADR-0001…0006 accepted (2026-07-21).** LOCATION/map/FGS implementation **lane open** — first tracking PR must run `security-review` before merging permissions/FGS.

### Product lane status (2026-07-21)
- Research + product brief + capability + ADRs: **done / accepted**
- Domain models + ports: **done**
- Slice-0 session + live 2D map + Room + FGS + Open-Meteo chip: **implemented** (`compileDebugKotlin` + `:domain:test` green)
- Security checklist: `docs/security/tracking-fgs-checklist.md`
- Offline packs, slope, auto-segment, 3D pitch, export: **later** (P1/P2/P3)

### Accepted stack
- Map: MapLibre Native Android (`docs/adr/0001-map-sdk.md`)
- Location: Fused + FGS while Recording (`0002`)
- Storage: Room (`0003`)
- Weather v1: Open-Meteo (`0004`)
- Modules: feature packages + data-* (`0005`)
- 3D: hillshade first (`0006`)

### Locked product prefs
Resort+freeride; first slice = session + live 2D map; Start+Pause; weather chip; global basemap; units default metric.

### Next skill pipeline
Device smoke (`assembleDebug` + real GPS) → Room/FGS edge tests → P1 offline packs / heatmap → P2 hillshade/slope/auto-segment.

---

## Язык общения

- С пользователем общайся **на русском**.
- Код, идентификаторы, commit messages, frontmatter skills — как в репо (обычно English).
- Ответы: без воды, факты / решения / риски / проверки.

- Этот файл — **живой**: агент **должен** обновлять `AGENTS.md` по ходу работы (см. § «Живой AGENTS.md»).

---

## Живой AGENTS.md (обязательно править по ходу)

`AGENTS.md` — project operating manual. **Не** one-shot bootstrap-документ.
Агент **MUST** подстраивать этот файл под реальность SkiSnow по мере работы.

### Что писать (только durable project truth)

- ADR/стек-решения: map SDK, location API, storage, units, minSdk, DI, offline
- Architecture invariants: module graph, permissions, package names, threading
- Product lane status: что открыто / закрыто (LOCATION, map, export…)
- Working skill routing: какой pipeline реально используем; gaps / «редко»
- Compose-mapping Emil: API, которые сработали на проекте
- Устойчивые user preferences (язык, test bar, commit style), если отличаются от текста

### Что НЕ писать

- Session notes, дневник, TODO-списки, «сегодня сделали…»
- Копипаст тел skills / длинные цитаты SKILL.md
- Временные гипотезы без решения
- Правки vendored skills (`.claude/skills/**`, `.agents/skills/**`)

### MUST обновлять, когда:

1. Принято решение → 1–3 предложения + ссылка на ADR/`docs/product/*`
2. Изменился invariant или module surface
3. Нашли/сменили skill-pipeline под SkiSnow
4. Skill useless/noisy → «редко / skip unless…» (vendor copy не удалять)
5. Открыли LOCATION/map/foreground → снять/сузить запреты здесь + product docs
6. User preference закрепилась на проект

### Как править

- Править **`AGENTS.md`** (корень). После material change — **коротко зеркалить** essentials в `CLAUDE.md`.
- Устарело — **заменить/удалить**, не копить противоречия.
- Детали → `docs/product/*`, `docs/adr/*`, code; здесь — сжатые правила.
- Решили/изменили поведение проекта — обновить **в той же сессии**, до «готово».
- Только research без решений — **не трогать** файл.
- После material edit — **одной фразой** сказать пользователю, что изменил в `AGENTS.md`.

### Changelog

При material change — одна строка в `## AGENTS changelog` внизу (дата + суть). Не diary.

---

## OMP: skills подключены?

**Да.** OMP discovery (см. `omp://skills.md`):

| Источник | Путь | Provider | Priority |
|----------|------|----------|---------:|
| Claude layout | `.claude/skills/<name>/SKILL.md` | `claude` | 80 |
| Agents layout | `.agents/skills/<name>/SKILL.md` | `agents` | 70 |

Правила:

- Layout **плоский**: один уровень `skills/<name>/SKILL.md` (вложенность не сканируется).
- В system prompt попадают **name + description**; тело skill — только через `read skill://<name>` (или `/skill:<name>`).
- Имена skills **не дублируются** между `.claude` и `.agents` (один и тот же набор; first-wins).
- `AGENTS.md` (этот файл) грузится отдельно как context (`agents-md` provider), не как skill.

**Обязательное поведение агента:**

### 0. Сначала рекомендовать skill(s) (перед работой)

Когда пользователь задаёт **вопрос**, формулирует **задачу**, просит **фичу/фикс/дизайн/план** — агент **сначала** (до длинного ответа и до кода):

1. Сопоставить запрос с каталогом skills ниже (и descriptions в runtime).
2. **Рекомендовать** 1–3 skill’а (порядок pipeline, если нужен).
3. Кратко: **зачем** каждый + что даст (1 строка на skill).
4. Формат ответа-старта (русский):
   ```
   Skills: `name-1` → `name-2` (опц. `name-3`)
   Зачем: …
   Дальше: читаю skill://… и делаю …
   ```
5. Сразу после рекомендации — **`read skill://<name>`** и выполнять (не ждать «ок», если задача уже ясна).
6. Нет match — сказать «специального skill нет» + fallback: `search-first` / `documentation-lookup` / `ecc-guide` / `prompt-optimizer`.
7. Тривиал (опечатка, «да/нет», уже открытый skill) — recommend можно пропустить.
8. Уточняющие вопросы — **после** skill-рекомендации, не вместо неё.

### 1–5. Исполнение

1. Перед нетривиальной задачей **выбрать matching skill(s)** из каталога ниже.
2. **MUST** `read skill://<name>` (или `skill://<name>/…`) **до** правок/плана по этой зоне.
3. Если skill match — не «импровизировать с нуля»; следовать skill workflow.
4. Несколько skills → читать в порядке pipeline (см. lifecycle).
5. Несовпадение? `skill-scout` / `ecc-guide` / `ecc-recipes` / `prompt-optimizer`.

Проверка в сессии: skills list в runtime должен включать vendored names  
(`android-clean-architecture`, `generate-design`, `emil-design-eng`, …).  
Счёт: **83** в каждом root (`vendor/INVENTORY.md`).

Refresh:

```powershell
.\scripts\vendor-all-skills.ps1
```

---

## Skill preference (источники)

1. **ECC Android/Kotlin** — код, архитектура, тесты, security, orch  
2. **Emil** — motion / UI polish (**адаптировать web → Compose**, тела skills не править)  
3. **Stitch** — mockups + DESIGN.md only (не React/RN codegen)

---

## Полный каталог (83) — когда какой skill

Канонические списки: `vendor/ecc/skill-list.txt`, `vendor/stitch-skills/skill-list.txt`, `vendor/emilkowalski-skills/skill-list.txt`.

### A. Продукт и scope (до кода)

| Skill | Когда |
|-------|--------|
| `product-lens` | «Зачем строим», диагностика направления |
| `product-capability` | PRD/intent → constraints / states / open questions |
| `intent-driven-development` | Acceptance criteria до/во время реализации |
| `architecture-decision-records` | ADR (map SDK, location, storage, …) |
| `blueprint` | Многосессионный multi-agent plan |
| `council` | Несколько валидных путей, нужен structured disagreement |
| `plan-canvas` | План на review с аннотациями |
| `plan-orchestrate` | План → ECC orchestrate chains |

**Pipeline:** `product-lens` → `product-capability` → `intent-driven-development` → ADR.

### B. Research before code

| Skill | Когда |
|-------|--------|
| `search-first` | Искать lib/pattern **до** custom code |
| `documentation-lookup` | Актуальные docs API (Compose, Koin, map SDK…) |
| `deep-research` / `research-ops` | Сравнение вариантов, evidence-first |
| `codebase-onboarding` | Онбординг в незнакомый кусок |
| `code-tour` | Пошаговый walkthrough с якорями |
| `repo-scan` | Аудит ассетов/модулей |
| `workspace-surface-audit` | Что реально доступно в harness |
| `context-budget` | Раздувание skills/MCP/rules |

### C. Android / Kotlin / Compose (код)

| Skill | Когда |
|-------|--------|
| `android-clean-architecture` | Модули, UseCase, Repository, DI |
| `compose-multiplatform-patterns` | Compose UI/state/nav/theme |
| `kotlin-patterns` | Идиоматичный Kotlin |
| `kotlin-coroutines-flows` | Flow/StateFlow, structured concurrency |
| `kotlin-testing` | Kotest/MockK/coroutine tests |
| `coding-standards` | Naming, immutability, review bar |
| `error-handling` | Typed errors, retries, UX ошибок |
| `hexagonal-architecture` | Ports/adapters границы |
| `design-system` | Токены/компоненты UI-системы |
| `make-interfaces-feel-better` | Spacing, hit areas, states |
| `accessibility` | WCAG / a11y traits |
| `latency-critical-systems` | p95, freshness (GPS UI updates) |
| `content-hash-cache-pattern` | Content-hash cache |

### D. Orchestration / delivery

| Skill | Когда |
|-------|--------|
| `orch-build-mvp` | Bootstrap MVP из спеки |
| `orch-add-feature` | Новая capability end-to-end |
| `orch-change-feature` | Меняем поведение (зелёные → новые тесты → impl) |
| `orch-fix-defect` | Баг: reproduce → fix → review |
| `orch-refine-code` | Refactor без смены поведения |
| `orch-pipeline` | Общий gated Research-Plan-TDD-Review-Commit |
| `tdd-workflow` | TDD, coverage bar |
| `verification-loop` | Verification system сессии |
| `delivery-gate` | Quality gate перед finish |
| `git-workflow` | Branch/commit/PR |
| `team-agent-orchestration` | Squad / Kanban agents |
| `dmux-workflows` | Multi-agent tmux panes |
| `parallel-execution-optimizer` | Параллельные lanes |
| `santa-method` | Adversarial dual review |
| `gateguard` | Gate перед Edit/Write/Bash |
| `safety-guard` | Destructive ops / prod safety |
| `production-audit` | Production readiness |
| `agentic-engineering` | Eval-first, decomposition, model routing |
| `loop-design-check` | Дизайн/ревью agent loops |
| `benchmark` | Performance baselines |

### E. UI design (Stitch) — mockups only

| Skill | Когда | MCP |
|-------|--------|-----|
| `enhance-prompt` | Размыть UI-идею → structured prompt | offline OK |
| `taste-design` | Premium anti-slop DESIGN.md | offline OK |
| `generate-design` | Экраны в Stitch | **нужен Stitch MCP** |
| `manage-design-system` | DESIGN.md → Stitch theme | MCP |
| `design-md` | DESIGN.md из Stitch project | MCP |
| `upload-to-stitch` | Upload HTML/PNG/DESIGN.md | MCP |
| `extract-design-md` | DESIGN.md из web source | обычно offline |
| `code-to-design` | HTML/frontend → Stitch loop | MCP |

**Исключено намеренно:** `react-components`, `react-native`, `shadcn-ui`, `stitch-loop`, …  
Реализация UI = **Compose**, не RN/React.

### F. Motion (Emil) — web recipes → Compose

| Skill | Когда |
|-------|--------|
| `emil-design-eng` | Frequency/purpose/easing decisions |
| `find-animation-opportunities` | Где motion нужен / где вреден |
| `review-animations` | Строгий review (+ `STANDARDS.md`) |
| `improve-animations` | Repo-wide audit → plans (read-only source) |
| `animation-vocabulary` | «Пружинка» → точный term |
| `apple-design` | Interruptibility, springs, 1:1 drag |

Compose mapping через `documentation-lookup`: `AnimatedVisibility`, `animate*AsState`, springs, gesture cancel.  
**Не** rewrite skill bodies.

### G. Security / quality / harness ops

| Skill | Когда |
|-------|--------|
| `security-review` | Auth, input, secrets, API, **до background location** |
| `click-path-audit` | Кнопки «работают по отдельности, ломают state» |
| `ai-regression-testing` | AI-blind spots / sandbox API tests |
| `agent-self-evaluation` | Scorecard после non-trivial task |
| `agent-introspection-debugging` | Agent failure recovery |
| `eval-harness` | EDD / formal evals |
| `prompt-optimizer` | Улучшить prompt (не выполнять задачу) |
| `ecc-guide` / `ecc-recipes` | Каталог ECC / pipeline recipes |
| `configure-ecc` / `agent-sort` | ECC install/sort (осторожно: у нас manual vendor) |
| `skill-scout` / `skill-stocktake` / `rules-distill` | Skills lifecycle |
| `continuous-learning-v2` | Instincts (hooks-heavy; project-scoped) |
| `strategic-compact` | Manual context compact |
| `knowledge-ops` | KB ingest/sync |
| `iterative-retrieval` | Progressive retrieval для subagents |
| `regex-vs-llm-structured-text` | Regex vs LLM parse |
| `ecc-guide` | live surface ECC |

### H. Полный alphabetical ECC set (69)

`accessibility`, `agent-introspection-debugging`, `agent-self-evaluation`, `agent-sort`, `agentic-engineering`, `ai-regression-testing`, `android-clean-architecture`, `architecture-decision-records`, `benchmark`, `blueprint`, `click-path-audit`, `code-tour`, `codebase-onboarding`, `coding-standards`, `compose-multiplatform-patterns`, `configure-ecc`, `content-hash-cache-pattern`, `context-budget`, `continuous-learning-v2`, `council`, `deep-research`, `delivery-gate`, `design-system`, `dmux-workflows`, `documentation-lookup`, `ecc-guide`, `ecc-recipes`, `error-handling`, `eval-harness`, `gateguard`, `git-workflow`, `hexagonal-architecture`, `intent-driven-development`, `iterative-retrieval`, `knowledge-ops`, `kotlin-coroutines-flows`, `kotlin-patterns`, `kotlin-testing`, `latency-critical-systems`, `loop-design-check`, `make-interfaces-feel-better`, `orch-add-feature`, `orch-build-mvp`, `orch-change-feature`, `orch-fix-defect`, `orch-pipeline`, `orch-refine-code`, `parallel-execution-optimizer`, `plan-canvas`, `plan-orchestrate`, `product-capability`, `product-lens`, `production-audit`, `prompt-optimizer`, `regex-vs-llm-structured-text`, `repo-scan`, `research-ops`, `rules-distill`, `safety-guard`, `santa-method`, `search-first`, `security-review`, `skill-scout`, `skill-stocktake`, `strategic-compact`, `tdd-workflow`, `team-agent-orchestration`, `verification-loop`, `workspace-surface-audit`.

---

## Agents (`.claude/agents/`)

Использовать через Task/delegation когда зона совпадает:

`kotlin-reviewer`, `kotlin-build-resolver`, `planner`, `architect`, `tdd-guide`, `code-reviewer`, `security-reviewer`, `build-error-resolver`, `doc-updater`, `docs-lookup`, `performance-optimizer`, `silent-failure-hunter`.

---

## Rules

Always-follow ECC packs:

- `.claude/rules/ecc/common/`
- `.claude/rules/ecc/kotlin/`

---

## Архитектура (scaffold)

```
app → presentation, domain, data
presentation → domain
data → domain
domain → (no Android)
```

Proposed expansion (packages first; Gradle modules later — ADR-0005):
`feature-session|map|history|weather` under presentation; `data-location|session|map|weather` under data.
- DI: **Koin**
- UI: Compose only; single-activity **NavHost** routes (`Session` → `Detail/{session_id}`; `Settings` planned)
- `applicationId` / namespace base: `com.skisnow.app`
- **LOCATION lane open** (ADR accepted); FGS/permissions only with security-review on the PR
- Domain ports: `SessionRepository`, `LocationTracker`, `StatsCalculator`, `WeatherRepository`
- Product docs: `docs/product/skisnow-intent.md`, `product-brief.md`, `competitive-research.md`, `capability-plan.md`, `stats-spec.md`
- ADRs: `docs/adr/0001`…`0006` (**accepted**)
- `capability-stub.md` → redirect to `capability-plan.md`

---

## Lifecycle (использовать skills по шагам)

1. Дополнить intent (user)  
2. `product-lens` → `product-capability` → `intent-driven-development`  
3. `taste-design` / `enhance-prompt` → DESIGN.md; если MCP: `manage-design-system` + `generate-design`  
4. `council` + `architecture-decision-records` (map / location / storage)  
5. `search-first` + `documentation-lookup`  
6. `orch-build-mvp` / `orch-add-feature` + Android/Kotlin skills  
7. Emil: `find-animation-opportunities` → impl → `review-animations`  
8. `kotlin-testing` + `tdd-workflow` + `verification-loop`  
9. `security-review` **до** background GPS  
10. Реальный GPS / map / stats / export  

---

## Do not

- Global `npx skills add` / ECC full installer вместо project vendor
- Nest skills под `.claude/skills/ecc/`
- Править vendored `SKILL.md` bodies
- React/RN/web dashboard codegen paths
- Игнорировать matching skill и писать «с нуля»
- Оставлять `AGENTS.md` устаревшим после project decision (MUST обновить)
- Молча делать задачу **без** skill-рекомендации (кроме trivial)

---

## Surfaces

| Path | Role |
|------|------|
| `.claude/skills/` | Primary skill root (OMP claude provider) |
| `.agents/skills/` | Dual root (OMP agents provider / Codex) |
| `.claude/rules/ecc/` | ECC rules |
| `.claude/agents/` | ECC agents |
| `docs/*-analysis.md` | Анализ трёх источников |
| `docs/product/` | Intent, brief, competitive research, capability, stats-spec |
| `vendor/*/` | Lists, LICENSE, SOURCE_COMMIT |
| `docs/adr/` | ADR drafts (map/location/storage/weather/modules/3D) |
| `scripts/vendor-all-skills.ps1` | Refresh vendor |

Analyses: `docs/ecc-analysis.md`, `docs/stitch-skills-analysis.md`, `docs/emil-skills-analysis.md`.

---

## AGENTS changelog

- 2026-07-21 — bootstrap: 83 skills, RU language, full skill routing, OMP discovery, living AGENTS
- 2026-07-21 — skill-first: на вопрос/задачу сначала рекомендовать 1–3 skills, потом read skill://
- 2026-07-21 — product research done; ADRs 0001–0006 proposed; LOCATION still closed; domain ports added
- 2026-07-21 — user accepted ADR-0001…0006; product prefs locked; LOCATION lane open with security-review gate
- 2026-07-21 — slice-0: Room + Fused/FGS + MapLibre 2D + Open-Meteo chip + Session UI; security checklist
- 2026-07-21 — PR#4 merged: session detail + elevation + NavHost navigation
