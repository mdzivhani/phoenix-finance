# Logging Standard — Phoenix Finance

## Purpose

Define the single, enforceable logging and observability standard for the Phoenix Finance application. This standard eliminates ad-hoc logging, ensures structured traceability across all layers, and shifts defect detection from runtime log review to automated alerting and structured queries.

## Scope

This standard applies to:

- All Java source code under `com.phoenix.finance.*`
- All backend layers: web controllers, servlets, services, resources (DAOs), utilities
- JSP error pages and frontend error reporting
- WildFly server configuration
- Docker container log output
- Any future microservice or module added to this repository

## Current State and Baseline Decision

The repository currently has **no explicit logging framework**. It relies entirely on WildFly's built-in JBoss Logging with no application-level log statements. This is a critical gap. The standard below establishes the mandatory baseline.

### Chosen Logging Stack

| Concern | Standard | Rationale |
|---------|----------|-----------|
| Logging API | **SLF4J 1.7.x** | Jakarta EE 8 compatible, decoupled from implementation |
| Logging Implementation | **JBoss Logging** (WildFly-provided) | Already bundled in WildFly 26; zero additional dependencies |
| Log Format | **Structured JSON** via WildFly JSON formatter | Machine-parseable, query-friendly |
| MDC (Mapped Diagnostic Context) | **SLF4J MDC** | Thread-local context propagation for correlation |

> **Assumption**: SLF4J 1.7.x is used because WildFly 26 ships with it. The dependency MUST be declared as `<scope>provided</scope>` in `pom.xml`. No other logging framework (Log4j, Logback, java.util.logging wrappers) is permitted.

---

## Mandatory Rules

### RULE-LOG-001: Use SLF4J for All Application Logging

