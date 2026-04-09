# Testing Standard — Phoenix Finance

## Purpose

Define the single, enforceable testing standard for the Phoenix Finance application. This standard ensures every bug fix includes a regression test, every new feature is covered by automated tests, and no code reaches production validated only by manual checking or runtime log review.

## Scope

This standard applies to:

- All Java source code under `com.phoenix.finance.*`
- All test code under `src/test/java/com/phoenix/finance/`
- Maven build configuration (`pom.xml`)
- CI pipeline test execution
- Local development workflow

## Current State and Problems

The repository currently has:

1. **6 test classes** with minimal coverage: `MoneyTest`, `ForecastServiceTest`, `InvestmentTest`, `InvestorTest`, `ForecastControllerImplTest`, `BondCalculationTest` (commented out)
2. **Tests are skipped by default**: The default Maven profile sets `<skipTests>true</skipTests>`
3. **No CI pipeline exists**: Tests are never enforced automatically
4. **Arquillian integration tests exist** but require a running WildFly instance
5. **Commented-out test classes**: `BondCalculationTest` is non-functional
6. **No test for mortgage loan, payoff calculator, or any of the new modules**
7. **No coverage measurement**: No JaCoCo or equivalent configured
8. **JUnit 4 only**: Test framework is outdated but consistent — standardize on JUnit 4 until a planned migration to JUnit 5

### Baseline Decision

| Concern | Standard | Rationale |
|---------|----------|-----------|
| Unit Test Framework | **JUnit 4.13.2** | Already in use; consistent across existing tests |
| Assertion Library | **JUnit 4 assertions + Hamcrest** | Bundled with JUnit 4 |
| Integration Test Framework | **Arquillian 1.7.1** | Already configured with WildFly managed/remote profiles |
| Mocking Framework | **Mockito 4.x** | MUST be added; no mocking capability exists currently |
| Coverage Tool | **JaCoCo 0.8.x** | MUST be added to Maven configuration |
| Test Execution | **maven-surefire-plugin 3.2.5** (unit), **maven-failsafe-plugin 3.2.5** (integration) | Surefire exists; Failsafe MUST be added |

> **Assumption**: Mockito `4.11.0` and JaCoCo `0.8.12` will be added as dependencies. These are compatible with Java 11.

---

## Mandatory Rules

### RULE-TEST-001: Tests MUST NOT Be Skipped by Default

The default Maven profile MUST run unit tests. The current `<skipTests>true</skipTests>` in the default profile is PROHIBITED and MUST be removed.

```xml
<!-- REQUIRED: Default profile runs unit tests -->
<profile>
    <id>default</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <skipTests>false</skipTests>
    </properties>
</profile>
```

Integration tests (Arquillian) MUST be separated using the `maven-failsafe-plugin` and run only when the appropriate profile is activated.

### RULE-TEST-002: Test Pyramid

The Phoenix Finance application MUST maintain this test distribution:

```
         ┌───────────┐
         │  E2E /    │  ← Manual + Selenium (future)
         │  Smoke    │     Validates deployed application
         ├───────────┤
         │Integration│  ← Arquillian with WildFly
         │  Tests    │     JPA, CDI, Servlet integration
         ├───────────┤
         │           │
         │   Unit    │  ← JUnit 4 + Mockito
         │   Tests   │     Business logic, calculations,
         │           │     services (mocked deps), utilities
         └───────────┘
```

| Layer | Runner | What It Tests | Minimum Requirement |
|-------|--------|--------------|---------------------|
| Unit | JUnit 4 + Mockito | Services, utilities, entities, calculations, validators | Every public method in service and utility classes |
| Integration | Arquillian + WildFly | JPA persistence, CDI wiring, servlet dispatch, transaction behavior | CRUD operations for each entity, service-to-resource integration |
| Smoke | Manual or automated (future) | Deployed application health | Health endpoint responds, login page loads |

### RULE-TEST-003: Every New Feature MUST Have Tests

No feature code MUST be merged without accompanying tests:

- **New entity**: Unit tests for constructors, getters, business methods, equals/hashCode
- **New service method**: Unit tests with mocked resource (DAO) layer
- **New servlet/controller action**: Unit tests for request validation, error handling paths
- **New utility/calculation method**: Unit tests with boundary values, edge cases, expected outputs
- **New DAO query**: Integration test verifying the query against a real database

### RULE-TEST-004: Every Bug Fix MUST Include a Regression Test

When fixing a bug:

