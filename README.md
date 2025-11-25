# README #

## Project Name ##
Phoenix Finance - Investment and Property Bond Management System

## Description ##
Phoenix Finance is a Java EE / Jakarta EE 8 web application for managing investments, property bonds, and investor portfolios. The system includes:

- **Investment Management**: Track and forecast investment performance
- **Property Bond Management**: Handle complex property bond calculations including transfer duty, legal fees, agent fees, and bond registration
- **Investor Portfolio Management**: Register and manage investor information
- **RESTful API**: JAX-RS based REST API for programmatic access
- **Web Interface**: JSF/JSP frontend with Bootstrap for responsive design

## Technology Stack ##

- **Java**: 11 (LTS)
- **Jakarta EE**: 8.0 APIs (JAX-RS, CDI, JPA, Bean Validation)
- **Application Server**: WildFly 25 (tested) – any Jakarta EE 8 compatible server should work
- **Database**: PostgreSQL 16 (container) / JDBC Driver 42.7.5 (module loaded automatically)
- **Build Tool**: Maven 3.9.x (Maven Wrapper included: `mvnw`, `mvnw.cmd`)
- **Frontend**: JSF 2.3, JSP, Bootstrap
- **Persistence**: JPA 2.2 / Hibernate
- **REST API**: JAX-RS 2.1
- **Tracing**: MicroProfile OpenTracing (Jaeger tracer initialized at startup)

## Prerequisites ##

- **Java Development Kit (JDK)**: JDK 11 or later
- **Application Server**: WildFly 26+ or any Jakarta EE 8 compatible server
- **Database**: PostgreSQL 12+ installed and running
- **Maven**: 3.6+ (or use included Maven Wrapper)

## Database Setup ##

### Option A: Manual ###
1. Install PostgreSQL (12+; tested with 16).
2. Create database & user (example):
   ```sql
   CREATE DATABASE phoenixdb;
   CREATE USER phoenix_user WITH PASSWORD 'phoenix_pass';
   GRANT ALL PRIVILEGES ON DATABASE phoenixdb TO phoenix_user;
   ```
3. `persistence.xml` uses the JTA datasource `java:jboss/datasources/PhoenixDS` – no direct JDBC URL config needed.
4. Datasource descriptor `WEB-INF/phoenix_investment_finance-ds.xml` resolves connection details from environment variables:
   - `POSTGRES_HOST` (default: postgres in Docker, localhost otherwise)
   - `POSTGRES_PORT` (default: 5432)
   - `POSTGRES_DB` (default: phoenixdb)
   - `POSTGRES_USER` (default: postgres)
   - `POSTGRES_PASSWORD` (default: postgres)
5. Set these before starting WildFly OR rely on docker-compose which injects them automatically.

### Option B: Script Assisted (PowerShell) ###
Use helper script to create database, user, and grants:
```powershell
pwsh -File scripts/setup-postgres.ps1 -DbName phoenixdb -DbUser phoenix_app -DbPassword PhoenixApp!123 -Host localhost -Port 5432
```
Requires `psql` in PATH. The script is idempotent (will skip existing objects).

## Build ##

### Using Maven Wrapper (Recommended) ###
```bash
# On Windows (PowerShell)
.\mvnw.cmd clean package

# On Linux/Mac
./mvnw clean package
```

### Using System Maven ###
```bash
mvn clean package
```

The WAR file will be generated in the `target/` directory as `phoenix_investment_finance.war`.

## Running the Application ##

### Option 1: Deploy to WildFly Manually (Local) ###

1. Start your WildFly server:
   ```bash
   %JBOSS_HOME%\bin\standalone.bat   # Windows
   $JBOSS_HOME/bin/standalone.sh     # Linux/Mac
   ```

2. Copy the WAR file to the deployments directory:
   ```bash
   copy target\phoenix_investment_finance.war %JBOSS_HOME%\standalone\deployments\
   ```

3. Access the application at: `http://localhost:8080/phoenix_investment_finance/`

### Option 2: Deploy via Maven Plugin ###
If you have a locally running WildFly with management interface enabled:
```bash
mvn -f phoenix_investment_finance/pom.xml wildfly:deploy
```
Use `-DskipWildFlyDeploy=true` when you only want a build without auto deploy.

### Option 3: Run Everything with Docker Compose ###

Prerequisites: Install Docker Desktop (Windows) or Docker Engine + Compose plugin (Linux/Mac).

Multi-stage build in `Dockerfile` compiles the WAR inside the build stage. Runtime stage:
* Installs PostgreSQL JDBC driver as a WildFly module
* Uses `scripts/configure-wildfly.sh` to start WildFly in admin-only mode, register the driver, then start normally
* Environment variables are passed from `docker-compose.yml` to bind the datasource

