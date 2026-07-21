# SkiSnow Vendor Inventory

Generated: 2026-07-21T05:52:04.6723008+05:00

## Skill counts (per root)

| Source | Count |
|--------|------:|
| ECC | 69 |
| Stitch | 8 |
| Emil Kowalski | 6 |
| **Total** | **83** |

Observed directories:
- .claude/skills: 83
- .agents/skills: 83

Expected total: **83** (69 + 8 + 6).

## ECC rules

Packs under .claude/rules/ecc/:
- common
- kotlin


## ECC agents

Files under .claude/agents/:
- architect.md
- build-error-resolver.md
- code-reviewer.md
- docs-lookup.md
- doc-updater.md
- kotlin-build-resolver.md
- kotlin-reviewer.md
- performance-optimizer.md
- planner.md
- security-reviewer.md
- silent-failure-hunter.md
- tdd-guide.md


## Provenance

| Source | Commit file |
|--------|-------------|
| ECC | vendor/ecc/SOURCE_COMMIT.txt |
| Stitch | vendor/stitch-skills/SOURCE_COMMIT.txt |
| Emil | vendor/emilkowalski-skills/SOURCE_COMMIT.txt |

## Skill lists

- vendor/ecc/skill-list.txt
- vendor/stitch-skills/skill-list.txt (+ plugin-map.txt)
- vendor/emilkowalski-skills/skill-list.txt

## Notes

- Skills are flat under skill roots (no source nesting).
- Vendored SKILL bodies must not be edited in-project; re-run this script to refresh.
- Stitch MCP is optional for vendoring; generation skills need MCP at runtime.