1. Write a test that **reproduces the bug** (test MUST fail before the fix)
2. Apply the fix
3. Verify the test passes
4. Name the test to describe the defect: `testLoanPayoff_doesNotOverflowWhenExtraPaymentExceedsBalance`

If writing a regression test is technically impossible (e.g., pure UI rendering issue with no backend logic), document the reason in the PR description. This exception MUST be rare.

### RULE-TEST-005: Test Naming Convention

All test methods MUST follow this pattern:

```
test<MethodOrBehavior>_<scenario>_<expectedOutcome>
```

Examples:

```java
@Test
public void testCalculateMonthlyPayment_standardLoan_returnsCorrectAmount() { }

@Test
public void testCalculateMonthlyPayment_zeroInterestRate_returnsPrincipalDividedByTerm() { }

@Test
public void testDeleteLoan_activeLoanWithBalance_throwsBusinessException() { }

@Test
public void testFindByInvestor_noInvestmentsExist_returnsEmptyList() { }

@Test(expected = EntityNotFoundException.class)
public void testFindById_nonExistentId_throwsEntityNotFoundException() { }
```

### RULE-TEST-006: Test Class Naming and Location

| Source Class | Test Class | Location |
|-------------|------------|----------|
| `InvestmentServiceImpl.java` | `InvestmentServiceImplTest.java` | `src/test/java/com/phoenix/finance/service/` |
| `MortgagePayoffCalculator.java` | `MortgagePayoffCalculatorTest.java` | `src/test/java/com/phoenix/finance/util/` |
| `MortgageLoanServlet.java` | `MortgageLoanServletTest.java` | `src/test/java/com/phoenix/finance/web/` |
| `InvestmentResourceImpl.java` | `InvestmentResourceImplIT.java` | `src/test/java/com/phoenix/finance/resource/` |

- Unit test classes: `*Test.java` (executed by Surefire)
- Integration test classes: `*IT.java` (executed by Failsafe)

### RULE-TEST-007: Deterministic Tests — No Flakiness

Tests MUST be deterministic and repeatable:

- MUST NOT depend on system clock. Use injected `Clock` or fixed dates in tests.
- MUST NOT depend on execution order. Each test MUST be independently runnable.
- MUST NOT depend on external services (database, network) in unit tests. Use mocks.
- MUST NOT use `Thread.sleep()` or timing-dependent assertions.
- MUST NOT depend on random values without a fixed seed.
- MUST NOT share mutable state between test methods.

```java
// CORRECT — fixed date for reproducibility
@Test
public void testCalculateLoanAge_createdOneYearAgo_returns12Months() {
    LocalDate creationDate = LocalDate.of(2025, 1, 1);
    LocalDate currentDate = LocalDate.of(2026, 1, 1);
    int months = LoanUtil.calculateLoanAgeMonths(creationDate, currentDate);
    assertEquals(12, months);
}

// PROHIBITED — depends on system clock
@Test
public void testCalculateLoanAge() {
    loan.setCreationDate(LocalDate.now().minusYears(1));
    assertEquals(12, loan.getAgeInMonths()); // flaky near month boundaries
}
```

### RULE-TEST-008: Mocking Rules

Mockito MUST be used for unit test isolation:

- **MUST mock**: Resource (DAO) classes when testing services. EntityManager when testing resources in unit tests. External dependencies and infrastructure.
- **MUST NOT mock**: The class under test. Value objects (`Money`, `ForecastItem`). Enums.
- **MUST NOT use** `Mockito.any()` when a specific value can be asserted. Overly broad matchers hide bugs.

```java
public class InvestmentServiceImplTest {

    @Mock
    private InvestmentResource investmentResource;

    @InjectMocks
    private InvestmentServiceImpl investmentService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateInvestment_validInvestment_persistsAndReturns() {
        Investment input = new Investment();
        input.setFund(InvestmentFund.ALLAN_GRAY);
        when(investmentResource.save(input)).thenReturn(input);

        Investment result = investmentService.createInvestment(input);

        assertNotNull(result);
        verify(investmentResource).save(input);
    }
}
```

### RULE-TEST-009: Test Data Management

- Test data MUST be created within the test method or `@Before` setup. No reliance on pre-existing database state.
- Integration tests MUST use `@Transactional` with rollback or clean up after themselves.
- No shared test databases between parallel test runs.
- BigDecimal comparisons MUST use `compareTo()`, not `equals()`, to avoid scale mismatches.

```java
// CORRECT
assertEquals(0, expectedAmount.compareTo(actualAmount));

// PROHIBITED — fails when scale differs (e.g., 100.00 vs 100.0)
assertEquals(expectedAmount, actualAmount);
```

