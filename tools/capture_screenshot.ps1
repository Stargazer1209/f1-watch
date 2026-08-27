# PowerShell Automated Screenshot & Test Workflow for F1 Watch
param(
    [string]$OutputFile = "screenshot_home.png"
)

$ErrorActionPreference = "Stop"
$toolsDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $toolsDir

$adb = "$projectDir\.tools\android-sdk\platform-tools\adb.exe"
$apkPath = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " F1 Watch Automated Screen Capture" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Check Connected Device / Emulator
$devices = & $adb devices | Select-String "device$"
if ($devices.Count -eq 0) {
    Write-Host "[!] No active Android device or emulator detected by ADB." -ForegroundColor Yellow
    Write-Host "    Please connect your phone via USB (with USB debugging enabled)" -ForegroundColor Yellow
    Write-Host "    OR connect via Wireless ADB (adb connect <IP>:<PORT>)" -ForegroundColor Yellow
    Write-Host "    OR start your Android Studio / AVD emulator." -ForegroundColor Yellow
    exit 1
}

Write-Host "[+] Found active device: $($devices[0].Line)" -ForegroundColor Green

# 2. Install latest Debug APK
if (Test-Path $apkPath) {
    Write-Host "[+] Installing latest APK: $apkPath..." -ForegroundColor Green
    & $adb install -r $apkPath
} else {
    Write-Host "[!] APK not found at $apkPath. Please build debug APK first." -ForegroundColor Red
    exit 1
}

# 3. Launch MainActivity
Write-Host "[+] Launching com.f1watch.MainActivity..." -ForegroundColor Green
& $adb shell am start -n com.f1watch/.MainActivity -W

# 4. Wait for Compose UI & Coil Images to render
Write-Host "[+] Waiting 3 seconds for Compose UI rendering..." -ForegroundColor Green
Start-Sleep -Seconds 3

# 5. Capture Screencap
$targetPath = Join-Path $projectDir $OutputFile
Write-Host "[+] Capturing screen to $targetPath..." -ForegroundColor Green
& $adb exec-out screencap -p > $targetPath

if (Test-Path $targetPath) {
    $size = (Get-Item $targetPath).Length
    Write-Host "[✓] Screenshot successfully captured! ($size bytes)" -ForegroundColor Green
    Write-Host "    Location: $targetPath" -ForegroundColor Cyan
} else {
    Write-Host "[!] Failed to capture screenshot." -ForegroundColor Red
    exit 1
}
