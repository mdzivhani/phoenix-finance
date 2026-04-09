# Error Handling Standard — Phoenix Finance

## Purpose

Define the single, enforceable error handling standard for the Phoenix Finance application. This standard eliminates silent failures, inconsistent error responses, raw exception leakage to clients, and empty catch blocks. Every error MUST be handled deliberately, logged appropriately, and communicated through a consistent structure.

## Scope

This standard applies to:

- All Java source code under `com.phoenix.finance.*`
- Web layer: servlets (`MortgageLoanServlet`, `MortgagePayoffServlet`) and JSF managed beans (`MortgageLoanController`, `MortgagePayoffController`, `InvestmentController`, etc.)
- Service layer: all `*ServiceImpl` classes
- Resource (DAO) layer: all `*ResourceImpl` classes
- Utility classes: `MortgagePayoffCalculator`, `ComplexPropertyBondUtil`, `MoneyFormatter`
- JSP error pages
- REST endpoints (current and future JAX-RS resources)

## Current State and Problems

The repository currently exhibits these error handling problems:

1. **RuntimeException wrapping**: `catch (Exception e) { throw new RuntimeException(e); }` provides no classification or context.
2. **FacesMessage without logging**: Errors shown to users via `FacesMessage` but not logged for diagnostics.
3. **Inconsistent null checks**: Some methods check for null; others do not.
4. **No domain exception hierarchy**: All failures surface as `RuntimeException` or `IllegalArgumentException`.
5. **No standard API error response shape**: Servlets and managed beans return errors differently.
6. **`hibernate.hbm2ddl.auto=update`**: Schema drift errors surface at runtime with no clear error translation.

---

## Mandatory Rules

### RULE-ERR-001: Domain Exception Hierarchy

The application MUST use a typed exception hierarchy under `com.phoenix.finance.exception`:

```java
// Base application exception — all domain exceptions extend this
public abstract class PhoenixException extends RuntimeException {
    private final String errorCode;

    protected PhoenixException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected PhoenixException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
```

```java
// Validation errors — client-fixable input problems
public class ValidationException extends PhoenixException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
```

```java
// Business rule violations — valid input but violates domain rules
public class BusinessException extends PhoenixException {
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
}
```

```java
// Entity not found
public class EntityNotFoundException extends PhoenixException {
    public EntityNotFoundException(String entityType, Object identifier) {
        super("NOT_FOUND", entityType + " not found: " + identifier);
    }
}
```

```java
// Infrastructure/system failures — database, connectivity, configuration
public class InfrastructureException extends PhoenixException {
    public InfrastructureException(String message, Throwable cause) {
        super("INFRASTRUCTURE_ERROR", message, cause);
    }
}
```

### RULE-ERR-002: Error Classification

Every error MUST be classified into exactly one of these categories:

| Category | Exception Type | HTTP Status (if applicable) | User Visibility |
|----------|---------------|----------------------------|-----------------|
| Validation Error | `ValidationException` | 400 Bad Request | Field-level details safe for user |
| Business Error | `BusinessException` | 409 Conflict / 422 Unprocessable | User-safe business message |
| Not Found | `EntityNotFoundException` | 404 Not Found | Entity type and identifier |
| Authentication/Authorization | Future: `SecurityException` | 401 / 403 | Generic "access denied" |
| Infrastructure Error | `InfrastructureException` | 503 Service Unavailable | Generic "service unavailable" |
| Unexpected Error | Unhandled `Exception` | 500 Internal Server Error | Generic "internal error" |

### RULE-ERR-003: No Empty Catch Blocks

Every `catch` block MUST perform at least one of:

1. Log the exception AND rethrow (as-is or translated)
2. Log the exception AND return an explicit error response
3. Log the exception AND add a `FacesMessage` error (JSF only)

```java
// CORRECT — log and translate
try {
    em.persist(investment);
} catch (PersistenceException e) {
    LOG.error("Failed to persist investment: investorNum={}", investorNum, e);
    throw new InfrastructureException("Failed to save investment", e);
}

// PROHIBITED — empty catch
try {
    em.persist(investment);
} catch (PersistenceException e) {
    // do nothing
}

// PROHIBITED — catch and continue silently
try {
    em.persist(investment);
} catch (Exception e) {
    LOG.error("Error", e);
    // continues execution with no investment saved
}
```

