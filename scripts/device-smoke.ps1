$ErrorActionPreference = "Stop"
$adb = "C:\Users\Yaroslav\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$java = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:JAVA_HOME = $java
$env:PATH = "$java\bin;C:\Users\Yaroslav\AppData\Local\Android\Sdk\platform-tools;$env:PATH"
$pkg = "com.skisnow.app"

function Adb {
  param([Parameter(ValueFromRemainingArguments=$true)][string[]]$A)
  & $adb @A
}

function Dump-Ui {
  Adb shell uiautomator dump /sdcard/window_dump.xml | Out-Null
  return (Adb shell cat /sdcard/window_dump.xml | Out-String)
}

function Find-Bounds([string]$xml, [string]$text) {
  $esc = [regex]::Escape($text)
  $m = [regex]::Match($xml, "text=`"$esc`"[^>]*bounds=`"\[(\d+),(\d+)\]\[(\d+),(\d+)\]`"")
  if (-not $m.Success) {
    $m = [regex]::Match($xml, "bounds=`"\[(\d+),(\d+)\]\[(\d+),(\d+)\]`"[^>]*text=`"$esc`"")
  }
  if (-not $m.Success) { return $null }
  $x1 = [int]$m.Groups[1].Value
  $y1 = [int]$m.Groups[2].Value
  $x2 = [int]$m.Groups[3].Value
  $y2 = [int]$m.Groups[4].Value
  return @{ X = [int](($x1 + $x2) / 2); Y = [int](($y1 + $y2) / 2) }
}

function Show-Texts([string]$xml) {
  $texts = [regex]::Matches($xml, 'text="([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
  Write-Host ("VISIBLE: " + ($texts -join " | "))
}

function Tap-Text([string]$text) {
  $xml = Dump-Ui
  $b = Find-Bounds $xml $text
  if ($null -eq $b) {
    Write-Host "NOT_FOUND: $text"
    Show-Texts $xml
    return $false
  }
  Write-Host "TAP $text at $($b.X),$($b.Y)"
  Adb shell input tap "$($b.X)" "$($b.Y)" | Out-Null
  return $true
}

function Assert-Text([string]$text) {
  $xml = Dump-Ui
  if ($xml -match [regex]::Escape("text=`"$text`"")) {
    Write-Host "OK: found $text"
    return $true
  }
  Write-Host "FAIL: missing $text"
  Show-Texts $xml
  return $false
}

function Dismiss-PermDialogs {
  for ($i = 0; $i -lt 4; $i++) {
    $xml = Dump-Ui
    $tapped = $false
    foreach ($label in @("While using the app", "Allow only while using the app", "Allow", "ALLOW", "Only this time")) {
      if ($xml -match [regex]::Escape("text=`"$label`"")) {
        if (Tap-Text $label) { $tapped = $true; Start-Sleep -Seconds 1; break }
      }
    }
    if (-not $tapped) { break }
  }
}

Write-Host "== devices =="
Adb devices -l

Write-Host "== grant permissions =="
Adb shell pm grant $pkg android.permission.ACCESS_FINE_LOCATION
Adb shell pm grant $pkg android.permission.ACCESS_COARSE_LOCATION
try { Adb shell pm grant $pkg android.permission.POST_NOTIFICATIONS } catch {}

Write-Host "== build+install =="
& .\gradlew.bat ":app:installDebug" "--console=plain"
if ($LASTEXITCODE -ne 0) { throw "installDebug failed exit=$LASTEXITCODE" }

Write-Host "== launch =="
Adb shell am force-stop $pkg
Start-Sleep -Seconds 1
Adb shell am start -n "$pkg/.MainActivity"
Start-Sleep -Seconds 5
Dismiss-PermDialogs

Write-Host "== Start =="
if (-not (Tap-Text "Start")) { throw "Start button not found" }
Start-Sleep -Seconds 3
Dismiss-PermDialogs
Start-Sleep -Seconds 2

if (-not (Assert-Text "Recording")) {
  throw "Did not enter Recording"
}

Write-Host "== Pause =="
if (-not (Tap-Text "Pause")) { throw "Pause not found" }
Start-Sleep -Seconds 2
if (-not (Assert-Text "Paused")) { throw "Not Paused" }

Write-Host "== Resume =="
if (-not (Tap-Text "Resume")) { throw "Resume not found" }
Start-Sleep -Seconds 2
if (-not (Assert-Text "Recording")) { throw "Not Recording after resume" }

Write-Host "== Stop =="
if (-not (Tap-Text "Stop")) { throw "Stop not found" }
Start-Sleep -Seconds 3
if (-not (Assert-Text "Ready")) { throw "Not Ready after stop" }
if (-not (Assert-Text "SAVED")) { Write-Host "WARN: SAVED not visible in history yet" }

Write-Host "SMOKE_PASS"
if ((Dump-Ui) -notmatch "SAVED") { Write-Host "WARN: SAVED not visible in history yet" } else { Write-Host "OK: found SAVED" }
