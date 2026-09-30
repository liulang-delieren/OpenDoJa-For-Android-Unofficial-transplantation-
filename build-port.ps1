param(
    [string]$Task = 'assembleDebug'
)
$wd = $PSScriptRoot
$gradle = 'D:\gradle-home\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat'
$env:JAVA_HOME = 'D:\Android Studio\jbr'
$env:GRADLE_USER_HOME = 'D:\gradle-home'
$tmp = 'D:\temp-g'
if (-not (Test-Path $tmp)) { New-Item -ItemType Directory -Path $tmp | Out-Null }
$env:TEMP = $tmp
$env:TMP = $tmp

$outLog = [IO.Path]::Combine($wd, 'build.log')
$errLog = [IO.Path]::Combine($wd, 'build.err')
if (Test-Path $outLog) { Remove-Item $outLog -Force }
if (Test-Path $errLog) { Remove-Item $errLog -Force }

$p = Start-Process -FilePath $gradle -ArgumentList @($Task, '--no-daemon', '--console=plain') `
    -WorkingDirectory $wd -PassThru -RedirectStandardOutput $outLog -RedirectStandardError $errLog
$deadline = (Get-Date).AddSeconds(900)
while (-not $p.HasExited -and (Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 5
}
if (-not $p.HasExited) {
    try { $p.Kill() } catch {}
    'BUILD TIMEOUT (killed)'
}
$p.Refresh()
"exit=" + $p.ExitCode
if (Test-Path $outLog) {
    $ok = Select-String -Path $outLog -Pattern 'BUILD SUCCESSFUL|BUILD FAILED' | Select-Object -Last 1
    if ($ok) { $ok.Line }
}
