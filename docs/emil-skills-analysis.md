# Emil Kowalski Skills Analysis for SkiSnow

Self-contained analysis of [emilkowalski/skills](https://github.com/emilkowalski/skills) for SkiSnow.

**Vendored commit:** `6bf24434f7730ad169077756cf9c7cd7bd675fc6` (see `vendor/emilkowalski-skills/SOURCE_COMMIT.txt`)  
**License:** MIT (`vendor/emilkowalski-skills/LICENSE`)  
**Site / course:** [animations.dev](https://animations.dev/)  
**Install upstream:** `npx skills@latest add emilkowalski/skills` (SkiSnow uses **manual vendor** instead)

---

## 1. What it is

“Skills for Design Engineers” by Emil Kowalski (craft sensibility from work at Vercel/Linear). Focus: UI polish, animation decision-making, motion review, and anti-slop interface taste. Skills amplify domain expertise rather than replace it.

All six skills are **INCLUDE** for SkiSnow (`vendor/emilkowalski-skills/skill-list.txt`).

---

## 2. Complete skill set

| Folder | Purpose |
|--------|---------|
| `emil-design-eng` | Core UI polish + animation decision framework (frequency / purpose / easing) |
| `review-animations` | Strict motion review; loads `STANDARDS.md`; default flag on motion PRs |
| `improve-animations` | Repo-wide motion audit → prioritized plans in `plans/` (read-only on source) |
| `find-animation-opportunities` | Propose where to add motion (and reject over-animation) |
| `animation-vocabulary` | Vague motion description → precise term glossary |
| `apple-design` | Apple fluid UI principles (interruptibility, springs, 1:1 drag) translated for implementation |

Companion files are part of whole-directory copy:

- `review-animations/STANDARDS.md` — ten non-negotiable motion standards
- `improve-animations/AUDIT.md`, `PLAN-TEMPLATE.md`

---

## 3. Web-native examples, Compose application

Skill bodies use **CSS / Motion / web metaphors** (easing curves, `transform`, enter/exit patterns). SkiSnow implements UI in **Jetpack Compose**.

**Policy:**

- Do **not** rewrite skill bodies in-tree.
- Map principles to Compose APIs via `documentation-lookup` and ECC Android skills:
  - `AnimatedVisibility`, `animate*AsState`, `Animatable`, spring specs
  - Gesture interruptibility (`Modifier.pointerInput`, nested scroll)
  - Reduced-motion / accessibility via Compose accessibility APIs
- Note adaptation expectations in root `AGENTS.md`.

Principles that transfer cleanly:

| Emil idea | Compose mapping (implementer duty) |
|-----------|-------------------------------------|
| Frequency / purpose before animating | Avoid animating every GPS tick; animate session start/stop, sheet open |
| ease-out on enter, not ease-in | Spring / tween specs with fast response |
| Interruptibility | Cancel/join animations on gesture |
| Nothing from scale(0) only | Opacity + small scale |
| Press feedback | InteractionSource / scale on press |

---

## 4. Why all six for a ski app

| Need | Skill |
|------|-------|
| Cold-hands UI: large targets, clear press feedback | `emil-design-eng`, `apple-design` |
| Start/pause/stop session feedback | `find-animation-opportunities`, `animation-vocabulary` |
| Map chrome + bottom sheets | `apple-design` (1:1 drag, springs) |
| Restraint on high-frequency GPS UI updates | Decision framework in `emil-design-eng` |
| Motion PR quality bar | `review-animations` + `STANDARDS.md` |
| Repo-wide polish later | `improve-animations` |

Over-animating live map position or stats counters would hurt battery, readability, and gloves-on usability — these skills help **reject** bad motion as much as add good motion.

---

## 5. Vendor layout

| Path | Role |
|------|------|
| `.claude/skills/<name>/` | All 6 Emil skill dirs (flat) |
| `.agents/skills/<name>/` | Dual surface |
| `vendor/emilkowalski-skills/` | skill-list, LICENSE, SOURCE_COMMIT |

Refresh:

```powershell
git clone --depth 1 https://github.com/emilkowalski/skills.git $env:TEMP\emilkowalski-skills-analysis
.\scripts\vendor-all-skills.ps1 -EmilRoot $env:TEMP\emilkowalski-skills-analysis
```

---

## 6. Anchors

| Skill | Anchor | Use |
|-------|--------|-----|
| `emil-design-eng` | Animation Decision Framework | Frequency/easing for session UI |
| `review-animations` | `STANDARDS.md` Ten Non-Negotiable Standards | Motion review bar |
