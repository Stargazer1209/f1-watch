# F1 Watch Instant UI Verification Workflow (Persistent AVD Mode)
param(
    [string]$OutputFile = "screenshot_home.png",
    [switch]$Build = $false
)

$ErrorActionPreference = "Stop"
$toolsDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $toolsDir

$adb = "$projectDir\.tools\android-sdk\platform-tools\adb.exe"
$emulator = "$projectDir\.tools\android-sdk\emulator\emulator.exe"
$apkPath = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
$targetPath = Join-Path $projectDir $OutputFile

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " F1 Watch Fast UI Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Ensure AVD is running (Hot Resident)
$deviceCheck = & $adb devices | Select-String "emulator-\d+\s+device"
if ($deviceCheck.Count -eq 0) {
    Write-Host "[+] AVD not running. Starting persistent background daemon..." -ForegroundColor Yellow
    
    # Launch detached emulator using WMI / Start-Process independent job
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $emulator
    $psi.Arguments = "-avd test_f1_avd -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect"
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    [System.Diagnostics.Process]::Start($psi) | Out-Null
    
    Write-Host "[+] Waiting for device connection..." -ForegroundColor Yellow
    & $adb wait-for-device
    for ($i = 0; $i -lt 40; $i++) {
        $booted = (& $adb shell getprop sys.boot_completed)
        if ($booted -and $booted.Trim() -eq "1") { break }
        Start-Sleep -Seconds 1
    }
    Write-Host "[✓] AVD online and resident." -ForegroundColor Green
} else {
    Write-Host "[✓] AVD daemon is already alive and warm: $($deviceCheck[0].Line.Trim())" -ForegroundColor Green
}

# 2. Build if requested
if ($Build) {
    Write-Host "[+] Building incremental APK..." -ForegroundColor Cyan
    $env:JAVA_HOME = "$projectDir\.tools\jdk"
    $env:ANDROID_HOME = "$projectDir\.tools\android-sdk"
    & "$projectDir\gradlew.bat" assembleDebug --console=plain
}

# 3. Hot Push APK (1-2s)
Write-Host "[+] Hot pushing APK to emulator..." -ForegroundColor Cyan
& $adb install -r $apkPath

# 4. Restart MainActivity & wait for Compose render
Write-Host "[+] Launching MainActivity..." -ForegroundColor Cyan
& $adb shell am start -n com.f1watch/.MainActivity -S

Write-Host "[+] Waiting 10 seconds for initial network/asset data load and Compose render..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

# 5. Capture Screencap
Write-Host "[+] Capturing screen to $targetPath..." -ForegroundColor Cyan
cmd.exe /c "`"$adb`" exec-out screencap -p > `"$targetPath`""

if (Test-Path $targetPath) {
    $size = (Get-Item $targetPath).Length
    Write-Host "[✓] UI successfully verified! Image saved to: $targetPath ($size bytes)" -ForegroundColor Green
} else {
    Write-Host "[!] Screencap failed." -ForegroundColor Red
}
