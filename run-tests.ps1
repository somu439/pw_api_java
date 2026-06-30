# Run Cucumber Tests and Generate Report
param(
    [string]$Env = "dev"
)

$MVN = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.8.4-bin\52ccbt68d252mdldqsfsn03jlf\apache-maven-3.8.4\bin\mvn.cmd"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$Report = Join-Path $ProjectDir "target\cucumber-html-reports\overview-features.html"

Set-Location $ProjectDir

Write-Host "================================================"
Write-Host " Running Cucumber Tests  [env=$Env]"
Write-Host "================================================"

& $MVN clean verify "-Denv=$Env"

$ExitCode = $LASTEXITCODE

Write-Host ""
Write-Host "================================================"

if (Test-Path $Report) {
    Write-Host " Report generated: $Report"
    Write-Host " Opening report in browser..."
    Start-Process $Report
} else {
    Write-Host " Report not found at: $Report"
}

Write-Host "================================================"
exit $ExitCode
