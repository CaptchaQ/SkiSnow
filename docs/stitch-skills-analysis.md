# Stitch Skills Analysis for SkiSnow

Self-contained analysis of [google-labs-code/stitch-skills](https://github.com/google-labs-code/stitch-skills) for SkiSnow.

**Vendored commit:** `ad4b8bc8c51991f53214b573c98eb4f46807e178` (see `vendor/stitch-skills/SOURCE_COMMIT.txt`)  
**License:** Apache-2.0 (`vendor/stitch-skills/LICENSE`)  
**Product:** [Google Stitch](https://stitch.withgoogle.com)

---

## 1. What it is

Agent Skills library for **Google Stitch** design tooling, following the [Agent Skills](https://agentskills.io) open standard. Compatible with Codex, Claude Code, Cursor, Gemini CLI, and similar harnesses.

**Most design/build skills require Stitch MCP** configured in the agent environment ([MCP setup](https://stitch.withgoogle.com/docs/mcp/setup/)). Offline drafting is still possible with `enhance-prompt` and `taste-design` for DESIGN.md text.

SkiSnow uses Stitch for **mobile UI mockups + DESIGN.md**, not for generating production React/RN code. Implementation stays **Jetpack Compose** via ECC Android skills.

---

## 2. Plugin structure

| Plugin | Path | Role |
|--------|------|------|
| `stitch-design` | `plugins/stitch-design/` | Design workflows (generate, manage DS, extract, upload) |
| `stitch-build` | `plugins/stitch-build/` | Code generation from designs (React, RN, remotion, …) |
| `stitch-utilities` | `plugins/stitch-utilities/` | DESIGN.md, prompt polish, loops, taste |

Marketplace catalog: `.agents/plugins/marketplace.json` lists the three plugins.

Upstream install options (SkiSnow does **not** use these globally):

- `npx skills add google-labs-code/stitch-skills`
- Codex marketplace sparse paths
- Claude: `npx plugins add google-labs-code/stitch-skills --scope project`

**SkiSnow path:** manual vendor of selected skill directories only.

---

## 3. Full inventory (15 skills)

| Folder | Frontmatter `name` | Plugin | Purpose |
|--------|--------------------|--------|---------|
| `generate-design` | `stitch::generate-design` | design | Screens from text/images, edit, variants via Stitch MCP |
| `manage-design-system` | `stitch::manage-design-system` | design | Create/apply Stitch design systems from DESIGN.md |
| `code-to-design` | `stitch::code-to-design` | design | Frontend code → Stitch design (HTML extract + upload) |
| `extract-design-md` | `stitch::extract-design-md` | design | DESIGN.md from frontend source (web frameworks) |
| `extract-static-html` | `stitch::extract-static-html` | design | Self-contained HTML snapshot of web UI |
| `upload-to-stitch` | `stitch::upload-to-stitch` | design | Upload HTML/images/DESIGN.md to Stitch project |
| `react-components` | `stitch::react-components` | build | Stitch → React/Vite components + validate scripts |
| `react-native` | `stitch::react-native` | build | Stitch → React Native StyleSheet components |
| `react-vite-dashboard` | `react-vite-dashboard` | build | Stitch → React+Vite dashboard patterns |
| `remotion` | `remotion` | build | Walkthrough videos from Stitch via Remotion |
| `shadcn-ui` | `shadcn-ui` | build | shadcn/ui integration guidance |
| `design-md` | `design-md` | utilities | Synthesize DESIGN.md from Stitch project assets |
| `enhance-prompt` | `enhance-prompt` | utilities | Polish vague UI prompts for Stitch (works offline for drafting) |
| `stitch-loop` | `stitch-loop` | utilities | Autonomous multi-page website baton loop |
| `taste-design` | `taste-design` | utilities | Premium anti-slop DESIGN.md for Stitch |

---

## 4. Skill anatomy

Each skill is a **directory**:

```
<skill>/
  SKILL.md
  scripts/      # optional
  resources/    # optional
  examples/     # optional
  references/   # optional
```

**Copy whole directories.** Destination folder name = **source folder name** (kebab-case), not the `name:` frontmatter when it contains `::` (e.g. frontmatter `stitch::generate-design` → folder `generate-design`).

---

## 5. Selection for SkiSnow

Stack decision: **native Compose**, not React/RN.

### INCLUDE (8) — `vendor/stitch-skills/skill-list.txt`

| Skill | Why for SkiSnow |
|-------|-----------------|
| `generate-design` | Mock ski map / session / history screens in Stitch (mobile prompts) before Compose |
| `manage-design-system` | Push snow/night piste DESIGN.md into Stitch project theme |
| `design-md` | Pull semantic DESIGN.md from Stitch screens once mockups exist |
| `taste-design` | Anti-slop DESIGN.md (no purple AI chrome; dense cockpit-capable ski HUD) |
| `enhance-prompt` | Turn “экран карты с треком” into Stitch-grade structured prompts |
| `upload-to-stitch` | Upload local mock HTML/PNG/DESIGN.md |
| `extract-design-md` | If web marketing site or HTML mock appears later, reverse design tokens |
| `code-to-design` | Optional HTML mock → Stitch; keeps design loop complete |

### EXCLUDE (7)

| Skill | Why exclude |
|-------|-------------|
| `react-components` | Wrong stack (React web); SkiSnow = Compose |
| `react-native` | Wrong stack; would fight Compose decision |
| `react-vite-dashboard` | Web dashboard / Web3 — out of scope |
| `shadcn-ui` | Web component system |
| `remotion` | Marketing video tooling — later optional |
| `stitch-loop` | Multi-page **website** baton loop — not Android app |
| `extract-static-html` | Running web app snapshot — no web app yet |

### Plugin map for re-copy — `vendor/stitch-skills/plugin-map.txt`

```
generate-design=plugins/stitch-design/skills/generate-design
manage-design-system=plugins/stitch-design/skills/manage-design-system
code-to-design=plugins/stitch-design/skills/code-to-design
extract-design-md=plugins/stitch-design/skills/extract-design-md
upload-to-stitch=plugins/stitch-design/skills/upload-to-stitch
design-md=plugins/stitch-utilities/skills/design-md
enhance-prompt=plugins/stitch-utilities/skills/enhance-prompt
taste-design=plugins/stitch-utilities/skills/taste-design
```

---

## 6. Prerequisites / contingency

| Situation | Action |
|-----------|--------|
| Stitch MCP credentials present | Full generate/manage/upload/design-md loop |
| MCP absent | Skills still vendored; document in README that generate/manage/upload need MCP |
| Offline DESIGN.md drafting | `enhance-prompt` + `taste-design` → hand-written `.stitch/DESIGN.md` later |

Optional later path (not required this bootstrap): `.stitch/DESIGN.md` via taste/enhance when UI design starts.

---

## 7. Anchor skills

| Skill | Anchor | Use |
|-------|--------|-----|
| `generate-design` | Prompt Enhancement Pipeline | Mobile mock generation |
| `taste-design` | Atmosphere / anti-patterns | Ski DESIGN.md quality bar |
