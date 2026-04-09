# Deployment Standard — Phoenix Finance

## Purpose

Define the single, enforceable deployment and release standard for the Phoenix Finance application. This standard ensures every deployment passes quality gates, every container build is reproducible, every environment is consistent, and no code reaches production without automated validation.

## Scope

This standard applies to:

- Maven build lifecycle (`pom.xml`)
- Docker container builds (`Dockerfile`)
- Docker Compose orchestration (`docker-compose.yml`)
- WildFly configuration scripts (`scripts/configure-wildfly.sh`, `scripts/register-driver.cli`)
- Local development scripts (`scripts/run-local.ps1`, `scripts/setup-postgres.ps1`)
- WildFly module definitions (`wildfly-module/`)
- Database schema management (`persistence.xml`)
- Environment configuration and secrets
- Future CI/CD pipeline configuration

## Current State and Problems

1. **No CI/CD pipeline**: No GitHub Actions, Jenkins, or any automated build/test/deploy pipeline exists.
2. **Unpinned Docker base image**: `FROM jboss/wildfly:latest` produces non-reproducible builds.
3. **Driver version mismatch**: ~~`module.xml` references `postgresql-42.7.5.jar` but `pom.xml` declares version `42.7.7`.~~ (RESOLVED — both now use `42.7.7`).
4. **`hibernate.hbm2ddl.auto=update`**: Schema changes happen automatically at startup — dangerous for production.
5. **Default database credentials**: `postgres/postgres` used in docker-compose.
6. **No health check endpoint**: The application has no programmatic health check.
7. **No deployment validation**: No smoke tests, readiness checks, or post-deployment verification.
8. **Tests skipped by default**: WAR can be built and deployed without any test execution.

---

## Mandatory Rules

### RULE-DEP-001: Pinned Docker Base Images

All Docker images MUST use a specific, immutable version tag. `latest` is PROHIBITED.

```dockerfile
# REQUIRED — pinned versions matching current production configuration
# Build stage: Eclipse Temurin JDK 25 + Maven 3.9.9 installed manually
FROM eclipse-temurin:25-jdk-noble AS build
# Runtime stage: Eclipse Temurin JDK 25 + WildFly 39.0.1.Final
FROM eclipse-temurin:25-jdk-noble AS runtime

# PROHIBITED
FROM jboss/wildfly:latest       # deprecated; stopped at WildFly 26 (Jakarta EE 8)
FROM quay.io/wildfly/wildfly:26.1.3.Final-jdk11  # Jakarta EE 8 only; incompatible with jakarta.* namespace
FROM maven:latest
```

> **Critical**: `quay.io/wildfly/wildfly` images up to 26.x use **Jakarta EE 8** (`javax.*` namespace).
> This project uses **Jakarta EE 10** (`jakarta.*` namespace) and requires **WildFly 39+**.
> WildFly 39 is installed via tarball in the Dockerfile from GitHub releases.

### RULE-DEP-002: PostgreSQL Driver Version Consistency

The PostgreSQL JDBC driver version MUST be identical in:

1. `pom.xml` dependency: `<version>42.7.7</version>`
2. `Dockerfile` download URL: `postgresql-42.7.7.jar`
3. `wildfly-module/org/postgresql/main/module.xml`: `<resource-root path="postgresql-42.7.7.jar"/>`

A **single Maven property** MUST control this version:

```xml
<properties>
    <postgresql.version>42.7.7</postgresql.version>
</properties>
```

Any mismatch between these three locations is a build-blocking defect.

### RULE-DEP-003: Build Pipeline Gates

The build MUST execute these stages in order. A failure at any stage MUST stop the pipeline:

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Clean   │───>│ Compile  │───>│  Test    │───>│ Verify   │───>│ Package  │───>│  Deploy  │
│          │    │          │    │ (unit)   │    │(coverage)│    │ (WAR)    │    │(optional)│
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
```

The minimum build command:

```bash
# Local build — REQUIRED before every commit
mvn clean verify