Every class that logs MUST use SLF4J via a private static final logger:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvestmentServiceImpl implements InvestmentService {
    private static final Logger LOG = LoggerFactory.getLogger(InvestmentServiceImpl.class);
}
```

- The logger field MUST be named `LOG`.
- The logger MUST be `private static final`.
- The class passed to `getLogger()` MUST be the enclosing class.

### RULE-LOG-002: Structured Log Fields via MDC

Every request entering the application MUST populate the following MDC fields before any business logic executes:

| MDC Key | Source | Required |
|---------|--------|----------|
| `correlationId` | HTTP header `X-Correlation-ID` or generated UUID | REQUIRED |
| `requestId` | Generated UUID per request | REQUIRED |
| `serviceName` | Constant: `phoenix-finance` | REQUIRED |
| `environment` | System property or env var `APP_ENV` | REQUIRED |
| `operationName` | Controller method or servlet path | REQUIRED |
| `userId` | Authenticated user identifier (when auth exists) | REQUIRED when available |

MDC MUST be set in a servlet filter that executes before all requests and cleared in a `finally` block:

```java
@WebFilter("/*")
public class LoggingContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        try {
            String correlationId = httpReq.getHeader("X-Correlation-ID");
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put("correlationId", correlationId);
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("serviceName", "phoenix-finance");
            MDC.put("environment", System.getProperty("APP_ENV", "development"));
            MDC.put("operationName", httpReq.getRequestURI());
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
```

### RULE-LOG-003: Mandatory Logging Points

The following events MUST be logged at the specified levels:

| Event | Level | Example |
|-------|-------|---------|
| Application startup complete | `INFO` | `LOG.info("Phoenix Finance started successfully")` |
| Application shutdown initiated | `INFO` | `LOG.info("Phoenix Finance shutting down")` |
| Incoming request received (controllers/servlets) | `DEBUG` | `LOG.debug("Processing request: {}", operationName)` |
| Request completed successfully | `DEBUG` | `LOG.debug("Request completed: {} in {}ms", operationName, duration)` |
| Database query execution (resource layer) | `DEBUG` | `LOG.debug("Executing query: findInvestmentsByInvestor investorNum={}", investorNum)` |
| Entity created/updated/deleted | `INFO` | `LOG.info("Investment created: id={}", investment.getId())` |
| Business rule violation | `WARN` | `LOG.warn("Loan not found for payoff: loanId={}", loanId)` |
| Validation failure | `WARN` | `LOG.warn("Validation failed: {}", constraintViolations)` |
| External call initiated | `DEBUG` | `LOG.debug("Connecting to datasource: {}", dsName)` |
| External call failed | `ERROR` | `LOG.error("Database connection failed", exception)` |
| Unhandled exception | `ERROR` | `LOG.error("Unexpected error in {}", operationName, exception)` |
| Security event (auth failure, access denied) | `WARN` | `LOG.warn("Unauthorized access attempt: {}", requestUri)` |
| Configuration loaded | `INFO` | `LOG.info("Loaded {} route mappings", mappingCount)` |
| Retry attempted | `WARN` | `LOG.warn("Retrying operation: {} attempt={}", operationName, attempt)` |

### RULE-LOG-004: Log Levels — Strict Definitions

| Level | Use Case | Production Visibility |
|-------|----------|----------------------|
| `ERROR` | Unrecoverable failures, exceptions requiring intervention | Always visible |
| `WARN` | Recoverable issues, business rule violations, degraded operations | Always visible |
| `INFO` | State transitions, entity lifecycle events, startup/shutdown | Always visible |
| `DEBUG` | Request flow, query details, method entry/exit, calculation steps | Disabled in production by default |
| `TRACE` | PROHIBITED in committed code | Never |

- `System.out.println()` is PROHIBITED.
- `System.err.println()` is PROHIBITED.
- `e.printStackTrace()` is PROHIBITED.

### RULE-LOG-005: Parameterized Messages Only

All log statements MUST use SLF4J parameterized messages. String concatenation in log statements is PROHIBITED.

```java
// CORRECT
LOG.info("Investment created: id={} investorNum={}", investment.getId(), investorNum);

// PROHIBITED
LOG.info("Investment created: id=" + investment.getId());
```

### RULE-LOG-006: Exception Logging

When logging exceptions:

- The exception object MUST be the last argument (SLF4J prints the full stack trace).
- Do NOT call `getMessage()` and pass it as a string parameter — pass the exception object.
- After logging an exception, the code MUST either rethrow, translate to a domain exception, or return an error response. Logging and silently continuing is PROHIBITED.

```java
// CORRECT
LOG.error("Failed to persist investment for investor={}", investorNum, exception);
throw new ServiceException("Investment creation failed", exception);

// PROHIBITED
LOG.error("Error: " + e.getMessage());
// ... continues execution silently
```

### RULE-LOG-007: No Sensitive Data in Logs

The following MUST NEVER appear in log output:

- Passwords, tokens, API keys, secrets
- Full credit card or bank account numbers
- South African ID numbers
- Raw HTTP `Authorization` headers
- Database connection strings with credentials
- Personal data beyond what is necessary for correlation (investor numbers are acceptable; full names and addresses are NOT)

When logging request/response payloads at `DEBUG` level, sensitive fields MUST be masked:

```java
LOG.debug("Investor registration: investorNum={} surname={}", investor.getInvestorNum(), "***");
```

---

## Prohibited Anti-Patterns

| Anti-Pattern | Why Prohibited |
|-------------|----------------|
| `System.out.println()` for logging | Unstructured, no levels, no MDC, no routing |
| `e.printStackTrace()` | Writes to stderr, bypasses logging framework |
| String concatenation in log messages | Performance cost even when level is disabled |
| Logging then swallowing exception silently | Masks failures, causes downstream errors |
| Logging secrets, passwords, tokens | Security violation |
| Using `java.util.logging` directly | Inconsistent with SLF4J standard |
| Adding Log4j or Logback as dependencies | Conflicts with WildFly's bundled logging |
| `TRACE` level in committed code | Excessive noise, never appropriate |
| Logging entire entity objects via `toString()` | May expose sensitive data, excessive output |
| Duplicate log statements for the same event | Log once at the correct layer |

---

## Required Implementation Patterns

### Pattern 1: Servlet Filter for MDC Context

See RULE-LOG-002. This filter MUST be created at `com.phoenix.finance.web.LoggingContextFilter`.

### Pattern 2: Controller/Servlet Logging

```java
@WebServlet("/mortgage/*")
public class MortgageLoanServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(MortgageLoanServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String action = req.getPathInfo();
        LOG.debug("Mortgage request received: action={}", action);
        try {
            // business logic
            LOG.debug("Mortgage request completed: action={} duration={}ms",
                action, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOG.error("Mortgage request failed: action={}", action, e);
            throw e;
        }
    }
}
```

### Pattern 3: Service Layer Logging

```java
@Stateless
public class InvestmentServiceImpl implements InvestmentService {
    private static final Logger LOG = LoggerFactory.getLogger(InvestmentServiceImpl.class);

