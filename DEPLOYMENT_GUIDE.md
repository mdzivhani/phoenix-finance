# Deployment Guide - Phoenix Finance with Mortgage Loans

## Quick Deployment Options

### Option 1: Docker Compose (Recommended - Easiest)

**Prerequisites**:
- Docker Desktop installed and running
- Git/terminal access

**Steps**:
```bash
# Navigate to project root
cd c:\workbench\personal_projects\phoenix-finance

# Build and start containers
docker-compose up --build -d

# Wait for startup (approximately 30-60 seconds)
# Check status
docker-compose ps

# Access application
# Open browser: http://localhost:8080/phoenix_investment_finance/
```

**Stopping**:
```bash
docker-compose down
```

**Logs**:
```bash
docker-compose logs -f app
docker-compose logs -f postgres
```

---

### Option 2: Local WildFly Installation

**Prerequisites**:
- JDK 11+ installed
- PostgreSQL 12+ running
- WildFly 26+ downloaded

**Steps**:

#### 2.1 Setup PostgreSQL
```powershell
# Run setup script
pwsh -File scripts/setup-postgres.ps1 `
  -DbName phoenixdb `
  -DbUser postgres `
  -DbPassword postgres `
  -Host localhost `
  -Port 5432

# Verify connection
psql -U postgres -d phoenixdb -c "SELECT 1"
```

#### 2.2 Configure WildFly
```bash
# Set environment variable
set JBOSS_HOME=C:\path\to\wildfly-26.0.0.Final

# Configure datasource
cd %JBOSS_HOME%\bin
jboss-cli.bat --file=..\..\scripts\register-driver.cli
jboss-cli.bat --file=..\..\scripts\configure-wildfly.sh
```

#### 2.3 Build Application
```bash
cd c:\workbench\personal_projects\phoenix-finance\phoenix_investment_finance

# Build with Maven
mvnw.cmd clean package -DskipTests -DskipWildFlyDeploy=true
```

#### 2.4 Start WildFly
```bash
# In one terminal
cd %JBOSS_HOME%\bin
standalone.bat

# Wait for "WildFly X.X.X.Final (WildFly Core X.X.X) started"
```

#### 2.5 Deploy Application
```bash
# In another terminal
cd c:\workbench\personal_projects\phoenix-finance\phoenix_investment_finance
copy target\phoenix_investment_finance.war %JBOSS_HOME%\standalone\deployments\
```

#### 2.6 Verify Deployment
```bash
# Check WildFly logs
cd %JBOSS_HOME%\standalone\log
type server.log | findstr "DEPLOYMENTS"

# Access application
# Open browser: http://localhost:8080/phoenix_investment_finance/
```

---

## Verification Steps

### 1. Application Accessibility
```bash
# Windows PowerShell
Invoke-WebRequest http://localhost:8080/phoenix_investment_finance/ | Select-Object StatusCode

# Expected: StatusCode 200
```

### 2. Database Connectivity
```bash
# Check PostgreSQL
psql -U postgres -d phoenixdb -c "SELECT version();"

# Check tables created
psql -U postgres -d phoenixdb -c "\dt"
```

### 3. Test Mortgage Loan Feature
1. Navigate to http://localhost:8080/phoenix_investment_finance/
2. Click "Mortgage Loans" in menu
3. Go to "Add New Loan" tab
4. Add a test loan entry
5. View in "View Loans" tab

---

## Production Deployment Considerations

### 1. Database Configuration
```xml
<!-- Update persistence.xml -->
<property name="hibernate.hbm2ddl.auto" value="validate"/>
<!-- Change from 'update' to 'validate' for production -->
```

### 2. Logging
```xml
<!-- Update WildFly logging configuration -->
<!-- Set to WARN or ERROR level instead of DEBUG -->
```

### 3. Security
- Change default database passwords
- Configure HTTPS/SSL certificates
- Enable WildFly security realm authentication
- Set up proper datasource security

### 4. Environment Variables
```bash
# Set before starting WildFly
set POSTGRES_HOST=your-db-server
set POSTGRES_PORT=5432
set POSTGRES_DB=phoenixdb
set POSTGRES_USER=phoenix_app
set POSTGRES_PASSWORD=your-secure-password
```

### 5. Resource Configuration
```bash
# Adjust JVM memory in standalone.conf.bat
set JAVA_OPTS=-Xms1024M -Xmx2048M
```

---

## Backup and Recovery

### Backup Database
```bash
# PostgreSQL backup
pg_dump -U postgres -d phoenixdb > backup_phoenixdb.sql

# With compression
pg_dump -U postgres -d phoenixdb | gzip > backup_phoenixdb.sql.gz
```

### Restore Database
```bash
# Basic restore
psql -U postgres -d phoenixdb < backup_phoenixdb.sql

