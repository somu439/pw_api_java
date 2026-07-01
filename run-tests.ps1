# Run Cucumber Tests and Generate Report
# Usage: .\run-tests.ps1 [-Env <env>] [-Tags <tags>]
#   -Env  : dev | stage | stage1 | stage2        (default: dev)
#   -Tags : cucumber tag expression               (default: "@system and @regression")
#
# Examples:
#   .\run-tests.ps1
#   .\run-tests.ps1 -Env stage1
#   .\run-tests.ps1 -Env stage2 -Tags "@regression"
#   .\run-tests.ps1 -Env dev -Tags "@system or @regression"
param(
    [string]$Env  = "dev",
    [string]$Tags = "@system and @regression"
)

$MVN = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.8.4-bin\52ccbt68d252mdldqsfsn03jlf\apache-maven-3.8.4\bin\mvn.cmd"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$Report = Join-Path $ProjectDir "target\cucumber-html-reports\overview-features.html"

Set-Location $ProjectDir

Write-Host "================================================"
Write-Host " Running Cucumber Tests"
Write-Host " env  : $Env"
Write-Host " tags : $Tags"
Write-Host "================================================"

& $MVN clean verify "-Denv=$Env" "-Dcucumber.filter.tags=$Tags"

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
