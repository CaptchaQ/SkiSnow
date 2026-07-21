# ECC Analysis for SkiSnow

Self-contained analysis of [affaan-m/ECC](https://github.com/affaan-m/ECC) as applied to the SkiSnow Android ski-tracking project.

**Vendored commit:** `5deee34c93395045b985e3baf91550e5f1ab7204` (see `vendor/ecc/SOURCE_COMMIT.txt`)  
**License:** MIT (`vendor/ecc/LICENSE`)  
**Observed surface on analysis clone:** ~278 skills, 67 agents, multi-language `rules/` packs

---

## 1. What ECC is

**Everything Claude Code (ECC)** is a harness-native agent operating system: skills, specialized agents, always-on rules, hooks, commands, and install tooling evolved for multi-harness agentic engineering (Claude Code, Codex, Cursor, OpenCode, Gemini, etc.).

| Fact | Value |
|------|--------|
| Source | https://github.com/affaan-m/ECC |
| Plugin slug | `ecc@ecc` |
| npm | `ecc-universal` (also `ecc-agentshield`) |
| Site | https://ecc.tools |
| License | MIT |

It is **not** a single framework library. It is an operator layer that steers coding agents toward TDD, security review, architecture, research-first work, and language-specific patterns (including Kotlin/Android).

---

## 2. Surface on main (relevant slices)

| Area | Path | Role for SkiSnow |
|------|------|------------------|
| Skills | `skills/` (~278) | Primary workflow surface; selective vendor into project |
| Agents | `agents/` (67+) | Domain reviewers (kotlin-reviewer, planner, tdd-guide, …) |
| Rules | `rules/common` + language packs (`kotlin/`, …) | Always-follow guidance; **not** shipped by Claude plugin |
| Hooks / commands / dashboard | not vendored here | Global runtime; SkiSnow stays project-local only |

**Kotlin pack under rules:** `coding-style.md`, `patterns.md`, `security.md`, `testing.md`, `hooks.md`.

---

## 3. Install models ECC recommends

From ECC README (install section):

1. **Plugin path (default for Claude Code):** `/plugin marketplace add` + `/plugin install ecc@ecc` loads skills/commands/hooks. **Rules are not distributed by the plugin** — copy `rules/common` + one language pack manually under `~/.claude/rules/ecc/` or project `.claude/rules/ecc/`.
2. **Manual installer:** `install.sh` / `install.ps1` / `npx ecc-install` with profiles (`minimal`, `full`, …). Use **instead of** plugin, not stacked on top.
3. **Do not stack** plugin + full installer — causes duplicate skills/hooks.
4. **Skills load flat:** Claude loads skills only from direct children of `~/.claude/skills` or project `.claude/skills`. Do **not** nest under `.claude/skills/ecc/`.

### SkiSnow choice

**Project-local manual vendor only** (this repo):

- Selected skill directories → `.claude/skills/<name>/` and `.agents/skills/<name>/`
- Rules → `.claude/rules/ecc/{common,kotlin}/`
- Agents → `.claude/agents/`
- No global `npx` / `install.sh --profile full` / marketplace install required

Refresh via `scripts/vendor-all-skills.ps1` (or `.sh`) against a shallow clone.

---

## 4. Selection matrix for SkiSnow

**INCLUDE: 69 skills** — canonical list in `vendor/ecc/skill-list.txt`.

### Why clusters

| Cluster | Skills (examples) | Ski-GPS rationale |
|---------|-------------------|-------------------|
| **Android core** | `android-clean-architecture`, `compose-multiplatform-patterns`, `kotlin-patterns`, `kotlin-coroutines-flows`, `kotlin-testing` | Native Compose + clean modules for map/session app |
| **Product / arch** | `product-lens`, `product-capability`, `intent-driven-development`, `hexagonal-architecture`, `architecture-decision-records`, `blueprint`, `search-first`, `documentation-lookup` | Map SDK / location / storage ADRs; no invented GPS product rules |
| **Quality** | `tdd-workflow`, `verification-loop`, `coding-standards`, `security-review`, `error-handling`, `git-workflow` | Cold-path reliability; location privacy later |
| **UI** | `design-system`, `make-interfaces-feel-better`, `accessibility`, `latency-critical-systems` | Glove-friendly UI; high-frequency GPS updates |
| **Orch** | full `orch-*`, `plan-canvas`, `plan-orchestrate` | MVP vertical slices without thrashing |
| **Agent ops** | `council`, research/eval/learning skills, `context-budget`, … | Multi-agent planning and harness hygiene |

### EXCLUDE (do not vendor)

- Web/JS framework packs (Vue, React web patterns, Next, etc.)
- Server backends (`kotlin-ktor*`, Spring, Django, Nest, …)
- iOS-only / Flutter / RN packs
- Domain ops (healthcare, DeFi, Itô prediction markets, …)
- Hooks, commands, dashboard GUI

Rationale: SkiSnow is **native Android Kotlin + Jetpack Compose**. Wrong-stack skills add noise and fight architecture.

---

## 5. Gaps (no ECC skill today)

ECC does **not** ship ready-made skills for:

- MapLibre / Google Maps / OSMDroid integration
- Foreground location services / battery policy
- Ski-specific stats (vertical drop, piste segments)
- Offline map tiles / GPX export UX

**Covered later via:** `search-first` + `documentation-lookup` + `product-capability` + ADRs (`architecture-decision-records`), then implementation under Android/Kotlin skills. Map/GPS feature code is intentionally out of scope for the bootstrap iteration.

---

## 6. Vendor layout + lifecycle

### Layout in SkiSnow

| Path | Content |
|------|---------|
| `.claude/skills/<name>/` | 69 ECC + Stitch + Emil (flat) |
| `.agents/skills/<name>/` | Dual surface for Codex-class harnesses |
| `.claude/rules/ecc/common/` | Common rules |
| `.claude/rules/ecc/kotlin/` | Kotlin rules |
| `.claude/agents/` | 12 selected agents |
| `vendor/ecc/` | Lists, LICENSE, SOURCE_COMMIT, provenance |

### Agents vendored

```
kotlin-reviewer.md
kotlin-build-resolver.md
planner.md
architect.md
tdd-guide.md
code-reviewer.md
security-reviewer.md
build-error-resolver.md
doc-updater.md
docs-lookup.md
performance-optimizer.md
silent-failure-hunter.md
```

### Lifecycle after vendor (recommended order)

1. User expands product intent (`docs/product/skisnow-intent.md`)
2. `product-lens` → `product-capability` → `intent-driven-development`
3. Stitch design loop (optional MCP) → DESIGN.md / mockups
4. `council` + `architecture-decision-records` → map SDK, location, storage
5. `search-first` + `documentation-lookup` → libraries
6. `orch-build-mvp` or vertical slice (fake track → list → detail)
7. Emil motion skills for session UI; `review-animations` on motion PRs
8. `kotlin-testing` + `tdd-workflow` + `verification-loop`
9. `security-review` before background location
10. Real GPS, map, stats, export, offline

### Refresh

```powershell
# Re-clone if temp vanished
git clone --depth 1 https://github.com/affaan-m/ECC.git $env:TEMP\ecc-analysis

.\scripts\vendor-all-skills.ps1 -EccRoot $env:TEMP\ecc-analysis
```

**Never edit** vendored `SKILL.md` bodies in-tree; re-vendor from upstream.

---

## 7. Anchors used for scaffold

| Skill | Anchor | Use |
|-------|--------|-----|
| `android-clean-architecture` | Module Structure + Dependency Rules | `app` / `domain` / `data` / `presentation` graph |
| `kotlin-coroutines-flows` | StateFlow / WhileSubscribed | Future GPS stream model |
| `product-capability` | Capability contract sections | `docs/product/capability-stub.md` shape |