# From compressed backup
gunzip -c backup_phoenixdb.sql.gz | psql -U postgres -d phoenixdb
```

### Backup Application
```bash
# Backup WAR and configuration
copy target\phoenix_investment_finance.war backup\
copy src\main\webapp\WEB-INF\* backup\WEB-INF\
```

---

## Troubleshooting Deployment Issues

### Issue: "Connection Refused" Error
**Cause**: PostgreSQL not running
**Solution**:
```bash
# Windows - Check if PostgreSQL is running
tasklist | findstr postgres

# If not running, start it
net start postgresql-x64-16
```

### Issue: "Port 8080 Already in Use"
**Solution**:
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual value)
taskkill /PID <PID> /F

# Or change WildFly port in standalone.xml
```

### Issue: "Deployment Failed"
**Solution**:
```bash
# Check logs
type %JBOSS_HOME%\standalone\log\server.log | findstr ERROR

# Clear deployments and retry
del %JBOSS_HOME%\standalone\deployments\*
copy target\phoenix_investment_finance.war %JBOSS_HOME%\standalone\deployments\
```

### Issue: "Driver Not Found"
**Solution**:
```bash
# Verify PostgreSQL driver in WildFly
dir %JBOSS_HOME%\modules\org\postgresql\main\

# Re-run registration script if missing
jboss-cli.bat --file=scripts\register-driver.cli
```

---

## Performance Tuning

### 1. Database Connection Pool
```xml
<!-- Update in phoenix_investment_finance-ds.xml -->
<min-pool-size>5</min-pool-size>
<max-pool-size>20</max-pool-size>
<idle-timeout-minutes>15</idle-timeout-minutes>
```

### 2. JPA/Hibernate Settings
```xml
<!-- persistence.xml -->
<property name="hibernate.jdbc.batch_size" value="20"/>
<property name="hibernate.jdbc.fetch_size" value="50"/>
<property name="hibernate.cache.use_second_level_cache" value="true"/>
```

### 3. WildFly Thread Pools
- Adjust thread pools in standalone.xml based on load
- Configure datasource thread pool size
- Set appropriate timeout values

---

## Health Checks

### Create Health Check Script
```powershell
# health-check.ps1
$appUrl = "http://localhost:8080/phoenix_investment_finance/"
$dbHost = "localhost"
$dbPort = 5432

Write-Host "Checking Application..."
try {
    $response = Invoke-WebRequest $appUrl -TimeoutSec 5
    Write-Host "✓ Application: OK ($($response.StatusCode))"
} catch {
    Write-Host "✗ Application: FAILED"
}

Write-Host "Checking Database..."
try {
    psql -U postgres -d phoenixdb -c "SELECT 1" | Out-Null
    Write-Host "✓ Database: OK"
} catch {
    Write-Host "✗ Database: FAILED"
}
```

**Run health check**:
```bash
powershell -File health-check.ps1
```

---

## Monitoring and Logging

### Enable Detailed Logging
```bash
# Add to standalone.conf.bat
set JAVA_OPTS=%JAVA_OPTS% -Dcom.phoenix.finance.log.level=DEBUG
```

### Check Application Logs
```bash
# Windows PowerShell
Get-Content -Path "%JBOSS_HOME%\standalone\log\server.log" -Tail 50

# Follow logs in real-time
Get-Content -Path "%JBOSS_HOME%\standalone\log\server.log" -Wait
```

### Monitor Database
```sql
-- Active connections
SELECT datname, usename, application_name, state FROM pg_stat_activity;

-- Table sizes
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) 
FROM pg_tables WHERE schemaname != 'pg_catalog' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## Maintenance Tasks

### Weekly
- Check application logs for errors
- Verify database backups created
- Monitor disk space usage
- Check connection pool status

### Monthly
- Review performance metrics
- Update log rotation settings
- Test backup/restore procedure
- Review security patches

### Quarterly
- Update dependencies
- Test disaster recovery
- Performance optimization review
- Security audit

---

## Undeployment

### Remove Application
```bash
# Stop WildFly
# Then delete WAR file
del %JBOSS_HOME%\standalone\deployments\phoenix_investment_finance.war

# Remove database
psql -U postgres -c "DROP DATABASE phoenixdb;"

# Remove WildFly configuration
del %JBOSS_HOME%\standalone\deployments\phoenix_investment_finance\
```

---

## Support Resources

- **WildFly Docs**: https://docs.wildfly.org/
- **PostgreSQL Docs**: https://www.postgresql.org/docs/
- **Jakarta EE**: https://jakarta.ee/
- **Hibernate**: https://hibernate.org/

---

**Deployment Guide v1.0**  
Ready for production deployment!
