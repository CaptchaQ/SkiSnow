#Requires -Version 5.1
<#
.SYNOPSIS
  Vendor selected ECC + Stitch + Emil skills into SkiSnow project skill roots.

.DESCRIPTION
  Copies whole skill directories into both:
    .claude/skills/<name>/
    .agents/skills/<name>/
  Also vendors ECC rules/agents and writes provenance under vendor/.

  Never edits skill bodies. Fails if any listed skill is missing SKILL.md.
#>
[CmdletBinding()]
param(
  [string]$EccRoot = $(if ($env:ECC_ROOT) { $env:ECC_ROOT } else { Join-Path $env:TEMP 'ecc-analysis' }),
  [string]$StitchRoot = $(if ($env:STITCH_SKILLS_ROOT) { $env:STITCH_SKILLS_ROOT } else { Join-Path $env:TEMP 'stitch-skills-analysis' }),
  [string]$EmilRoot = $(if ($env:EMIL_SKILLS_ROOT) { $env:EMIL_SKILLS_ROOT } else { Join-Path $env:TEMP 'emilkowalski-skills-analysis' }),
  [string]$ProjectRoot
)

if (-not $ProjectRoot) {
  if ($PSScriptRoot) {
    $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
  } else {
    $ProjectRoot = (Get-Location).Path
  }
}

$ErrorActionPreference = 'Stop'

function Write-Info([string]$Message) { Write-Host "[vendor] $Message" }
function Write-Fail([string]$Message) { throw "[vendor] ERROR: $Message" }

function Ensure-Dir([string]$Path) {
  if (-not (Test-Path -LiteralPath $Path)) {
    New-Item -ItemType Directory -Path $Path -Force | Out-Null
  }
}

function Copy-SkillDir {
  param(
    [Parameter(Mandatory)][string]$SourceDir,
    [Parameter(Mandatory)][string]$Name,
    [Parameter(Mandatory)][string]$ClaudeSkills,
    [Parameter(Mandatory)][string]$AgentsSkills
  )
  $skillMd = Join-Path $SourceDir 'SKILL.md'
  if (-not (Test-Path -LiteralPath $skillMd)) {
    Write-Fail "Missing SKILL.md for skill '$Name' at $SourceDir"
  }
  $destClaude = Join-Path $ClaudeSkills $Name
  $destAgents = Join-Path $AgentsSkills $Name
  if (Test-Path -LiteralPath $destClaude) { Remove-Item -LiteralPath $destClaude -Recurse -Force }
  if (Test-Path -LiteralPath $destAgents) { Remove-Item -LiteralPath $destAgents -Recurse -Force }
  Copy-Item -LiteralPath $SourceDir -Destination $destClaude -Recurse -Force
  Copy-Item -LiteralPath $SourceDir -Destination $destAgents -Recurse -Force
}

function Get-GitHead([string]$RepoRoot) {
  if (-not (Test-Path -LiteralPath (Join-Path $RepoRoot '.git'))) {
    return 'unknown (no .git)'
  }
  Push-Location $RepoRoot
  try {
    $head = (git rev-parse HEAD 2>$null)
    if (-not $head) { return 'unknown' }
    return $head.Trim()
  } finally {
    Pop-Location
  }
}

function Copy-IfExists([string]$Source, [string]$Dest) {
  if (Test-Path -LiteralPath $Source) {
    Ensure-Dir (Split-Path -Parent $Dest)
    Copy-Item -LiteralPath $Source -Destination $Dest -Force
  }
}

Write-Info "ProjectRoot=$ProjectRoot"
Write-Info "EccRoot=$EccRoot"
Write-Info "StitchRoot=$StitchRoot"
Write-Info "EmilRoot=$EmilRoot"

foreach ($root in @($EccRoot, $StitchRoot, $EmilRoot)) {
  if (-not (Test-Path -LiteralPath $root)) {
    Write-Fail "Source root not found: $root"
  }
}

$claudeSkills = Join-Path $ProjectRoot '.claude/skills'
$agentsSkills = Join-Path $ProjectRoot '.agents/skills'
$claudeRules = Join-Path $ProjectRoot '.claude/rules/ecc'
$claudeAgents = Join-Path $ProjectRoot '.claude/agents'
$vendorEcc = Join-Path $ProjectRoot 'vendor/ecc'
$vendorStitch = Join-Path $ProjectRoot 'vendor/stitch-skills'
$vendorEmil = Join-Path $ProjectRoot 'vendor/emilkowalski-skills'

