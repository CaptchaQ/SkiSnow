$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

gh repo edit CaptchaQ/SkiSnow --description "Alpine ski and freeride tracker for Android"
gh repo edit CaptchaQ/SkiSnow --add-topic kotlin
gh repo edit CaptchaQ/SkiSnow --add-topic android
gh repo edit CaptchaQ/SkiSnow --add-topic jetpack-compose
gh repo edit CaptchaQ/SkiSnow --add-topic maplibre
gh repo edit CaptchaQ/SkiSnow --add-topic skiing

$i2 = gh issue create -t "feat: session detail and elevation chart" -F docs/roadmap/issue-pr2.md -l enhancement
$i3 = gh issue create -t "feat: settings and units preference" -F docs/roadmap/issue-pr3.md -l enhancement
$i4 = gh issue create -t "feat: offline basemap packs" -F docs/roadmap/issue-pr4.md -l enhancement

Write-Host "ISSUES: $i2 $i3 $i4"
Write-Host "REPO: https://github.com/CaptchaQ/SkiSnow"