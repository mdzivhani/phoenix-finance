# Coding Standards — Phoenix Finance

## Purpose

Define the single, enforceable coding and architecture baseline for the Phoenix Finance application. This standard eliminates inconsistent patterns, enforces naming conventions, controls dependency management, and ensures every engineer and coding agent produces code that is structurally consistent with the existing codebase.

## Scope

This standard applies to:

- All Java source code under `com.phoenix.finance.*`
- All JSP view files under `src/main/webapp/WEB-INF/view/`
- All configuration files (XML, properties)
- All build configuration (`pom.xml`)
- All scripts (`scripts/`)
- All Docker and infrastructure files

## Current State and Problems

1. **Mixed CDI scoping**: `@Stateless @Dependent` is redundant (`@Stateless` implies its own lifecycle). Some services use `@ApplicationScoped @Transactional`, others use `@Stateless`.
2. **Inconsistent qualifier usage**: Custom `@Controller` and `@Resource` qualifiers exist but are not consistently applied.
3. **No code formatting standard**: No `.editorconfig`, Checkstyle, or formatter configuration.
4. **Package `entity` is overloaded**: Sub-packages (`investment`, `bond`, `client_enum`, `loan_enum`) use snake_case naming.
5. **Deprecated Docker image**: `jboss/wildfly` is deprecated in favor of `quay.io/wildfly/wildfly`.
6. **No `.gitignore`**: Build artifacts may be committed to version control.

---

## Mandatory Rules

### RULE-CODE-001: Java Version and Language Level

- Java version: **11 LTS**. All code MUST compile against Java 11.
- Language features above Java 11 are PROHIBITED.
- The `pom.xml` MUST specify:

```xml
<maven.compiler.source>11</maven.compiler.source>
<maven.compiler.target>11</maven.compiler.target>
```

Upgrading to Java 17 requires a planned migration including WildFly version upgrade. This MUST NOT happen incrementally.

### RULE-CODE-002: Package Structure

The following package structure is the enforced standard:

```
com.phoenix.finance/
    entity/                     # JPA entities and value objects
        investment/             # Investment-related entities and enums
        bond/                   # Bond-related entities and enums
        client/                 # Client/investor enums (renamed from client_enum)
        loan/                   # Loan-related entities and enums (renamed from loan_enum)
    exception/                  # Domain exception hierarchy (NEW)
    resource/                   # DAO interfaces and implementations
    service/                    # Service interfaces and implementations
    web/                        # Servlets, JSF managed beans, filters
    qualifier/                  # CDI qualifier annotations
    util/                       # Utility and helper classes
```

Rules:
- Sub-packages MUST use **camelCase**, not snake_case. `client_enum` → `client`. `loan_enum` → `loan`.
- New packages MUST NOT be created at the top level without documented justification.
- One public class per file. The filename MUST match the public class name.

### RULE-CODE-003: Naming Conventions

#### Classes

| Type | Convention | Example |
|------|-----------|---------|
| JPA Entity | Noun, singular | `Investment`, `MortgageLoan`, `Investor` |
| Service Interface | `<Entity>Service` | `InvestmentService`, `MortgageLoanService` |
| Service Implementation | `<Entity>ServiceImpl` | `InvestmentServiceImpl`, `MortgageLoanServiceImpl` |
| DAO Interface | `<Entity>Resource` | `InvestmentResource`, `InvestorResource` |
| DAO Implementation | `<Entity>ResourceImpl` | `InvestmentResourceImpl` |
| Servlet | `<Entity>Servlet` | `MortgageLoanServlet` |
| JSF Managed Bean | `<Entity>Controller` | `MortgageLoanController` |
| Exception | `<Domain>Exception` | `ValidationException`, `EntityNotFoundException` |
| Enum | Noun or adjective | `LoanStatus`, `InvestmentFund`, `Gender` |
| Value Object | Noun | `Money`, `ForecastItem` |
| Utility | `<Domain>Util` or `<Domain>Calculator` | `MortgagePayoffCalculator`, `ComplexPropertyBondUtil` |
| Filter | `<Purpose>Filter` | `LoggingContextFilter` |

#### Methods

| Type | Convention | Example |
|------|-----------|---------|
| Getters | `get<Property>()` | `getLoanAmount()` |
| Boolean getters | `is<Property>()` | `isActive()` |
| Setters | `set<Property>()` | `setLoanAmount()` |
| Finders | `findBy<Criteria>()` | `findByInvestorNum()`, `findById()` |
| Actions | verb + noun | `createLoan()`, `calculatePayoff()`, `deleteLoan()` |
| Test methods | `test<Method>_<scenario>_<outcome>()` | See testing.md |