Ensure-Dir $claudeSkills
Ensure-Dir $agentsSkills
Ensure-Dir $claudeRules
Ensure-Dir $claudeAgents
Ensure-Dir $vendorEcc
Ensure-Dir $vendorStitch
Ensure-Dir $vendorEmil

# --- ECC skills ---
$eccSkillList = Join-Path $vendorEcc 'skill-list.txt'
if (-not (Test-Path -LiteralPath $eccSkillList)) { Write-Fail "Missing $eccSkillList" }
$eccSkills = Get-Content -LiteralPath $eccSkillList | Where-Object { $_.Trim() -ne '' } | ForEach-Object { $_.Trim() }
Write-Info "Copying $($eccSkills.Count) ECC skills..."
foreach ($name in $eccSkills) {
  $src = Join-Path $EccRoot "skills/$name"
  if (-not (Test-Path -LiteralPath $src)) { Write-Fail "ECC skill directory missing: $src" }
  Copy-SkillDir -SourceDir $src -Name $name -ClaudeSkills $claudeSkills -AgentsSkills $agentsSkills
}

# --- ECC rules ---
$ruleListPath = Join-Path $vendorEcc 'rule-list.txt'
$rulePacks = Get-Content -LiteralPath $ruleListPath | Where-Object { $_.Trim() -ne '' } | ForEach-Object { $_.Trim() }
Write-Info "Copying ECC rule packs: $($rulePacks -join ', ')"
foreach ($pack in $rulePacks) {
  $src = Join-Path $EccRoot "rules/$pack"
  if (-not (Test-Path -LiteralPath $src)) { Write-Fail "ECC rules pack missing: $src" }
  $dest = Join-Path $claudeRules $pack
  if (Test-Path -LiteralPath $dest) { Remove-Item -LiteralPath $dest -Recurse -Force }
  Copy-Item -LiteralPath $src -Destination $dest -Recurse -Force
}

# --- ECC agents ---
$agentListPath = Join-Path $vendorEcc 'agent-list.txt'
$agents = Get-Content -LiteralPath $agentListPath | Where-Object { $_.Trim() -ne '' } | ForEach-Object { $_.Trim() }
Write-Info "Copying $($agents.Count) ECC agents..."
foreach ($agentFile in $agents) {
  $src = Join-Path $EccRoot "agents/$agentFile"
  if (-not (Test-Path -LiteralPath $src)) { Write-Fail "ECC agent missing: $src" }
  Copy-Item -LiteralPath $src -Destination (Join-Path $claudeAgents $agentFile) -Force
}

Copy-IfExists (Join-Path $EccRoot 'LICENSE') (Join-Path $vendorEcc 'LICENSE')
Set-Content -LiteralPath (Join-Path $vendorEcc 'SOURCE_COMMIT.txt') -Value (Get-GitHead $EccRoot) -NoNewline
Add-Content -LiteralPath (Join-Path $vendorEcc 'SOURCE_COMMIT.txt') -Value "`n"
Add-Content -LiteralPath (Join-Path $vendorEcc 'SOURCE_COMMIT.txt') -Value "source=https://github.com/affaan-m/ECC`n"
Add-Content -LiteralPath (Join-Path $vendorEcc 'SOURCE_COMMIT.txt') -Value "copied=$(Get-Date -Format o)`n"