```bash
docker compose build
docker compose up -d
docker compose logs -f app
```

Access: `http://localhost:8080/phoenix_investment_finance/`

Health / Logs:
```bash
docker compose ps
docker compose logs -f app
docker compose logs -f postgres
```

Teardown:
```bash
docker compose down
```

If you update dependencies, just re-run `docker compose build`.

### Option 4: Quick Local Run Script (PowerShell) ###

Use helper script to build and deploy without manually copying files.
```powershell
scripts\run-local.ps1 -PostgresHost localhost -PostgresPort 5432 -PostgresDb phoenixdb -PostgresUser postgres -PostgresPassword postgres -WildFlyHome "$env:JBOSS_HOME"
```
Script workflow:
1. Exports datasource env vars
2. Builds with Maven Wrapper (`-DskipWildFlyDeploy=true` prevents plugin auto deploy)
3. Copies WAR into `%JBOSS_HOME%/standalone/deployments`
4. Starts WildFly
5. Waits for startup (& optional browser open)

To skip system Maven usage, replace `mvn` with `./mvnw` or `mvnw.cmd`.

### Option 5: Maven Wrapper ###
Wrapper ensures consistent Maven version even if not installed globally:
```bash
./mvnw -f phoenix_investment_finance/pom.xml clean package -DskipTests
```
Windows:
```powershell
.\mvnw.cmd -f phoenix_investment_finance/pom.xml clean package -DskipTests
```

## Testing ##

Run unit tests:
```bash
mvn test
```

Run integration tests with Arquillian (requires WildFly):
```bash
mvn clean test -Parq-wildfly-managed
```

## API Documentation ##

### REST Endpoints ###

- **Investors**: `/rest/investors/*`
- **Investments**: `/rest/investments/*`
- **Property Bonds**: `/rest/bonds/*`

Example API call:
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"name":"John","surname":"Doe","dob":"1990-01-01"}' \
  http://localhost:8080/phoenix_investment_finance/rest/investors/register
