# Development Lifecycle Standard — Phoenix Finance

## Purpose

Define the end-to-end engineering workflow for the Phoenix Finance application. This standard makes the process repeatable from requirement to production, ensures every defect feeds back into improved tests and standards, and eliminates the cycle of fixing one issue while introducing another.

## Scope

This standard applies to:

- All development work: features, bug fixes, refactoring, infrastructure changes
- All contributors: developers and coding agents
- All stages: planning, implementation, testing, review, deployment, incident response

---

## Mandatory Rules

### RULE-LIFE-001: How Work Starts

Every unit of work MUST begin with a clear definition:

1. **Feature**: A GitHub Issue describing the requirement, acceptance criteria, and affected components.
2. **Bug Fix**: A GitHub Issue describing the observed behavior, expected behavior, steps to reproduce, and environment.
3. **Refactoring**: A GitHub Issue describing what is being changed, why, and what MUST NOT change in behavior.
4. **Infrastructure Change**: A GitHub Issue describing the change, impact assessment, and rollback plan.

Work without a linked issue is PROHIBITED for anything beyond trivial typo fixes.

> **Assumption**: GitHub Issues is the issue tracker (repository is hosted on GitHub). If another tracker is used, substitute accordingly, but the requirement for a written definition stands.

### RULE-LIFE-002: Translating Requirements into Tasks

Before writing code, the developer MUST:

1. **Read the issue** and confirm understanding of acceptance criteria.
2. **Identify affected files** — which entities, services, controllers, JSP pages, tests, and configuration files are impacted.
3. **Check existing tests** — are there tests for the affected code? If not, writing tests for the existing behavior is part of the task.
4. **Estimate risk** — does this change touch financial calculations (`MortgagePayoffCalculator`, `ComplexPropertyBondUtil`, `InvestmentForecastServiceImpl`)? If yes, additional test scrutiny is REQUIRED.
5. **Break down the work** into discrete commits:
   - Schema/entity changes (if any)
   - Service layer changes
   - Web layer changes
   - Tests
   - Configuration changes

Each commit MUST be independently compilable. A commit that breaks the build is PROHIBITED.

### RULE-LIFE-003: Branch Strategy

| Branch | Purpose | Protection |
|--------|---------|------------|
| `master` | Production-ready code | Protected: no direct pushes, require PR, require CI pass |
| `feature/<issue-number>-<short-description>` | Feature development | Created from `master` |
| `bugfix/<issue-number>-<short-description>` | Bug fixes | Created from `master` |
| `hotfix/<issue-number>-<short-description>` | Production emergency fixes | Created from `master`, fast-tracked review |

Examples:
- `feature/42-add-loan-amortization-schedule`
- `bugfix/57-payoff-calculator-overflow`
- `hotfix/63-null-pointer-on-loan-search`

Rules:
- Branch names MUST be lowercase with hyphens.
- Branch names MUST include the issue number.
- Long-lived feature branches (>5 working days) MUST rebase on `master` at least every 2 days.
- Stale branches (>30 days without activity) MUST be deleted.

### RULE-LIFE-004: Implementation Expectations

During implementation, the developer MUST:

1. Follow all standards in:
   - [coding-standards.md](coding-standards.md)
   - [error-handling.md](error-handling.md)
   - [logging.md](logging.md)
   - [testing.md](testing.md)
2. Write tests alongside code, not as an afterthought.
3. Keep commits small and focused — one logical change per commit.
4. Write clear commit messages following this format:

```
<type>(scope): <description>

[optional body explaining why, not what]

Refs: #<issue-number>
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `chore`

Examples:
```
feat(mortgage): add loan amortization schedule calculation

Adds monthly breakdown with principal, interest, and remaining balance
for standard and accelerated payoff scenarios.

Refs: #42
```

```
fix(payoff): prevent overflow when extra payment exceeds balance

Extra payment amount was not capped at the remaining balance,
causing negative balance in the payment schedule.

Refs: #57
```

### RULE-LIFE-005: Mandatory Local Validation Before Commit

Before committing, the developer MUST run and pass:

```bash
# 1. Compile and run all unit tests
cd phoenix_investment_finance
mvn clean test

# 2. Full verification including coverage (before PR)
mvn clean verify

