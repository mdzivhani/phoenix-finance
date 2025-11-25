param(
  [string]$PostgresHost = "localhost",
  [int]$PostgresPort = 5432,
  [string]$PostgresDb = "phoenixdb",
  [string]$PostgresUser = "postgres",
  [string]$PostgresPassword = "postgres",
  [string]$WildFlyHome = $env:JBOSS_HOME,
  [switch]$OpenBrowser,
  [int]$StartupWaitSeconds = 25
)

if (-not $WildFlyHome) {
  Write-Error "WildFly home not provided. Set JBOSS_HOME or pass -WildFlyHome."; exit 1
}

Write-Host "[1/6] Setting environment variables for datasource" -ForegroundColor Cyan
$env:POSTGRES_HOST = $PostgresHost
$env:POSTGRES_PORT = $PostgresPort
$env:POSTGRES_DB = $PostgresDb
$env:POSTGRES_USER = $PostgresUser
$env:POSTGRES_PASSWORD = $PostgresPassword

Write-Host "[2/6] Building WAR (skip auto deploy)" -ForegroundColor Cyan
& mvn -f "phoenix_investment_finance/pom.xml" clean package -DskipTests -DskipWildFlyDeploy=true
if ($LASTEXITCODE -ne 0) { Write-Error "Maven build failed"; exit 1 }

$warPath = "phoenix_investment_finance/target/phoenix_investment_finance.war"
if (-not (Test-Path $warPath)) { Write-Error "WAR not found at $warPath"; exit 1 }

$deployDir = Join-Path $WildFlyHome "standalone/deployments"
if (-not (Test-Path $deployDir)) { Write-Error "Deployments directory not found: $deployDir"; exit 1 }

Write-Host "[3/6] Copying WAR to deployments" -ForegroundColor Cyan
Copy-Item $warPath $deployDir -Force

Write-Host "[4/6] Starting WildFly" -ForegroundColor Cyan
$standalone = Join-Path $WildFlyHome "bin/standalone.bat"
Start-Process -FilePath $standalone -ArgumentList "-b","0.0.0.0" -WindowStyle Minimized

Write-Host "[5/6] Waiting $StartupWaitSeconds seconds for server startup" -ForegroundColor Cyan
Start-Sleep -Seconds $StartupWaitSeconds

$url = "http://localhost:8080/phoenix_investment_finance/"
Write-Host "[6/6] Application should be accessible at $url" -ForegroundColor Green

if ($OpenBrowser) {
  Write-Host "Opening browser..." -ForegroundColor Cyan
  Start-Process $url
}

Write-Host "Done." -ForegroundColor Green