    @Override
    public Investment createInvestment(Investment investment) {
        LOG.info("Creating investment: investorNum={} fund={}",
            investment.getInvestor().getInvestorNum(), investment.getFund());
        Investment created = investmentResource.save(investment);
        LOG.info("Investment created: id={}", created.getId());
        return created;
    }
}
```

### Pattern 4: Resource (DAO) Layer Logging

```java
@RequestScoped
public class InvestmentResourceImpl extends BaseResource implements InvestmentResource {
    private static final Logger LOG = LoggerFactory.getLogger(InvestmentResourceImpl.class);

    @Override
    public List<Investment> findByInvestor(String investorNum) {
        LOG.debug("Finding investments: investorNum={}", investorNum);
        List<Investment> results = em.createNamedQuery(...)
            .setParameter("investorNum", investorNum)
            .getResultList();
        LOG.debug("Found {} investments for investorNum={}", results.size(), investorNum);
        return results;
    }
}
```

### Pattern 5: Startup/Shutdown Logging

Application startup events MUST be logged using a CDI observer or `@Startup` EJB:

```java
@Singleton
@Startup
public class ApplicationLifecycleLogger {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLifecycleLogger.class);

    @PostConstruct
    public void onStartup() {
        LOG.info("Phoenix Finance application started: env={}", System.getProperty("APP_ENV", "unknown"));
    }

    @PreDestroy
    public void onShutdown() {
        LOG.info("Phoenix Finance application shutting down");
    }
}
```

---

## WildFly JSON Logging Configuration

WildFly MUST be configured to output structured JSON logs. Add to `standalone.xml` or via CLI during container startup in `configure-wildfly.sh`:

```bash
# Add JSON formatter
/subsystem=logging/json-formatter=JSON:add(exception-output-type=formatted, pretty-print=false, \
  meta-data={"serviceName"=>"phoenix-finance"}, \
  key-overrides={timestamp="@timestamp"})

# Configure console handler to use JSON
/subsystem=logging/console-handler=CONSOLE:write-attribute(name=named-formatter, value=JSON)

# Set root logger level
/subsystem=logging/root-logger=ROOT:write-attribute(name=level, value=INFO)

# Set application logger level
/subsystem=logging/logger=com.phoenix.finance:add(level=INFO)
```

Production containers MUST output JSON to stdout. Docker and orchestrators capture stdout natively.

---

## Validation and Enforcement Approach

### Code Review Checklist (Logging)

- [ ] Every class that performs business logic has a `private static final Logger LOG`
- [ ] No `System.out`, `System.err`, or `e.printStackTrace()` anywhere
- [ ] All log messages use parameterized format (`{}`)
- [ ] Exceptions are passed as the last argument, not via `getMessage()`
- [ ] No sensitive data in log statements
- [ ] MDC fields are set before processing begins
- [ ] Error paths log at `ERROR` level with the exception object
- [ ] Log-then-swallow pattern is absent
- [ ] No `TRACE` level in committed code

### CI Gate (Future)

- Grep-based check: fail the build if `System.out.println`, `System.err.println`, or `e.printStackTrace()` appears in `src/main/java/`.
- Static analysis rule: flag string concatenation inside log method calls.

---

## Pull Request / Merge Acceptance Criteria

A pull request MUST NOT be merged if:

1. Any new or modified class lacks a SLF4J logger where one is required per RULE-LOG-003
2. Any `System.out.println()`, `System.err.println()`, or `e.printStackTrace()` is present in `src/main/java/`
3. Any log statement uses string concatenation instead of parameterized messages
4. Any exception is logged and then silently swallowed without rethrow or error response
5. Any sensitive data (passwords, tokens, ID numbers) appears in log output
6. MDC context is not set for new request entry points (servlets, controllers)
7. New error paths lack `ERROR`-level logging with the exception object
