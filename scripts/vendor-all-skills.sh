#!/usr/bin/env bash
# Vendor selected ECC + Stitch + Emil skills into SkiSnow project skill roots.
# Mirrors scripts/vendor-all-skills.ps1
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${PROJECT_ROOT:-$(cd "$SCRIPT_DIR/.." && pwd)}"
ECC_ROOT="${ECC_ROOT:-${TEMP:-/tmp}/ecc-analysis}"
STITCH_ROOT="${STITCH_SKILLS_ROOT:-${TEMP:-/tmp}/stitch-skills-analysis}"
EMIL_ROOT="${EMIL_SKILLS_ROOT:-${TEMP:-/tmp}/emilkowalski-skills-analysis}"

info() { echo "[vendor] $*"; }
fail() { echo "[vendor] ERROR: $*" >&2; exit 1; }

git_head() {
  local root="$1"
  if [[ -d "$root/.git" ]]; then
    git -C "$root" rev-parse HEAD 2>/dev/null || echo "unknown"
  else
    echo "unknown (no .git)"
  fi
}

copy_skill() {
  local src="$1" name="$2" claude="$3" agents="$4"
  [[ -f "$src/SKILL.md" ]] || fail "Missing SKILL.md for skill '$name' at $src"
  rm -rf "$claude/$name" "$agents/$name"
  mkdir -p "$claude" "$agents"
  cp -R "$src" "$claude/$name"
  cp -R "$src" "$agents/$name"
}

info "ProjectRoot=$PROJECT_ROOT"
info "EccRoot=$ECC_ROOT"
info "StitchRoot=$STITCH_ROOT"
info "EmilRoot=$EMIL_ROOT"

for root in "$ECC_ROOT" "$STITCH_ROOT" "$EMIL_ROOT"; do
  [[ -d "$root" ]] || fail "Source root not found: $root"
done

CLAUDE_SKILLS="$PROJECT_ROOT/.claude/skills"
AGENTS_SKILLS="$PROJECT_ROOT/.agents/skills"
CLAUDE_RULES="$PROJECT_ROOT/.claude/rules/ecc"
CLAUDE_AGENTS="$PROJECT_ROOT/.claude/agents"
VENDOR_ECC="$PROJECT_ROOT/vendor/ecc"
VENDOR_STITCH="$PROJECT_ROOT/vendor/stitch-skills"
VENDOR_EMIL="$PROJECT_ROOT/vendor/emilkowalski-skills"

mkdir -p "$CLAUDE_SKILLS" "$AGENTS_SKILLS" "$CLAUDE_RULES" "$CLAUDE_AGENTS" \
  "$VENDOR_ECC" "$VENDOR_STITCH" "$VENDOR_EMIL"

# ECC skills
mapfile -t ECC_SKILLS < <(grep -v '^[[:space:]]*$' "$VENDOR_ECC/skill-list.txt")
info "Copying ${#ECC_SKILLS[@]} ECC skills..."
for name in "${ECC_SKILLS[@]}"; do
  src="$ECC_ROOT/skills/$name"
  [[ -d "$src" ]] || fail "ECC skill directory missing: $src"
  copy_skill "$src" "$name" "$CLAUDE_SKILLS" "$AGENTS_SKILLS"
done

# ECC rules
mapfile -t RULE_PACKS < <(grep -v '^[[:space:]]*$' "$VENDOR_ECC/rule-list.txt")
info "Copying ECC rule packs..."
for pack in "${RULE_PACKS[@]}"; do
  src="$ECC_ROOT/rules/$pack"
  [[ -d "$src" ]] || fail "ECC rules pack missing: $src"
  rm -rf "$CLAUDE_RULES/$pack"
  cp -R "$src" "$CLAUDE_RULES/$pack"
done

# ECC agents
mapfile -t AGENTS < <(grep -v '^[[:space:]]*$' "$VENDOR_ECC/agent-list.txt")
info "Copying ${#AGENTS[@]} ECC agents..."
for agent in "${AGENTS[@]}"; do
  src="$ECC_ROOT/agents/$agent"
  [[ -f "$src" ]] || fail "ECC agent missing: $src"
  cp "$src" "$CLAUDE_AGENTS/$agent"
done

[[ -f "$ECC_ROOT/LICENSE" ]] && cp "$ECC_ROOT/LICENSE" "$VENDOR_ECC/LICENSE"
{
  git_head "$ECC_ROOT"
  echo "source=https://github.com/affaan-m/ECC"
  echo "copied=$(date -Iseconds 2>/dev/null || date)"
} > "$VENDOR_ECC/SOURCE_COMMIT.txt"

