# Phoenix Finance — GitHub Copilot Agent Instructions

## MANDATORY: Read instructions before any work

**Every Copilot task, suggestion, or implementation MUST begin by reading ALL files in the `instructions/` folder.** This is the first action, before writing any code, editing any file, or suggesting any change.

```
instructions/
  coding-standards.md   — Java standards, package structure, naming, forbidden patterns
  deployment.md         — Docker, WildFly, build pipeline, RULE-DEP-001 through RULE-DEP-012
  development-lifecycle.md — Branch strategy, commit standards, PR requirements
  error-handling.md     — Exception hierarchy, logging, user-visible error requirements
  logging.md            — Log levels, MDC, what MUST and MUST NOT be logged
  testing.md            — Test scope, coverage thresholds, RULE-TEST-001 through RULE-TEST-006
```

**Failure to read instructions before working is a defect and the output must be discarded.**

---

## Enforcement

Any RULE-* defined in any instruction file is a **blocking constraint**. Violations are defects that must be fixed before the task is considered complete. Examples:

| Rule | Location | Constraint |
|------|----------|------------|
| RULE-DEP-001 | deployment.md | Docker images must use `eclipse-temurin:25-jdk-noble` + WildFly 39 |
| RULE-DEP-002 | deployment.md | PostgreSQL driver version must be `42.7.7` in pom.xml, Dockerfile, and module.xml |
| RULE-DEP-012 | deployment.md | Smoke tests MUST pass after every Docker deployment |
| RULE-TEST-001..006 | testing.md | Tests must not be skipped; 100% pass rate required |

---

## Technology Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 25 | `maven.compiler.release=25` in pom.xml |
| Jakarta EE | 10 | `jakarta.*` namespace — `javax.*` is **PROHIBITED** |
| WildFly | 39.0.1.Final | Jakarta EE 10 runtime |
| Jakarta Faces | 4.0 | Views are `.xhtml` Facelets — JSF `.jsp` files are **NOT supported** |
| JSTL taglib | `jakarta.tags.core` | Old URI `http://java.sun.com/jsp/jstl/core` is **PROHIBITED** |
| PostgreSQL driver | 42.7.7 | Must be consistent in all 3 locations |
| Maven | 3.9.9 | Via Maven wrapper (mvnw/mvnw.cmd) |

---

## Key Architecture Constraints

### `javax.*` is PROHIBITED

WildFly 39 does not provide any `javax.*` Jakarta EE modules. Every Java import, XML namespace, and taglib URI must use the `jakarta.*` equivalent.

```java
// PROHIBITED
import javax.servlet.http.HttpServlet;
import javax.persistence.Entity;

// REQUIRED
import jakarta.servlet.http.HttpServlet;
import jakarta.persistence.Entity;
```

### Jakarta Faces Views Must Be Facelets

Jakarta Faces 4.0 removed JSP TLD support. Views using `<h:`, `<f:` components must be `.xhtml` Facelets, not `.jsp` files.

- **JSF pages**: place at `src/main/webapp/*.xhtml`, mapped via `FacesServlet` (`*.xhtml`) in `web.xml`
- **Non-JSF JSP pages**: continue to use `.jsp` via the custom `Dispatcher` servlet (`/finance/*`)

### Dispatcher Servlet Routing

The custom `Dispatcher` servlet at `/finance/*` reads `mapping.properties` and forwards to JSP views in `/WEB-INF/view/*.jsp`. Any new controller action must be registered in `mapping.properties`.

---

## Post-Deployment Verification (RULE-DEP-012)

After `docker compose up --build -d`, ALL of these must return HTTP 200:

```powershell
Invoke-WebRequest "http://localhost:8080/phoenix_investment_finance/finance/enterInvestmentDetails.jsp" -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "http://localhost:8080/phoenix_investment_finance/finance/enterInvestorNumber.jsp" -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "http://localhost:8080/phoenix_investment_finance/mortgageLoanManagement.xhtml" -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "http://localhost:8080/phoenix_investment_finance/mortgagePayoffAccelerator.xhtml" -UseBasicParsing | Select-Object StatusCode
```

A deployment is NOT complete until all smoke tests pass. Do NOT report success otherwise.

---

## Test Requirements

- Run: `.\mvnw.cmd clean test` from `phoenix_investment_finance/`
- Expected: `BUILD SUCCESS`, `Tests run: 8, Failures: 0, Errors: 0`
- Tests may NOT be skipped (`-DskipTests` is PROHIBITED unless building Docker image layer cache)