#### Variables

- Local variables and parameters: **camelCase** — `investorNum`, `loanAmount`
- Constants: **UPPER_SNAKE_CASE** — `MAX_TERM_MONTHS`, `DEFAULT_RATE`
- Entity fields: **camelCase** — `loanId`, `interestRate`, `currentBalance`
- No single-letter variable names except loop counters (`i`, `j`) and lambda parameters

#### Database

- Table names: match entity class name (JPA default) — `MortgageLoan`, `Investment`
- Column names: match field name (JPA default) — `loanId`, `interestRate`
- Named queries: `<Entity>.findBy<Criteria>` — `Investment.findByInvestor`

### RULE-CODE-004: CDI and EJB Scoping Standard

The following scoping pattern is the enforced standard:

| Layer | Annotation | Rationale |
|-------|-----------|-----------|
| Services | `@Stateless` | Container-managed transactions, pooling, thread-safety |
| Resources (DAOs) | `@Stateless` | Same as services; EntityManager is not thread-safe |
| JSF Managed Beans | `@Named @ViewScoped` | State lives for the duration of a JSF view |
| Servlets | `@WebServlet` | Servlet container managed |
| Filters | `@WebFilter` | Servlet container managed |
| Singleton startup beans | `@Singleton @Startup` | Application-scoped lifecycle |
| Utility classes | Static methods (no CDI) | No state, no injection needed |

**PROHIBITED**:
- `@Stateless @Dependent` together (redundant — `@Stateless` manages its own lifecycle)
- `@ApplicationScoped` on beans that hold mutable state (thread-safety risk)
- `@ApplicationScoped @Transactional` on services — use `@Stateless` instead for container-managed transactions
- Field injection (`@Inject`) on non-CDI-managed classes

The existing `MortgageLoanServiceImpl` uses `@ApplicationScoped @Transactional`. This MUST be migrated to `@Stateless` to match the standard used by `InvestmentServiceImpl`, `InvestorServiceImpl`, and `PropertyBondServiceImpl`.

### RULE-CODE-005: Dependency Injection

- Use `@Inject` for CDI beans.
- Use `@PersistenceContext` for EntityManager (in `BaseResource` or DAO classes only).
- Use `@EJB` only for remote EJB references (none currently exist).
- Constructor injection is PREFERRED for testability. Field injection is ACCEPTABLE for CDI managed beans.
- PROHIBITED: Creating instances with `new` for service or resource classes. Use CDI injection.

### RULE-CODE-006: Entity Design

All JPA entities MUST follow these rules:

- `@Entity` annotation on the class.
- `@Id` with explicit generation strategy: `@GeneratedValue(strategy = GenerationType.IDENTITY)` for PostgreSQL.
- The `Investor` entity uses a business key (`investorNum`) as `@Id` — this is ACCEPTABLE as an existing pattern but MUST NOT be used for new entities. New entities MUST use surrogate keys (`Long` with `@GeneratedValue`).
- `@Column` annotations MUST specify `nullable` and `length` where applicable.
- All monetary values MUST use `BigDecimal` (or the `Money` embeddable). `double` and `float` for money are PROHIBITED.
- `equals()` and `hashCode()` MUST be based on the business key or entity ID, not on all fields.
- `toString()` MUST NOT include sensitive data (passwords, ID numbers, full account numbers).
- Cascade types MUST be explicit: `@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})`. Using `CascadeType.ALL` is PROHIBITED unless the child entity has no independent lifecycle.

### RULE-CODE-007: BigDecimal Usage

All financial calculations MUST use `BigDecimal`:

```java
// REQUIRED — explicit scale and rounding
BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
BigDecimal payment = principal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

// PROHIBITED — loses precision
double monthlyRate = annualRate / 12.0;
BigDecimal payment = BigDecimal.valueOf(principal * monthlyRate);

// PROHIBITED — no rounding mode specified
BigDecimal result = a.divide(b);  // throws ArithmeticException for non-terminating decimals
```

Rules:
- `divide()` MUST always specify scale and `RoundingMode`.
- Financial display values MUST use `setScale(2, RoundingMode.HALF_UP)`.
- Internal calculation intermediates MUST use at least scale 10 to avoid precision loss.
- Comparison MUST use `compareTo()`, not `equals()` (scale-sensitive).

### RULE-CODE-008: Null Handling