# CI build — REQUIRED for merge eligibility
mvn clean verify -Pcoverage
```

Skipping tests (`-DskipTests`, `-Dmaven.test.skip=true`) is PROHIBITED in CI. It is PROHIBITED in local builds except for rapid iteration on non-test code, and the developer MUST run `mvn clean verify` before committing.

### RULE-DEP-004: Container Build Standard

The Dockerfile MUST follow this structure:

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:25-jdk-noble AS build
# Install Maven 3.9.9 (no official Maven+JDK25 image at time of writing)
ARG MAVEN_VERSION=3.9.9
RUN curl -fL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xz -C /opt && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn
WORKDIR /app
COPY pom.xml .
# Download dependencies first for Docker layer caching
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean verify -B

# Stage 2: Runtime — WildFly 39 (Jakarta EE 10, jakarta.* namespace)
FROM eclipse-temurin:25-jdk-noble AS runtime
ARG WILDFLY_VERSION=39.0.1.Final
RUN curl -fL https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz \
    | tar -xz -C /opt && mv /opt/wildfly-${WILDFLY_VERSION} /opt/jboss/wildfly

# Install PostgreSQL module
ARG POSTGRESQL_VERSION=42.7.7
ADD https://jdbc.postgresql.org/download/postgresql-${POSTGRESQL_VERSION}.jar \
    /opt/jboss/wildfly/modules/org/postgresql/main/
COPY wildfly-module/org/postgresql/main/module.xml \
    /opt/jboss/wildfly/modules/org/postgresql/main/

# Configure WildFly
COPY scripts/configure-wildfly.sh /opt/jboss/
RUN chmod +x /opt/jboss/configure-wildfly.sh

# Deploy WAR
COPY --from=build /app/target/phoenix_investment_finance.war \
    /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080 9990

CMD ["/opt/jboss/configure-wildfly.sh"]
```

Requirements:

- Multi-stage build: compile in build stage, copy only WAR to runtime stage.
- Dependency download MUST be a separate layer for cache efficiency.
- Tests MUST run in the build stage (`mvn clean verify`).
- No source code in the runtime image.
- No build tools in the runtime image.
- `EXPOSE` MUST declare all used ports.

### RULE-DEP-005: Environment Configuration

All environment-specific values MUST come from environment variables. Hardcoded values are PROHIBITED.

| Variable | Purpose | Default (dev only) | REQUIRED in Production |
|----------|---------|-------------------|----------------------|
| `POSTGRES_HOST` | Database hostname | `localhost` | YES |
| `POSTGRES_PORT` | Database port | `5432` | YES |
| `POSTGRES_DB` | Database name | `phoenixdb` | YES |
| `POSTGRES_USER` | Database username | — | YES (no default) |
| `POSTGRES_PASSWORD` | Database password | — | YES (no default) |
| `APP_ENV` | Environment name | `development` | YES |
| `WILDFLY_USER` | Admin console user | — | YES for admin access |
| `WILDFLY_PASS` | Admin console password | — | YES for admin access |

**PROHIBITED**:
- Hardcoded database credentials in source code, XML, or properties files
- Default passwords in production configuration
- Credentials in Docker image layers
- Credentials committed to version control

The datasource XML (`phoenix_investment_finance-ds.xml`) MUST continue using environment variable substitution:

```xml
<connection-url>jdbc:postgresql://${env.POSTGRES_HOST:localhost}:${env.POSTGRES_PORT:5432}/${env.POSTGRES_DB:phoenixdb}</connection-url>
<user-name>${env.POSTGRES_USER}</user-name>
<password>${env.POSTGRES_PASSWORD}</password>
```

### RULE-DEP-006: Database Schema Management

`hibernate.hbm2ddl.auto=update` is PROHIBITED in production.

| Environment | `hbm2ddl.auto` Value | Approach |
|-------------|---------------------|----------|
| Development | `update` | Acceptable for rapid iteration |
| Test | `create-drop` | Clean schema per test run |
| Staging | `validate` | Schema managed by migration scripts |
| Production | `validate` | Schema managed by migration scripts |

> **Assumption**: Flyway or Liquibase is not currently in use. When database schema changes are needed for staging/production, SQL migration scripts MUST be written manually, version-controlled under `src/main/resources/db/migration/`, and applied before deployment. Adding Flyway (`org.flywaydb:flyway-core`) to automate this is RECOMMENDED as a follow-up.

The `hbm2ddl.auto` value MUST be controlled by a system property or environment variable:

```xml
<property name="hibernate.hbm2ddl.auto" value="${env.HIBERNATE_DDL_AUTO:validate}"/>
```

### RULE-DEP-007: Health Check Endpoint

The application MUST expose a health check endpoint. Until JAX-RS is fully activated, use a servlet:

```java
@WebServlet("/health")
public class HealthCheckServlet extends HttpServlet {
    @PersistenceContext(unitName = "phoenixPersistence")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            resp.setStatus(200);
            resp.getWriter().write("{\"status\":\"UP\",\"database\":\"UP\"}");
        } catch (Exception e) {
            resp.setStatus(503);
            resp.getWriter().write("{\"status\":\"DOWN\",\"database\":\"DOWN\"}");
        }
    }
}
```

Docker Compose MUST use this endpoint for health checks:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/phoenix_investment_finance/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