# Stitch
declare -A PLUGIN_MAP
while IFS='=' read -r key val; do
  [[ -z "${key// }" ]] && continue
  PLUGIN_MAP["$key"]="$val"
done < "$VENDOR_STITCH/plugin-map.txt"

mapfile -t STITCH_SKILLS < <(grep -v '^[[:space:]]*$' "$VENDOR_STITCH/skill-list.txt")
info "Copying ${#STITCH_SKILLS[@]} Stitch skills..."
for name in "${STITCH_SKILLS[@]}"; do
  rel="${PLUGIN_MAP[$name]:-}"
  [[ -n "$rel" ]] || fail "No plugin-map entry for stitch skill '$name'"
  src="$STITCH_ROOT/$rel"
  [[ -d "$src" ]] || fail "Stitch skill directory missing: $src"
  copy_skill "$src" "$name" "$CLAUDE_SKILLS" "$AGENTS_SKILLS"
done

[[ -f "$STITCH_ROOT/LICENSE" ]] && cp "$STITCH_ROOT/LICENSE" "$VENDOR_STITCH/LICENSE"
{
  git_head "$STITCH_ROOT"
  echo "source=https://github.com/google-labs-code/stitch-skills"
  echo "copied=$(date -Iseconds 2>/dev/null || date)"
} > "$VENDOR_STITCH/SOURCE_COMMIT.txt"

# Emil
mapfile -t EMIL_SKILLS < <(grep -v '^[[:space:]]*$' "$VENDOR_EMIL/skill-list.txt")
info "Copying ${#EMIL_SKILLS[@]} Emil skills..."
for name in "${EMIL_SKILLS[@]}"; do
  src="$EMIL_ROOT/skills/$name"
  [[ -d "$src" ]] || fail "Emil skill directory missing: $src"
  copy_skill "$src" "$name" "$CLAUDE_SKILLS" "$AGENTS_SKILLS"
done

[[ -f "$EMIL_ROOT/LICENSE" ]] && cp "$EMIL_ROOT/LICENSE" "$VENDOR_EMIL/LICENSE"
{
  git_head "$EMIL_ROOT"
  echo "source=https://github.com/emilkowalski/skills"
  echo "copied=$(date -Iseconds 2>/dev/null || date)"
} > "$VENDOR_EMIL/SOURCE_COMMIT.txt"

CLAUDE_COUNT=$(find "$CLAUDE_SKILLS" -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
AGENTS_COUNT=$(find "$AGENTS_SKILLS" -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
TOTAL=$(( ${#ECC_SKILLS[@]} + ${#STITCH_SKILLS[@]} + ${#EMIL_SKILLS[@]} ))

cat > "$PROJECT_ROOT/vendor/INVENTORY.md" <<EOF
# SkiSnow Vendor Inventory

Generated: $(date -Iseconds 2>/dev/null || date)

## Skill counts (per root)

| Source | Count |
|--------|------:|
| ECC | ${#ECC_SKILLS[@]} |
| Stitch | ${#STITCH_SKILLS[@]} |
| Emil Kowalski | ${#EMIL_SKILLS[@]} |
| **Total** | **$TOTAL** |

Observed directories:
- \`.claude/skills\`: $CLAUDE_COUNT
- \`.agents/skills\`: $AGENTS_COUNT

Expected total: **83** (69 + 8 + 6).

## Provenance

| Source | Commit file |
|--------|-------------|
| ECC | vendor/ecc/SOURCE_COMMIT.txt |
| Stitch | vendor/stitch-skills/SOURCE_COMMIT.txt |
| Emil | vendor/emilkowalski-skills/SOURCE_COMMIT.txt |

## Notes

- Skills are flat under skill roots (no source nesting).
- Vendored SKILL bodies must not be edited in-project; re-run this script to refresh.
- Stitch MCP is optional for vendoring; generation skills need MCP at runtime.
EOF

if [[ "$CLAUDE_COUNT" != "83" || "$AGENTS_COUNT" != "83" ]]; then
  fail "Expected 83 skills in each root; got claude=$CLAUDE_COUNT agents=$AGENTS_COUNT"
fi

info "Done. Inventories written. claude=$CLAUDE_COUNT agents=$AGENTS_COUNT"