- Methods MUST NOT return `null` to indicate "not found" — throw `EntityNotFoundException` instead.
- Method parameters MUST be validated at system boundaries (see error-handling.md).
- `Optional` is ACCEPTABLE for service method return types when the caller legitimately needs to distinguish presence from absence (not for entities with IDs).
- Collections MUST be returned as empty collections, never `null`.
- Annotations: `@Column(nullable = false)` on required entity fields. Bean Validation `@NotNull` on required parameters exposed to users.

### RULE-CODE-009: API Design (Servlet / JAX-RS)

The current application uses servlets and JSF managed beans. These conventions apply:

**Servlets:**
- URL patterns MUST follow REST-like conventions: `/mortgage/loans`, `/investment/forecast`
- HTTP methods MUST be used correctly: GET for reads, POST for creates, PUT for updates, DELETE for deletes
- Request parameters MUST be validated (see error-handling.md)
- Response MUST be forwarded to a JSP view or returned as JSON (not a mix within the same servlet)

**JSF Managed Beans:**
- Action methods MUST return `void` for AJAX actions or a navigation outcome string
- `@PostConstruct` MUST only initialize state; it MUST NOT perform expensive operations without guard conditions
- View state MUST be `Serializable`

**Future JAX-RS Endpoints:**
- Path: `/api/v1/<resource>` (e.g., `/api/v1/loans`, `/api/v1/investments`)
- Request/Response: JSON only (`@Produces(MediaType.APPLICATION_JSON)`)
- Error responses: standard shape per error-handling.md

### RULE-CODE-010: Configuration Conventions

- Environment-specific values MUST come from environment variables or system properties.
- Application-internal configuration (route mappings, constants) MUST be in properties files or enums.
- `mapping.properties` is the route configuration file and MUST be kept in sync with actual servlet/controller mappings.
- Magic numbers are PROHIBITED. Use named constants:

```java
// CORRECT
private static final int MONTHS_PER_YEAR = 12;
private static final int MAX_LOAN_TERM_MONTHS = 360;

// PROHIBITED
int monthlyRate = rate / 12;
if (term > 360) { ... }
```

### RULE-CODE-011: Dependency Management

**Adding New Dependencies:**
1. The dependency MUST be compatible with Java 11 and Jakarta EE 8.
2. The dependency MUST NOT conflict with WildFly-provided libraries.
3. Dependencies provided by WildFly MUST use `<scope>provided</scope>`.
4. The version MUST be declared as a property in `<properties>`, not inline.
5. The dependency MUST be justified in the PR description.

**Version Properties Standard:**
All dependency versions MUST be managed via Maven properties:

```xml
<properties>
    <java.version>11</java.version>
    <jakarta.ee.version>8.0.0</jakarta.ee.version>
    <postgresql.version>42.7.7</postgresql.version>
    <hibernate.validator.version>6.2.5.Final</hibernate.validator.version>
    <hibernate.jpamodelgen.version>5.6.15.Final</hibernate.jpamodelgen.version>
    <junit.version>4.13.2</junit.version>
    <mockito.version>4.11.0</mockito.version>
    <arquillian.version>1.7.1.Final</arquillian.version>
    <jacoco.version>0.8.12</jacoco.version>
    <taglibs.version>1.2.5</taglibs.version>
    <surefire.version>3.2.5</surefire.version>
    <war.plugin.version>3.4.0</war.plugin.version>
    <compiler.plugin.version>3.13.0</compiler.plugin.version>
</properties>
```

**PROHIBITED:**
- Adding a dependency without a version property
- Using `RELEASE` or `LATEST` as version values
- Adding a dependency that duplicates functionality already available in Jakarta EE 8 or WildFly
- Adding a logging framework (Log4j, Logback) — use WildFly-provided SLF4J/JBoss Logging

### RULE-CODE-012: Code Organization Within Files

Standard file structure for a service implementation:

```java
package com.phoenix.finance.service;

// 1. Imports (grouped: java, javax/jakarta, third-party, project)
import java.math.BigDecimal;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.phoenix.finance.entity.Investment;
import com.phoenix.finance.exception.EntityNotFoundException;
import com.phoenix.finance.resource.InvestmentResource;

// 2. Class declaration
@Stateless
public class InvestmentServiceImpl implements InvestmentService {

    // 3. Logger (always first field)
    private static final Logger LOG = LoggerFactory.getLogger(InvestmentServiceImpl.class);

    // 4. Constants
    private static final int MAX_RESULTS = 100;

    // 5. Injected dependencies
    @Inject
    private InvestmentResource investmentResource;

    // 6. Public methods (interface contract)

    // 7. Private/helper methods
}
```