### RULE-DEP-008: Docker Compose Configuration

The `docker-compose.yml` MUST follow this standard:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16.2  # MUST be pinned
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-phoenixdb}
      POSTGRES_USER: ${POSTGRES_USER}       # NO default in production
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD} # NO default in production
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_PORT: 5432
      POSTGRES_DB: ${POSTGRES_DB:-phoenixdb}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      APP_ENV: ${APP_ENV:-development}
      HIBERNATE_DDL_AUTO: ${HIBERNATE_DDL_AUTO:-update}
    ports:
      - "8080:8080"
      - "9990:9990"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/phoenix_investment_finance/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 90s

volumes:
  pgdata:
```

A `.env.example` file MUST exist in the repository root with all required variables (without values for secrets):

```env
POSTGRES_DB=phoenixdb
POSTGRES_USER=
POSTGRES_PASSWORD=
APP_ENV=development
HIBERNATE_DDL_AUTO=update
```

The actual `.env` file MUST be in `.gitignore`.

### RULE-DEP-012: Post-Deployment Smoke Test (MANDATORY)

After every Docker deployment (`docker compose up`), the following smoke tests MUST pass before the deployment is declared successful.

A deployment that shows **WildFly started** but has pages that return raw path text, `500 Internal Server Error`, or do not render HTML form elements is considered **FAILED**.

**Required checks**:

```powershell
# All commands must return HTTP 200 and contain HTML form elements
$base = "http://localhost:8080/phoenix_investment_finance"

# 1. Main page
(Invoke-WebRequest "$base/index.jsp" -UseBasicParsing).StatusCode  # Expected: 200

# 2. Core JSP pages (served via Dispatcher servlet)
(Invoke-WebRequest "$base/finance/enterInvestmentDetails.jsp" -UseBasicParsing).Content -match '<form'  # Expected: True
(Invoke-WebRequest "$base/finance/enterInvestorNumber.jsp" -UseBasicParsing).Content -match '<form'     # Expected: True

# 3. Jakarta Faces (Facelets) pages
(Invoke-WebRequest "$base/mortgageLoanManagement.xhtml" -UseBasicParsing).StatusCode    # Expected: 200
(Invoke-WebRequest "$base/mortgagePayoffAccelerator.xhtml" -UseBasicParsing).StatusCode # Expected: 200
```

**Any result other than HTTP 200 is a blocking defect.** Investigate WildFly logs immediately:
- `docker logs phoenix-finance-app 2>&1 | Select-String "ERROR|Exception"`
- Check for `javax.servlet.*` ClassNotFoundException — means Jakarta EE namespace mismatch (see NOTE below)
- Check for `FaceletException` — means XHTML is not well-formed

> **NOTE — Jakarta EE Namespace**: WildFly 39 uses **Jakarta EE 10** (`jakarta.*` namespace only).
> All Java source files MUST use `jakarta.*` imports, NOT `javax.*`.
> XML descriptors MUST use `https://jakarta.ee/xml/ns/jakartaee` namespace.
> JSP taglibs for JSTL MUST use `jakarta.tags.core` (not `http://java.sun.com/jsp/jstl/core`).
> Jakarta Faces views MUST be `.xhtml` Facelets (`.jsp` with JSF tags is not supported in Faces 4.0).

---

### RULE-DEP-009: Deployment Readiness Checklist

Before any deployment, the following MUST be verified:

| Check | Method | Blocking |
|-------|--------|----------|
| All unit tests pass | `mvn clean test` | YES |
| All integration tests pass | `mvn clean verify -Parq-wildfly-managed` | YES |
| Coverage thresholds met | JaCoCo verify phase | YES |
| No `System.out.println` in `src/main/java` | Grep check | YES |
| Docker image builds successfully | `docker build .` | YES |
| Application starts in Docker | `docker-compose up` + health check | YES |
| Health endpoint responds HTTP 200 | `curl http://localhost:8080/phoenix_investment_finance/health` | YES |
| Database connectivity confirmed | Health endpoint reports `database: UP` | YES |
| Environment variables are set (no defaults for secrets) | Startup validation | YES |
| `hbm2ddl.auto` is NOT `update` or `create` in production | Configuration review | YES |
| WAR file version matches the release | `META-INF/MANIFEST.MF` or `pom.properties` check | YES |

### RULE-DEP-010: Rollback Strategy

Every deployment MUST have a rollback plan:

- **Docker**: Keep the previous image tag. Rollback = `docker-compose down && docker tag previous-image:tag && docker-compose up`.
- **WildFly standalone**: Keep the previous WAR in a backup directory. Rollback = copy previous WAR to `deployments/`.
- **Database**: If migration scripts were applied, a corresponding rollback script MUST exist. Rolling forward is preferred over rollback for schema changes.