### RULE-ERR-004: No Raw Exception Messages to Clients

Exception messages, stack traces, and internal system details MUST NEVER be returned to the client. The web layer MUST map exceptions to user-safe responses.

```java
// CORRECT — user-safe message, internal details logged
catch (InfrastructureException e) {
    LOG.error("Database failure during loan creation", e);
    addFacesMessage(FacesMessage.SEVERITY_ERROR, "Service Unavailable",
        "Unable to process your request. Please try again later.");
}

// PROHIBITED — raw exception to user
catch (Exception e) {
    addFacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
}
```

### RULE-ERR-005: No Generic RuntimeException Wrapping

`throw new RuntimeException(e)` is PROHIBITED. Exceptions MUST be translated to the appropriate domain exception:

```java
// CORRECT
catch (PersistenceException e) {
    throw new InfrastructureException("Failed to query investments", e);
}

// CORRECT — rethrow if already a domain exception
catch (PhoenixException e) {
    throw e;
}

// PROHIBITED
catch (Exception e) {
    throw new RuntimeException(e);
}
```

### RULE-ERR-006: Validate at System Boundaries

Input validation MUST occur at the entry point to the application (servlets, managed beans) BEFORE invoking service methods. Services MUST NOT silently accept invalid data.

```java
// Controller/Servlet — validate first
public void createLoan() {
    Map<String, String> errors = new LinkedHashMap<>();
    if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
        errors.put("loanAmount", "Loan amount must be positive");
    }
    if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
        errors.put("interestRate", "Interest rate must be positive");
    }
    if (termMonths == null || termMonths <= 0) {
        errors.put("termMonths", "Loan term must be positive");
    }
    if (!errors.isEmpty()) {
        throw new ValidationException("Invalid loan data", errors);
    }
    mortgageLoanService.createLoan(loan);
}
```

### RULE-ERR-007: Service Layer Guard Clauses

Service methods MUST validate preconditions and throw `BusinessException` or `EntityNotFoundException` for domain rule violations:

```java
@Override
public MortgageLoan updateLoan(MortgageLoan loan) {
    if (loan == null || loan.getLoanId() == null) {
        throw new ValidationException("Loan and loan ID are required",
            Map.of("loanId", "Loan ID must not be null"));
    }
    MortgageLoan existing = em.find(MortgageLoan.class, loan.getLoanId());
    if (existing == null) {
        throw new EntityNotFoundException("MortgageLoan", loan.getLoanId());
    }
    if (existing.getStatus() == LoanStatus.COMPLETED) {
        throw new BusinessException("LOAN_COMPLETED",
            "Cannot modify a completed loan: " + loan.getLoanId());
    }
    return em.merge(loan);
}
```

### RULE-ERR-008: JSF Error Handling Pattern

All JSF managed beans MUST handle errors through a standardized method:

```java
@Named
@ViewScoped
public class MortgageLoanController implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(MortgageLoanController.class);

    private void handleError(String operation, Exception e) {
        if (e instanceof ValidationException) {
            ValidationException ve = (ValidationException) e;
            ve.getFieldErrors().forEach((field, msg) ->
                FacesContext.getCurrentInstance().addMessage(field,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null)));
            LOG.warn("Validation failed: operation={} errors={}", operation, ve.getFieldErrors());
        } else if (e instanceof EntityNotFoundException) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Not Found",
                    "The requested record was not found."));
            LOG.warn("Entity not found: operation={}", operation, e);
        } else if (e instanceof BusinessException) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Business Rule Violation",
                    e.getMessage()));
            LOG.warn("Business rule violation: operation={}", operation, e);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
                    "An unexpected error occurred. Please try again later."));
            LOG.error("Unexpected error: operation={}", operation, e);
        }
    }
}
```

### RULE-ERR-009: Servlet Error Handling Pattern