### RULE-CODE-013: Frontend Standards (JSP/Bootstrap)

- JSP pages MUST include common header and footer via `<%@ include %>` or `<jsp:include>`.
- All form inputs MUST have `id`, `name`, and `label` attributes.
- Bootstrap 4 is the UI framework. No additional CSS frameworks MUST be added without justification.
- Custom CSS MUST go in `resources/css/style.css` — inline styles are PROHIBITED.
- JavaScript MUST go in separate `.js` files — inline `<script>` blocks with business logic are PROHIBITED. Event handlers and initialization in `<script>` tags are acceptable.
- All user-facing text MUST be in English.
- Form validation MUST use HTML5 validation attributes (`required`, `min`, `max`, `pattern`) as a first line of defense, with server-side validation as the authoritative check.

### RULE-CODE-014: Git Hygiene

A `.gitignore` MUST exist in the repository root with at minimum:

```gitignore
# Build output
target/
*.war
*.class

# IDE files
.idea/
*.iml
.project
.classpath
.settings/
.vscode/

# Environment
.env
*.env.local

# OS files
.DS_Store
Thumbs.db

# Maven wrapper (optional — include if wrapper JARs are committed)
# .mvn/wrapper/maven-wrapper.jar
```

**PROHIBITED in version control:**
- `target/` directory or any build output
- `.env` files containing secrets
- IDE-specific configuration files
- WAR files or compiled classes

---

## Prohibited Anti-Patterns

| Anti-Pattern | Why Prohibited | Correct Alternative |
|-------------|----------------|---------------------|
| `double` or `float` for money | Precision loss in financial calculations | `BigDecimal` or `Money` embeddable |
| Magic numbers in calculations | Unreadable, error-prone | Named constants |
| `@Stateless @Dependent` | Redundant, confusing lifecycle | `@Stateless` only |
| `@ApplicationScoped` on services with mutable state | Thread-safety violations | `@Stateless` |
| `CascadeType.ALL` without justification | Accidental deletions | Explicit cascade types |
| `new ServiceImpl()` in controller | Bypasses CDI, breaks injection chain | `@Inject` |
| Inline SQL strings | SQL injection risk, unmaintainable | Named queries or Criteria API |
| Snake_case package names | Java convention violation | camelCase |
| Returning `null` for "not found" | Caller forgets null check, NPE | Throw `EntityNotFoundException` |
| `System.out.println` for debugging | Bypasses logging framework | `LOG.debug()` |
| Dead code (commented-out methods, unused imports) | Confusing, maintenance burden | Remove it |
| Hardcoded configuration values | Cannot change without rebuild | Environment variables or properties |

---

## Validation and Enforcement Approach

### Code Formatting

An `.editorconfig` file MUST be created in the repository root:

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_style = space
indent_size = 4
max_line_length = 120

[*.xml]
indent_style = space
indent_size = 4

[*.jsp]
indent_style = space
indent_size = 4

[*.{yml,yaml}]
indent_style = space
indent_size = 2

[*.md]
trim_trailing_whitespace = false

[*.properties]
indent_style = space
indent_size = 4
```

### Code Review Checklist (Coding Standards)

- [ ] Class/method/variable names follow naming conventions
- [ ] Package structure matches the enforced standard
- [ ] CDI scoping uses the correct annotation per layer
- [ ] `BigDecimal` used for all financial values
- [ ] `divide()` calls specify scale and `RoundingMode`
- [ ] No `null` returns for entity lookups
- [ ] No magic numbers — constants used
- [ ] No dead code or commented-out code
- [ ] No new dependencies without version properties and justification
- [ ] `.gitignore` includes build artifacts and secrets
- [ ] Imports are organized and unused imports are removed
- [ ] No inline styles in JSP pages

---

## Pull Request / Merge Acceptance Criteria

A pull request MUST NOT be merged if:

1. Java code does not compile against Java 11
2. New files are placed in incorrect packages
3. Class/method/variable naming violates conventions
4. `double` or `float` is used for financial values
5. `BigDecimal.divide()` is called without `RoundingMode`
6. A new dependency is added without a version property in `<properties>`
7. A new dependency conflicts with WildFly-provided libraries
8. Dead code (commented-out methods, unused imports) is present in new or modified files
9. Magic numbers appear in new or modified code
10. CDI scoping annotations are incorrect per the standard
11. `target/`, `.env`, or IDE files are included in the commit