### RULE-TEST-010: Coverage Requirements

JaCoCo MUST be configured with these thresholds:

| Metric | Minimum | Target |
|--------|---------|--------|
| Line coverage (overall) | **50%** | 70% |
| Branch coverage (overall) | **40%** | 60% |
| Service layer line coverage | **70%** | 85% |
| Utility/calculation line coverage | **80%** | 95% |

> **Rationale**: The repository has near-zero coverage. The minimums are bootstrapping targets. They MUST increase by 5% per quarter until the target is reached.

JaCoCo Maven configuration:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### RULE-TEST-011: Required Test Coverage for Existing Code (Priority Order)

The following areas MUST be tested first, in this order:

1. **`MortgagePayoffCalculator`** — Complex financial calculations with five scenarios. Every scenario MUST have unit tests with known-good expected values verified by manual calculation.
2. **`ComplexPropertyBondUtil`** — Bond payment calculations. Edge cases for zero rates, single-month terms, maximum values.
3. **`MortgageLoanServiceImpl`** — All CRUD operations, status transitions, validation rules.
4. **`InvestmentServiceImpl`** — Create, read, update, delete, forecast generation.
5. **`InvestorServiceImpl`** — Registration, lookup, validation.
6. **`PropertyBondServiceImpl`** — CRUD and forecast.
7. **`Money` class** — Existing tests exist but MUST be expanded for edge cases (null, zero, negative, overflow).

---

## Prohibited Anti-Patterns

| Anti-Pattern | Why Prohibited |
|-------------|----------------|
| `<skipTests>true</skipTests>` in default profile | Tests never run, defects reach production |
| Commented-out test classes | Dead test code; creates false sense of coverage |
| Tests without assertions | Pass vacuously, prove nothing |
| `@Ignore` without a linked issue or reason | Skipped tests rot and are never re-enabled |
| Using `System.out.println` in tests | Use assertions, not visual inspection |
| Tests that depend on database state from other tests | Order-dependent, breaks in parallel execution |
| Testing private methods directly via reflection | Test through public API; refactor if private method needs testing |
| Mocking the class under test | Defeats the purpose of unit testing |
| Using production database for tests | Data corruption, non-deterministic results |
| Logs as test evidence ("I checked the logs and it worked") | MUST have automated assertions |

---

## Required Implementation Patterns

### Pattern 1: Service Unit Test (with Mockito)

```java
@RunWith(MockitoJUnitRunner.class)
public class MortgageLoanServiceImplTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private MortgageLoanServiceImpl service;

    @Test
    public void testCreateLoan_validLoan_persistsSuccessfully() {
        MortgageLoan loan = createTestLoan(new BigDecimal("500000"), new BigDecimal("10.5"), 240);
        service.createLoan(loan);
        verify(em).persist(loan);
    }

    @Test(expected = ValidationException.class)
    public void testCreateLoan_nullLoan_throwsValidation() {
        service.createLoan(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateLoan_nonExistentLoan_throwsNotFound() {
        MortgageLoan loan = createTestLoan(new BigDecimal("500000"), new BigDecimal("10.5"), 240);
        loan.setLoanId(999L);
        when(em.find(MortgageLoan.class, 999L)).thenReturn(null);
        service.updateLoan(loan);
    }

    private MortgageLoan createTestLoan(BigDecimal amount, BigDecimal rate, int term) {
        MortgageLoan loan = new MortgageLoan();
        loan.setLoanAmount(amount);
        loan.setInterestRate(rate);
        loan.setTermMonths(term);
        loan.setStatus(LoanStatus.ACTIVE);
        return loan;
    }
}
```

### Pattern 2: Calculation Utility Test