All servlets MUST catch exceptions in `doGet`/`doPost` and forward to a consistent error page:

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    try {
        // business logic
    } catch (EntityNotFoundException e) {
        LOG.warn("Entity not found: uri={}", req.getRequestURI(), e);
        req.setAttribute("errorTitle", "Not Found");
        req.setAttribute("errorMessage", "The requested record was not found.");
        req.getRequestDispatcher("/WEB-INF/view/error.jsp").forward(req, resp);
    } catch (ValidationException e) {
        LOG.warn("Validation error: uri={}", req.getRequestURI(), e);
        req.setAttribute("errorTitle", "Invalid Input");
        req.setAttribute("errorMessage", "Please correct your input and try again.");
        req.setAttribute("fieldErrors", e.getFieldErrors());
        req.getRequestDispatcher("/WEB-INF/view/error.jsp").forward(req, resp);
    } catch (PhoenixException e) {
        LOG.error("Application error: uri={}", req.getRequestURI(), e);
        req.setAttribute("errorTitle", "Error");
        req.setAttribute("errorMessage", "An error occurred. Please try again later.");
        req.getRequestDispatcher("/WEB-INF/view/error.jsp").forward(req, resp);
    } catch (Exception e) {
        LOG.error("Unexpected error: uri={}", req.getRequestURI(), e);
        req.setAttribute("errorTitle", "System Error");
        req.setAttribute("errorMessage", "An unexpected error occurred.");
        req.getRequestDispatcher("/WEB-INF/view/error.jsp").forward(req, resp);
    }
}
```

### RULE-ERR-010: Future JAX-RS Error Response Shape

If JAX-RS endpoints are added (the `javax.ws.rs-api` dependency already exists), all error responses MUST use this shape:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid loan data",
    "details": [
      { "field": "loanAmount", "message": "Loan amount must be positive" },
      { "field": "interestRate", "message": "Interest rate must be positive" }
    ],
    "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "timestamp": "2026-04-08T10:30:00Z"
  }
}
```

This MUST be implemented via a JAX-RS `ExceptionMapper`:

```java
@Provider
public class PhoenixExceptionMapper implements ExceptionMapper<PhoenixException> {
    private static final Logger LOG = LoggerFactory.getLogger(PhoenixExceptionMapper.class);

    @Override
    public Response toResponse(PhoenixException exception) {
        int status;
        if (exception instanceof ValidationException) {
            status = 400;
        } else if (exception instanceof EntityNotFoundException) {
            status = 404;
        } else if (exception instanceof BusinessException) {
            status = 409;
        } else if (exception instanceof InfrastructureException) {
            status = 503;
        } else {
            status = 500;
        }

        LOG.error("Mapped exception: code={} status={}", exception.getErrorCode(), status, exception);

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getErrorCode(),
            exception.getMessage(),
            MDC.get("correlationId")
        );
        return Response.status(status).entity(errorResponse).build();
    }
}
```

---

## Prohibited Anti-Patterns

| Anti-Pattern | Why Prohibited | Correct Alternative |
|-------------|----------------|---------------------|
| Empty `catch` block | Silently hides failures | Log + rethrow or return error |
| `catch (Exception e) { throw new RuntimeException(e); }` | No classification, no context | Translate to domain exception |
| `e.getMessage()` returned to client | Leaks internal details | User-safe message |
| `e.printStackTrace()` | Bypasses logging, no MDC | `LOG.error("message", e)` |
| Logging then continuing without error handling | Failure is ignored downstream | Log + rethrow or return error |
| Catching `Throwable` | Catches `Error` types that should crash the JVM | Catch `Exception` only |
| Null return from service methods without documentation | Caller has no signal of failure | Throw `EntityNotFoundException` |
| Multiple catch blocks all doing the same thing | Unmaintainable | Ordered catch with specific handling |
| Returning HTTP 200 with an error body | Client cannot distinguish success from failure | Use appropriate HTTP status code |

---

## Required Implementation Patterns

### Pattern 1: Exception Package Structure

```
com.phoenix.finance.exception/
    PhoenixException.java          (abstract base)
    ValidationException.java       (400 - client input errors)
    BusinessException.java         (409/422 - domain rule violations)
    EntityNotFoundException.java   (404 - entity lookup failures)
    InfrastructureException.java   (503 - system/database failures)
```

