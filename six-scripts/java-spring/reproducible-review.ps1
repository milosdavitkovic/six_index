param(
    [string] $BaseUrl = "http://localhost:8080",
    [string] $IndexCode = "SMI",
    [string] $ReviewPeriod = "Q3-2026",
    [string] $OutputDirectory = "target/reproducible-review"
)

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$outputPath = Join-Path $repositoryRoot $OutputDirectory

New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

function Invoke-Curl {
    param([string[]] $Arguments)

    & curl.exe --fail-with-body @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "curl failed with exit code $LASTEXITCODE"
    }
}

Invoke-Curl @(
    "$BaseUrl/api/health",
    "--output", (Join-Path $outputPath "health.json")
)

Invoke-Curl @(
    "-X", "POST", "$BaseUrl/api/import/all",
    "-F", "spiUniverse=@$(Join-Path $repositoryRoot 'data/spi_universe.csv')",
    "-F", "securityData=@$(Join-Path $repositoryRoot 'data/sec_data.csv')",
    "-F", "composition=@$(Join-Path $repositoryRoot 'data/composition.csv')",
    "--output", (Join-Path $outputPath "import.json")
)

Invoke-Curl @(
    "-X", "POST", "$BaseUrl/api/index-reviews/$IndexCode/$ReviewPeriod/run",
    "--output", (Join-Path $outputPath "review-report.json")
)

Write-Host "Reproducible review completed."
Write-Host "Report: $(Join-Path $outputPath 'review-report.json')"