# 3. If Docker/config changed, verify container build
cd ..
docker build -t phoenix-finance:local .
```

Committing code that fails `mvn clean test` is PROHIBITED.

### RULE-LIFE-006: Pull Request Rules

Every PR MUST:

1. **Reference the issue**: Include `Closes #<number>` or `Refs #<number>` in the PR description.
2. **Include a description**: What changed, why, and how to test it.
3. **Pass all CI checks**: Build, test, coverage, prohibited pattern checks.
4. **Include tests**: Per testing.md — features need tests, bug fixes need regression tests.
5. **Be reviewed**: At least one approval from a team member (or thorough self-review for solo projects with documented justification).
6. **Be rebased on `master`**: No merge commits from stale branches.

PR Description Template:

```markdown
## Summary
Brief description of the change.

## Issue
Closes #<number>

## Changes
- Changed X in Y because Z
- Added test for scenario A

## Testing
- [ ] Unit tests pass (`mvn clean test`)
- [ ] Integration tests pass (if applicable)
- [ ] Manual testing performed (describe steps)

## Checklist
- [ ] Code follows coding-standards.md
- [ ] Error handling follows error-handling.md
- [ ] Logging follows logging.md
- [ ] Tests follow testing.md
- [ ] No `System.out.println` or `e.printStackTrace()`
- [ ] No hardcoded credentials or environment values
- [ ] No dead code or commented-out code
```

### RULE-LIFE-007: Code Review Checklist

Reviewers MUST verify:

**Correctness:**
- [ ] Code does what the issue describes
- [ ] Edge cases are handled (null, empty, boundary values)
- [ ] Financial calculations use `BigDecimal` with correct rounding

**Standards Compliance:**
- [ ] Naming conventions followed
- [ ] CDI scoping correct per coding-standards.md
- [ ] Logging added per logging.md
- [ ] Error handling per error-handling.md
- [ ] Tests per testing.md

**Safety:**
- [ ] No sensitive data in logs or error messages
- [ ] No hardcoded credentials
- [ ] Input validation at system boundaries
- [ ] No `RuntimeException` wrapping — domain exceptions used

**Maintainability:**
- [ ] No dead code
- [ ] No magic numbers
- [ ] No duplicate logic
- [ ] Dependencies justified if new

**Deployment:**
- [ ] No breaking changes to configuration without migration path
- [ ] `hbm2ddl.auto` not changed to `update` or `create` in production paths
- [ ] Docker changes build successfully

### RULE-LIFE-008: CI Expectations

The CI pipeline (GitHub Actions — see deployment.md) MUST execute on every push to `master` and every pull request:

```
PR opened/updated → CI triggered → All checks pass → Review → Merge
```

CI stages:
1. **Compile**: `mvn compile` — code compiles without errors
2. **Unit Test**: `mvn test` — all unit tests pass
3. **Coverage**: JaCoCo verify — coverage thresholds met
4. **Pattern Check**: Grep for prohibited patterns (`System.out.println`, `printStackTrace`)
5. **Docker Build**: Container image builds successfully
6. **Integration Test** (optional, on demand): Arquillian tests against WildFly

A merge MUST NOT proceed if any CI stage fails.

### RULE-LIFE-009: Deployment Readiness Checklist

Before deploying (see deployment.md for full details):

- [ ] All CI checks pass on `master`
- [ ] Version number updated for release builds
- [ ] Health check endpoint responds
- [ ] Database migration scripts applied (if schema changed)
- [ ] `hbm2ddl.auto` set to `validate` for production
- [ ] Environment variables set (no defaults for secrets)
- [ ] Rollback plan documented
- [ ] Previous version archived

### RULE-LIFE-010: Incident Follow-Up

When a defect is found in production:

1. **Immediate**: Triage severity and determine if rollback is needed.
2. **Within 24 hours**: Create a GitHub Issue with:
   - Title: `[INCIDENT] <brief description>`
   - Steps to reproduce
   - Root cause analysis (even if preliminary)
   - Affected component and log evidence
3. **Fix**: Create a `hotfix/` branch. The fix MUST include:
   - A regression test that reproduces the bug
   - Updated logging if the defect was not visible in logs
   - Updated error handling if the error was silently swallowed
4. **Post-Fix**: Review whether the existing standards would have prevented this defect:
   - If YES: the standard was not followed — address in code review process.
   - If NO: the standard has a gap — update the relevant instruction file with a new rule.

This feedback loop is REQUIRED. Defects that repeat without standard updates are a process failure.