### Pattern 2: Resource (DAO) Layer

Resource classes MUST translate JPA/persistence exceptions to `InfrastructureException`:

```java
@Override
public Investment save(Investment investment) {
    try {
        em.persist(investment);
        em.flush();
        return investment;
    } catch (PersistenceException e) {
        throw new InfrastructureException("Failed to persist investment", e);
    }
}

@Override
public Investment findById(Long id) {
    Investment investment = em.find(Investment.class, id);
    if (investment == null) {
        throw new EntityNotFoundException("Investment", id);
    }
    return investment;
}
```

### Pattern 3: Service Layer

Service classes MUST enforce business rules and translate resource exceptions:

```java
@Override
public void deleteLoan(Long loanId) {
    MortgageLoan loan = em.find(MortgageLoan.class, loanId);
    if (loan == null) {
        throw new EntityNotFoundException("MortgageLoan", loanId);
    }
    if (loan.getStatus() == LoanStatus.ACTIVE && loan.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
        throw new BusinessException("LOAN_HAS_BALANCE",
            "Cannot delete active loan with outstanding balance");
    }
    em.remove(loan);
}
```

### Pattern 4: Calculation Utility Error Handling

Utility classes performing calculations MUST validate inputs and throw `ValidationException`:

```java
public class MortgagePayoffCalculator {
    public static PayoffScenario calculateStandardPayoff(
            BigDecimal principal, BigDecimal annualRate, int termMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid principal amount",
                Map.of("principal", "Principal must be positive"));
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid interest rate",
                Map.of("annualRate", "Annual rate must be positive"));
        }
        if (termMonths <= 0) {
            throw new ValidationException("Invalid term",
                Map.of("termMonths", "Term must be positive"));
        }
        // calculation logic
    }
}
```

### Pattern 5: Error JSP Page

The `error.jsp` page MUST display user-safe messages from request attributes and MUST NOT display stack traces or internal exception details:

```jsp
<h2>${errorTitle != null ? errorTitle : 'Error'}</h2>
<p>${errorMessage != null ? errorMessage : 'An unexpected error occurred.'}</p>
<c:if test="${not empty fieldErrors}">
    <ul>
        <c:forEach var="entry" items="${fieldErrors}">
            <li><strong>${entry.key}:</strong> ${entry.value}</li>
        </c:forEach>
    </ul>
</c:if>
```

Stack traces MUST NOT appear in any JSP page.

---

## Validation and Enforcement Approach

### Code Review Checklist (Error Handling)

- [ ] No empty `catch` blocks
- [ ] No `throw new RuntimeException(e)` — domain exceptions used instead
- [ ] No `e.getMessage()` returned to clients
- [ ] No `e.printStackTrace()` calls
- [ ] No `catch (Throwable t)` blocks
- [ ] All new exceptions extend `PhoenixException` hierarchy
- [ ] Input validation occurs at system boundaries (controllers/servlets)
- [ ] Service methods throw `EntityNotFoundException` instead of returning null for required entities
- [ ] Error responses use user-safe messages
- [ ] Exceptions are logged at the appropriate level with the exception object

### CI Gate (Future)

- Static analysis: flag empty catch blocks, `RuntimeException` wrapping, `printStackTrace()` calls.
- Pattern-match: ensure all classes in `com.phoenix.finance.exception` extend `PhoenixException`.

---

## Pull Request / Merge Acceptance Criteria

A pull request MUST NOT be merged if:

1. Any empty `catch` block exists in new or modified code
2. Any `throw new RuntimeException(e)` exists without domain exception translation
3. Any exception message or stack trace is exposed to clients
4. Any `e.printStackTrace()` call exists in `src/main/java/`
5. Any service method returns `null` for entity lookups instead of throwing `EntityNotFoundException`
6. Any new exception class does not extend the `PhoenixException` hierarchy
7. Any validation logic is performed inside the service/resource layer instead of at the system boundary (controllers/servlets) — service-layer guard clauses for preconditions are acceptable, but full input parsing/validation belongs at the boundary
8. The error JSP page has been modified to display raw exception details