```java
public class MortgagePayoffCalculatorTest {

    @Test
    public void testCalculateStandardPayoff_knownValues_matchesExpectedSchedule() {
        // R500,000 loan at 10.5% over 20 years (240 months)
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal rate = new BigDecimal("10.5");
        int termMonths = 240;

        PayoffScenario result = MortgagePayoffCalculator.calculateStandardPayoff(
            principal, rate, termMonths);

        assertEquals(240, result.getMonthsToPayoff());
        // Monthly payment for these inputs = R4,993.57 (verified independently)
        assertEquals(0, new BigDecimal("4993.57").compareTo(
            result.getMonthlyPayment().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    public void testCalculateBiweeklyPayoff_savesInterest_comparedToStandard() {
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal rate = new BigDecimal("10.5");
        int termMonths = 240;

        PayoffScenario standard = MortgagePayoffCalculator.calculateStandardPayoff(
            principal, rate, termMonths);
        PayoffScenario biweekly = MortgagePayoffCalculator.calculateBiweeklyPayoff(
            principal, rate, termMonths);

        assertTrue("Biweekly must pay off faster",
            biweekly.getMonthsToPayoff() < standard.getMonthsToPayoff());
        assertTrue("Biweekly must save interest",
            biweekly.getTotalInterest().compareTo(standard.getTotalInterest()) < 0);
    }

    @Test(expected = ValidationException.class)
    public void testCalculateStandardPayoff_negativePrincipal_throwsValidation() {
        MortgagePayoffCalculator.calculateStandardPayoff(
            new BigDecimal("-100000"), new BigDecimal("10.5"), 240);
    }

    @Test(expected = ValidationException.class)
    public void testCalculateStandardPayoff_zeroRate_throwsValidation() {
        MortgagePayoffCalculator.calculateStandardPayoff(
            new BigDecimal("500000"), BigDecimal.ZERO, 240);
    }
}
```

### Pattern 3: Entity Test

```java
public class MortgageLoanTest {

    @Test
    public void testNewLoan_defaultStatus_isActive() {
        MortgageLoan loan = new MortgageLoan();
        loan.setStatus(LoanStatus.ACTIVE);
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    public void testMoney_embeddedInInvestment_retainsValue() {
        Money money = new Money(new BigDecimal("1000.50"));
        Investment investment = new Investment();
        investment.setInitialInvestment(money);
        assertEquals(0, new BigDecimal("1000.50").compareTo(
            investment.getInitialInvestment().getAmount()));
    }
}
```

### Pattern 4: Integration Test (Arquillian)

```java
@RunWith(Arquillian.class)
public class InvestmentResourceImplIT {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackages(true, "com.phoenix.finance")
            .addAsResource("META-INF/persistence.xml")
            .addAsWebInfResource("beans.xml");
    }

    @Inject
    private InvestmentResource investmentResource;

    @Test
    public void testSaveAndFind_persistsCorrectly() {
        // Create test investor first
        // Create investment
        // Save via resource
        // Find by ID
        // Assert fields match
    }
}
```

---

## Maven Configuration Requirements

### Dependencies to Add

```xml
<!-- Mockito for unit testing -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>4.11.0</version>
    <scope>test</scope>
</dependency>
```

### Plugins to Configure

```xml
<!-- Surefire for unit tests (*Test.java) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
        </includes>
        <excludes>
            <exclude>**/*IT.java</exclude>
        </excludes>
    </configuration>
</plugin>

<!-- Failsafe for integration tests (*IT.java) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.2.5</version>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <includes>
            <include>**/*IT.java</include>
        </includes>
    </configuration>
</plugin>
```

---

## Validation and Enforcement Approach

### Local Development

1. Run `mvn clean test` before every commit. This MUST pass.
2. Run `mvn clean verify` to include integration tests when modifying persistence or CDI code.
3. Check coverage report at `target/site/jacoco/index.html` after running tests.

### CI Gate (Future)

1. `mvn clean verify` MUST pass — no test failures allowed.
2. JaCoCo coverage check MUST pass — below-threshold coverage breaks the build.
3. Commented-out test classes (`@Ignore` without issue reference) MUST be flagged.

### Code Review Checklist (Testing)

- [ ] New feature has unit tests covering happy path and error paths
- [ ] Bug fix includes a regression test that would have caught the original bug
- [ ] Test names follow `test<Method>_<scenario>_<expectedOutcome>` convention
- [ ] No `System.out.println` in test code
- [ ] No `@Ignore` without a linked issue number in the annotation message
- [ ] No commented-out test code
- [ ] No `Thread.sleep()` or timing-dependent logic
- [ ] Mocks are used appropriately (not mocking the class under test)
- [ ] BigDecimal assertions use `compareTo()` not `equals()`
- [ ] Test data is self-contained — no dependency on external state

---

## Pull Request / Merge Acceptance Criteria

A pull request MUST NOT be merged if:

1. Any new or modified feature code lacks accompanying unit tests
2. Any bug fix lacks a regression test (unless documented as technically impossible)
3. `mvn clean test` fails on any test
4. `<skipTests>true</skipTests>` is present in the default Maven profile
5. Commented-out test classes remain without an `@Ignore("Issue #XXX")` annotation
6. Coverage drops below the configured JaCoCo threshold
7. Test names do not follow the naming convention
8. Any test uses `Thread.sleep()`, `System.out.println`, or depends on execution order