### RULE-LIFE-011: Standard Evolution

The instruction files in `/instructions/` are living documents:

- When a new pattern is adopted (e.g., adding Flyway for migrations), update the relevant instruction file.
- When a rule is found to be impractical, modify it with documented rationale — do not silently ignore it.
- Changes to instruction files MUST go through the same PR process as code changes.
- Instruction file changes MUST include the date and reason in the commit message.

---

## Prohibited Anti-Patterns

| Anti-Pattern | Why Prohibited |
|-------------|----------------|
| Working without a linked issue | No traceability, no acceptance criteria |
| Committing directly to `master` | Bypasses review and CI |
| "It works on my machine" as proof | Must pass CI, not just local |
| Fixing a bug without a regression test | Bug will recur |
| Large monolithic PRs (>500 lines changed) | Unreviewable, high merge conflict risk |
| Merging with failing CI | Broken code reaches production |
| Production defects without follow-up | Same defect repeats |
| Standards ignored with no feedback | Standards rot, become irrelevant |
| Logs as the only validation of a fix | Automated tests are the validation |
| Deploying on Friday afternoon | Reduced capacity for incident response |

---

## Required Implementation Patterns

### Pattern 1: Feature Development Workflow

```
1. Issue created → assigned → understood
2. Branch created: feature/<issue>-<description>
3. Implementation with tests (iterative)
4. Local validation: mvn clean verify
5. Push branch → PR opened
6. CI runs → all checks pass
7. Code review → feedback addressed
8. Rebase on master if needed
9. Merge (squash or rebase)
10. Delete branch
11. Verify deployment (if applicable)
```

### Pattern 2: Bug Fix Workflow

```
1. Issue created with reproduction steps
2. Branch created: bugfix/<issue>-<description>
3. Write failing test that reproduces the bug
4. Fix the bug
5. Verify test passes
6. Add/update logging if defect was invisible
7. Local validation: mvn clean verify
8. Push branch → PR opened
9. PR description explains root cause
10. CI runs → merge → deploy
```

### Pattern 3: Hotfix Workflow

```
1. Incident detected → severity assessed
2. Decision: rollback or hotfix
3. If hotfix: branch from master (hotfix/<issue>-<description>)
4. Minimal fix + regression test
5. Fast-tracked review (same-day)
6. Merge → deploy → monitor
7. Post-incident: update standards if gap found
```

### Pattern 4: Refactoring Workflow

```
1. Issue created explaining the refactoring goal and scope
2. Branch created: refactor/<issue>-<description>
3. Ensure existing tests pass BEFORE any changes
4. Refactor in small steps — run tests after each step
5. No behavior changes — tests must not change assertions
6. If tests are insufficient, add characterization tests first
7. Local validation → PR → CI → merge
```

---

## Validation and Enforcement Approach

### Process Enforcement

| Control | Mechanism | Status |
|---------|-----------|--------|
| Branch protection on `master` | GitHub branch protection rules | MUST be configured |
| Require PR for merge | GitHub branch protection | MUST be configured |
| Require CI pass for merge | GitHub required status checks | MUST be configured |
| Require issue link in PR | PR template + review checklist | Manual enforcement |
| Require tests with features | Review checklist + coverage gate | Manual + automated |
| Require regression test with bug fix | Review checklist | Manual enforcement |
| Post-incident standard review | RULE-LIFE-010 | Manual enforcement |

### Recommended GitHub Repository Settings

```
Branch protection rules for 'master':
  ✓ Require pull request reviews before merging (1 approval minimum)
  ✓ Require status checks to pass before merging
    - Required: "Build and Test" (CI workflow)
  ✓ Require branches to be up to date before merging
  ✓ Do not allow bypassing the above settings
  ✗ Allow force pushes: DISABLED
  ✗ Allow deletions: DISABLED
```

---

## Pull Request / Merge Acceptance Criteria

A pull request MUST NOT be merged if:

1. It has no linked issue (except trivial typo/formatting fixes)
2. CI pipeline has not run or has failures
3. No tests are included for new features or bug fixes
4. The PR description is empty or does not explain the change
5. The code review checklist items are not addressed
6. The branch is not up to date with `master`
7. The commit messages do not follow the `<type>(scope): <description>` format
8. The PR introduces changes outside the scope of the linked issue without explanation
9. Dead code, commented-out code, or `TODO` comments are present without linked issues
10. The change breaks any existing test
