<#!
.SYNOPSIS
    Sets up a PostgreSQL database and user for the Phoenix Finance application.
.DESCRIPTION
    Creates database, application role, grants privileges. Requires psql in PATH and a superuser login.
.PARAMETER DbName
    Name of the database to create (default: phoenixdb)
.PARAMETER DbUser
    Application user to create (default: phoenix_app)
.PARAMETER DbPassword
    Password for the application user (default: PhoenixApp!123)
.PARAMETER Host
    PostgreSQL host (default: localhost)
.PARAMETER Port
    PostgreSQL port (default: 5432)
.PARAMETER SuperUser
    PostgreSQL superuser (default: postgres)
.PARAMETER SuperPass
    PostgreSQL superuser password (optional; if omitted you will be prompted if required)
.EXAMPLE
    .\setup-postgres.ps1 -DbName phoenixdb -DbUser phoenix_app -DbPassword S3cret!
#>
param(
    [string]$DbName = "phoenixdb",
    [string]$DbUser = "phoenix_app",
    [string]$DbPassword = "PhoenixApp!123",
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$SuperUser = "postgres",
    [string]$SuperPass = $env:PG_SUPER_PASS
)

function Invoke-Sql {
    param([string]$Sql)
    $pgPassArg = if ($SuperPass) { $env:PGPASSWORD = $SuperPass; "" } else { "" }
    $cmd = "psql -h $Host -p $Port -U $SuperUser -d postgres -v ON_ERROR_STOP=1 -c \"$Sql\""
    Write-Host "Running: $Sql" -ForegroundColor Cyan
    $res = & powershell -NoLogo -NoProfile -Command $cmd 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Failed executing SQL: $Sql`n$res" }
}

# Pre-flight checks
if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
    Write-Error "psql not found. Install PostgreSQL client tools first."; exit 1
}

Write-Host "Setting up PostgreSQL database '$DbName' and user '$DbUser'..." -ForegroundColor Green

try {
    # Create database if not exists
    Invoke-Sql "DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname='$DbName') THEN PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE $DbName'); END IF; END $$;" 2>$null
    # Simpler create (will error if exists, so wrapped)
    Invoke-Sql "CREATE DATABASE $DbName" 2>$null
} catch { Write-Host "Database may already exist: $DbName" -ForegroundColor Yellow }

try {
    Invoke-Sql "CREATE ROLE $DbUser LOGIN PASSWORD '$DbPassword'" 2>$null
} catch { Write-Host "Role may already exist: $DbUser" -ForegroundColor Yellow }

Invoke-Sql "GRANT ALL PRIVILEGES ON DATABASE $DbName TO $DbUser" 2>$null

# Extensions (optional)
try { Invoke-Sql "CREATE EXTENSION IF NOT EXISTS uuid-ossp" } catch { Write-Host "Could not create extension uuid-ossp (needs shared_preload_libraries on some setups)" -ForegroundColor Yellow }

Write-Host "PostgreSQL setup complete." -ForegroundColor Green
Write-Host "Connection URL: jdbc:postgresql://$Host:$Port/$DbName" -ForegroundColor Green
Write-Host "Update persistence.xml if host/port differ from defaults." -ForegroundColor Yellow