# --- Stitch skills ---
$stitchList = Join-Path $vendorStitch 'skill-list.txt'
$pluginMapPath = Join-Path $vendorStitch 'plugin-map.txt'
if (-not (Test-Path -LiteralPath $pluginMapPath)) { Write-Fail "Missing $pluginMapPath" }
$map = @{}
Get-Content -LiteralPath $pluginMapPath | Where-Object { $_.Trim() -ne '' } | ForEach-Object {
  $parts = $_.Split('=', 2)
  if ($parts.Count -ne 2) { Write-Fail "Bad plugin-map line: $_" }
  $map[$parts[0].Trim()] = $parts[1].Trim()
}
$stitchSkills = Get-Content -LiteralPath $stitchList | Where-Object { $_.Trim() -ne '' } | ForEach-Object { $_.Trim() }
Write-Info "Copying $($stitchSkills.Count) Stitch skills..."
foreach ($name in $stitchSkills) {
  if (-not $map.ContainsKey($name)) { Write-Fail "No plugin-map entry for stitch skill '$name'" }
  $src = Join-Path $StitchRoot $map[$name]
  if (-not (Test-Path -LiteralPath $src)) { Write-Fail "Stitch skill directory missing: $src" }
  Copy-SkillDir -SourceDir $src -Name $name -ClaudeSkills $claudeSkills -AgentsSkills $agentsSkills
}
Copy-IfExists (Join-Path $StitchRoot 'LICENSE') (Join-Path $vendorStitch 'LICENSE')
Set-Content -LiteralPath (Join-Path $vendorStitch 'SOURCE_COMMIT.txt') -Value (Get-GitHead $StitchRoot) -NoNewline
Add-Content -LiteralPath (Join-Path $vendorStitch 'SOURCE_COMMIT.txt') -Value "`n"
Add-Content -LiteralPath (Join-Path $vendorStitch 'SOURCE_COMMIT.txt') -Value "source=https://github.com/google-labs-code/stitch-skills`n"
Add-Content -LiteralPath (Join-Path $vendorStitch 'SOURCE_COMMIT.txt') -Value "copied=$(Get-Date -Format o)`n"

# --- Emil skills ---
$emilList = Join-Path $vendorEmil 'skill-list.txt'
$emilSkills = Get-Content -LiteralPath $emilList | Where-Object { $_.Trim() -ne '' } | ForEach-Object { $_.Trim() }
Write-Info "Copying $($emilSkills.Count) Emil skills..."
foreach ($name in $emilSkills) {
  $src = Join-Path $EmilRoot "skills/$name"
  if (-not (Test-Path -LiteralPath $src)) { Write-Fail "Emil skill directory missing: $src" }
  Copy-SkillDir -SourceDir $src -Name $name -ClaudeSkills $claudeSkills -AgentsSkills $agentsSkills
}
Copy-IfExists (Join-Path $EmilRoot 'LICENSE') (Join-Path $vendorEmil 'LICENSE')
Set-Content -LiteralPath (Join-Path $vendorEmil 'SOURCE_COMMIT.txt') -Value (Get-GitHead $EmilRoot) -NoNewline
Add-Content -LiteralPath (Join-Path $vendorEmil 'SOURCE_COMMIT.txt') -Value "`n"
Add-Content -LiteralPath (Join-Path $vendorEmil 'SOURCE_COMMIT.txt') -Value "source=https://github.com/emilkowalski/skills`n"
Add-Content -LiteralPath (Join-Path $vendorEmil 'SOURCE_COMMIT.txt') -Value "copied=$(Get-Date -Format o)`n"

# --- Inventory ---
$claudeCount = (Get-ChildItem -LiteralPath $claudeSkills -Directory).Count
$agentsCount = (Get-ChildItem -LiteralPath $agentsSkills -Directory).Count
$agentFiles = (Get-ChildItem -LiteralPath $claudeAgents -File -Filter '*.md').Name | Sort-Object
$ruleDirs = (Get-ChildItem -LiteralPath $claudeRules -Directory).Name | Sort-Object

$inventory = @"
# SkiSnow Vendor Inventory

Generated: $(Get-Date -Format o)

## Skill counts (per root)

| Source | Count |
|--------|------:|
| ECC | $($eccSkills.Count) |
| Stitch | $($stitchSkills.Count) |
| Emil Kowalski | $($emilSkills.Count) |
| **Total** | **$($eccSkills.Count + $stitchSkills.Count + $emilSkills.Count)** |

Observed directories:
- `.claude/skills`: $claudeCount
- `.agents/skills`: $agentsCount

Expected total: **83** (69 + 8 + 6).

## ECC rules

Packs under `.claude/rules/ecc/`:
$($ruleDirs | ForEach-Object { "- $_" } | Out-String)

## ECC agents

Files under `.claude/agents/`:
$($agentFiles | ForEach-Object { "- $_" } | Out-String)

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
"@

Set-Content -LiteralPath (Join-Path $ProjectRoot 'vendor/INVENTORY.md') -Value $inventory

if ($claudeCount -ne 83 -or $agentsCount -ne 83) {
  Write-Fail "Expected 83 skills in each root; got claude=$claudeCount agents=$agentsCount"
}

Write-Info "Done. Inventories written. claude=$claudeCount agents=$agentsCount"
