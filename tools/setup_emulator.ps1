# PowerShell AVD Emulator Setup & Launch Helper
param(
    [string]$AvdName = "test_f1_avd"
)

$toolsDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $toolsDir

$env:JAVA_HOME = "$projectDir\.tools\jdk"
$env:ANDROID_HOME = "$projectDir\.tools\android-sdk"

$emulator = "$projectDir\.tools\android-sdk\emulator\emulator.exe"
$avdManager = "$projectDir\.tools\android-sdk\cmdline-tools\latest\bin\avdmanager.bat"
$adb = "$projectDir\.tools\android-sdk\platform-tools\adb.exe"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " F1 Watch AVD Emulator Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Verify AVD exists
$avds = & $emulator -list-avds
if ($avds -notcontains $AvdName) {
    Write-Host "[+] Creating AVD '$AvdName'..." -ForegroundColor Green
    "no`n" | & $avdManager create avd -n $AvdName -k "system-images;android-34;google_apis;x86_64" --device "pixel_6" --force
} else {
    Write-Host "[+] AVD '$AvdName' already exists." -ForegroundColor Green
}

# 2. Launch Emulator
Write-Host "[+] Starting emulator '$AvdName'..." -ForegroundColor Green
Start-Process -FilePath $emulator -ArgumentList "-avd", $AvdName, "-no-audio", "-no-boot-anim", "-gpu", "swiftshader_indirect"

# 3. Wait for device
Write-Host "[+] Waiting for device boot..." -ForegroundColor Green
& $adb wait-for-device
while ((& $adb shell getprop sys.boot_completed).Trim() -ne "1") {
    Start-Sleep -Seconds 2
}
Write-Host "[✓] Emulator booted successfully!" -ForegroundColor Green