```

## Security & Modernization (November 2025) ##

✅ **Critical Security Fixes Applied:**
- PostgreSQL JDBC Driver upgraded from `42.1.1` to `42.7.5`
  - Fixed CVE-2024-1597 (Critical)
  - Fixed CVE-2022-21724 (High)
  - Fixed CVE-2022-31197 (High)
  - Fixed CVE-2020-13692 (High)
  - Fixed CVE-2022-26520 (Low)

✅ **Modernization Updates:**
- Java upgraded from 8 → 11 (LTS)
- Migrated to explicit Jakarta EE 8 dependencies
- Added Maven Wrapper for reproducible builds
- Introduced PostgreSQL module & automated driver registration in Docker
- Added helper scripts (`run-local.ps1`, `setup-postgres.ps1`, `configure-wildfly.sh`)
- Updated Docker multi-stage build for lean runtime image
- Externalized datasource via env vars for portability

## Troubleshooting ##

| Issue | Symptom | Resolution |
|-------|---------|------------|
| Missing JDBC driver | `jboss.jdbc-driver.postgresql` service missing | Rebuild image: `docker compose build --no-cache`; ensure module jar exists under `wildfly-module/org/postgresql/main/` |
| Datasource not bound | PhoenixDS not listed / 500 errors | Confirm env vars in compose or PowerShell script; check `docker compose logs app` for binding line `WFLYJCA0001: Bound data source` |
| WAR not deploying | No `Deployed phoenix_investment_finance.war` in logs | Verify build success, re-run `mvn clean package`; check permissions if copying manually |
| Port conflict | WildFly fails to bind 8080 | Change published port in `docker-compose.yml` or stop conflicting process |
| Slow startup | Long Hibernate / Infinispan init | Allocate more memory (Docker Desktop resources) or reduce services in WildFly config |

Quick log filters:
```bash
docker compose logs app | grep -E "postgresql|PhoenixDS|WFLYSRV0010|ERROR"
```

To redeploy after code changes:
```bash
./mvnw -f phoenix_investment_finance/pom.xml clean package -DskipTests
docker compose build app
docker compose up -d
```

## Developers ##
Craig, Mulalo, Phomolo, Celokushe

## Business Rules ##

### Complex loan charges ###

#### Transfer duty ####
|Value of the property (R)| Rate|
|--------------|----------------|
|0 - R900,000|0%|
|R900,001 - R1,250,000|3% of the value above R900,000| 
|R1,250,001 � R1 750 000|R10 500 + 6% of the value above R 1 250 000
|R1,250,001 - R1,750,000|R10,500 + 6% of the value above R1,250,000|
|R1,750,001 � R2,250,000|R40,500 + 8% of the value above R1,750,000|
|R2,250,001 � R10,000,000|R80,500 +11% of the value above R2,250,000|
|R10,000,001 and above|R933,000 + 13% of the value above R10,000,000|

#### Legal Fees ####
The biggest value of either; 0.8% of your bond or R15,000.00

#### Agent Fees ####
5%-7% of the bond

#### Bond Registration Fees ####
R5000 (fixed)

## Build (Summary) ##

Standard WAR:
```bash
mvn -f phoenix_investment_finance/pom.xml clean package -DskipTests -DskipWildFlyDeploy=true
```

Docker (build image containing WAR):
```bash
docker build -t phoenix-finance .
```

Compose (full stack):
```bash
docker compose build
docker compose up -d
```

## Source Management ##

Standard Git workflow (branches via feature/*, PRs to master). See command reference below.

### Git ###

#### Commands ####

##### Branch management  #####

Creates a new branch from using the current branch as base
> git checkout -b <new-branch-name>

Changes from the specified branch to the
> git checkout <branch-to-change-to>

Merges the current branch with the provided branch
> git merge <branch-to-be-merged-to-the-current-branch>

##### Viewing Options #####

Show available __local__ branches
> git branch

Similar to show but defaults the difference between HEAD and the staged changes so far
> git diff

Show the recent changes to the branch(as well as affected snippets) 
> git show

Show staged, unstaged & untracked changes so far
> git status

##### Staging Files #####

Add__ a file to be staged
> git add <file>

Add all files from __root__ onwards recursively
> git add .

#### Unstaging Files ####

Stage a __deleted__ file
> git rm --cached	<file>

Stage deleted files
> git rm --cached -r <files>

Discard any changes on the current file and defaults to the last commit of that branch (HEAD)
> git reset HEAD <file>

Discard any changes on the current file and defaults to the last commit of the specified branch
> git reset <commit-hashcode> <file>

##### Commiting changes #####

Commit a single file's changes with a message
> git commit -m "<message>"

Commit all staged files to a single commit with a message
> git commit -am "<message>"

##### Staging Management #####

Temporarily store the current branch's staged changes
> git stash

##### Local Repo Management #####

Pull to overwrite current branch with origin/master(remote)'s latest commits
> git pull --rebase

Merge local changes to whilst preparing to handle confilcts
> git pull

Push the changes of the current branch to the applicable remote branch
> git push

##### Rebase conflicts #####

"Skips the patch"(Untested)
> git rebase --skip

Conflicts have been resolved, added& commited to the merge branch.
> git rebase --continue

Cancels the attempted rebase & checkouts to the original branch
> git rebase --abort

## Hacks ##

### Dev Methodology ###
+ Agile Manifesto
	* Read it
	* Find out who defined it
	* Why it was created 

+ Agile Breakdown
	* Ceremonies
		Stand Up - 15min
			Ask 3 questions
				> What did I do?
				> What am I do today?
				> What is blocking me from progressing?

	* Sprint
		5 days
			Has a sprint backlog
			Contains Tickets
			Can only hold one ticket at a time
			Concludes in a retro

	* Plan
		Has a product backlog
		Never work on anything that your product owner doens't approve or isn't in your product backlog.

	* Environments
		Development on a local machine->Quality Assurance->Production

		* Read Up
			On estimation points
			How we predict time for how stories are completed
				
+ Team Coding standards
	Pair Programming

### Curl ###

#### Resources ####

[Command Overview for POST](https://gist.github.com/subfuzion/08c5d85437d5d4f00e58)

[POST examples](https://gist.github.com/joyrexus/524c7e811e4abf9afe56)

#### Flags ####
|Command Flag|Description 	|
|------------|--------------|
| `-d`       | Data         |
| `-H`       | Header       |
| `-X`       | Request      |
| `-F`         | Form         |
|`-u <user:password>` |Login Details |

#### Command Examples ####

> curl -H "Content-Type: application/json" -X POST -d {\"name\":\"Command\",\"surname\":\"Line Utility\",\"dob\":\"1990-04-28\"} http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add

> curl -X POST -F 'name=Command' -F 'surname=Utility' -F 'dob=1990-04-28' http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add


> curl -H 'Content-Type: application/json' -X POST -d '{"name":"Command","surname":"Line Utility","dob":"1990-04-28"}' http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add

##### Works for POST #####
Note Content-Type used by JBOSS i.e. application/x-www-form-urlencoded
>	curl -d "name=Hai&surname=Man&dob=2017-06-29" -X POST -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add