### RULE-DEP-011: Artifact Versioning

- Development builds: `1.0-SNAPSHOT` (current)
- Release builds: Semantic versioning `MAJOR.MINOR.PATCH` (e.g., `1.0.0`, `1.1.0`, `1.1.1`)
- Docker images MUST be tagged with the Maven version: `phoenix-finance:1.0.0`
- `latest` tag on Docker images is PROHIBITED for deployments. Use explicit version tags.
- The WAR file MUST contain version information in `META-INF/MANIFEST.MF` via the `maven-war-plugin`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
        <archive>
            <manifest>
                <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

---

## Prohibited Anti-Patterns

| Anti-Pattern | Why Prohibited |
|-------------|----------------|
| `FROM image:latest` in Dockerfile | Non-reproducible builds, surprise breaking changes |
| Skipping tests in CI (`-DskipTests`) | Untested code reaches production |
| Hardcoded credentials in source/config | Security violation, cannot rotate without rebuild |
| `hbm2ddl.auto=update` in production | Uncontrolled schema changes, data loss risk |
| Default passwords (`postgres/postgres`) in production | Trivially compromised |
| Deploying without health check verification | Dead deployment goes undetected |
| Committing `.env` files with secrets | Secrets in version control |
| Building Docker image without running tests | Untested artifacts deployed |
| Deploying an unversioned WAR | Cannot determine what is running in production |
| Manual deployment without checklist verification | Steps missed, inconsistent outcomes |

---

## Required Implementation Patterns

### Pattern 1: CI Pipeline (GitHub Actions)

This pipeline MUST be created at `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [master]
  pull_request:
    branches: [master]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16.2
        env:
          POSTGRES_DB: phoenixdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven

      - name: Build and Test
        working-directory: phoenix_investment_finance
        run: mvn clean verify -B

      - name: Check for prohibited patterns
        run: |
          if grep -rn "System\.out\.println\|System\.err\.println\|\.printStackTrace()" \
            phoenix_investment_finance/src/main/java/; then
            echo "FAIL: Prohibited logging patterns found"
            exit 1
          fi

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: phoenix_investment_finance/target/site/jacoco/

      - name: Build Docker image
        run: docker build -t phoenix-finance:${{ github.sha }} .
```

### Pattern 2: Release Workflow

```
1. Update version: mvn versions:set -DnewVersion=X.Y.Z
2. Run full build: mvn clean verify
3. Tag release: git tag -a vX.Y.Z -m "Release X.Y.Z"
4. Build Docker image: docker build -t phoenix-finance:X.Y.Z .
5. Push tag: git push origin vX.Y.Z
6. Deploy to staging
7. Run health check against staging
8. Deploy to production (if staging passes)
9. Run health check against production
10. Monitor logs for 15 minutes post-deploy
```

### Pattern 3: Local Development Startup

The `scripts/run-local.ps1` script MUST:

1. Verify PostgreSQL is running
2. Run `mvn clean verify` (not just `package`)
3. Copy WAR to WildFly deployments
4. Start WildFly
5. Wait for health check to respond 200
6. Open browser

---

## Validation and Enforcement Approach

### Pre-Commit

- Developer MUST run `mvn clean verify` locally before committing.
- Developer MUST verify the Docker build completes if Dockerfile or configuration was changed.

### CI Gate

- `mvn clean verify` MUST pass (compile + unit tests + coverage check).
- Grep for prohibited patterns MUST pass.
- Docker image build MUST succeed.

### Pre-Merge

- All CI checks MUST pass.
- Code review MUST be approved.
- No merge to `master` with failing tests.

### Post-Deploy

- Health check MUST respond HTTP 200 within 90 seconds of deployment.
- Application logs MUST show successful startup (no ERROR-level entries).

---

## Pull Request / Merge Acceptance Criteria

A pull request MUST NOT be merged if:

1. CI pipeline fails on any stage (compile, test, coverage, Docker build)
2. Tests are skipped via `-DskipTests` or `maven.test.skip`
3. The Dockerfile uses an unpinned (`:latest`) base image
4. PostgreSQL driver version is inconsistent across `pom.xml`, Dockerfile, and `module.xml`
5. Hardcoded credentials exist in any source file, configuration file, or script
6. `hbm2ddl.auto` is set to `update` or `create` in production configuration paths
7. The health check endpoint is broken or removed
8. Environment-specific values are hardcoded instead of using environment variables
9. Docker Compose `depends_on` does not use `condition: service_healthy`
10. The WAR artifact is unversioned (missing implementation entries in MANIFEST.MF)
