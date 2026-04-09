# Phoenix Finance

**Investment and Property Bond Management System with Mortgage Loan Acceleration**

A production-ready Jakarta EE 10 web application built on WildFly 39 and Java 25, for managing investments, property bonds, investor portfolios, and mortgage loan payoff acceleration planning.

## 🚀 Quick Start

```powershell
# Run with Docker (recommended)
docker compose up --build -d

# Access application
# http://localhost:8080/phoenix_investment_finance/
```

```powershell
# Build and test locally
cd phoenix_investment_finance
.\mvnw.cmd clean test
```

## 📋 Tech Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 25 | Eclipse Temurin 25 (LTS) |
| Jakarta EE | 10 | `jakarta.*` namespace — `javax.*` is not supported |
| WildFly | 39.0.1.Final | Jakarta EE 10 runtime |
| Jakarta Faces | 4.0 | Facelets `.xhtml` views |
| JSTL | `jakarta.tags.core` | Provided by WildFly 39 |
| PostgreSQL driver | 42.7.7 | JDBC driver (no known CVEs) |
| Maven | 3.9.9 | Via Maven wrapper (`mvnw`/`mvnw.cmd`) |
| Docker | 25+ | Multi-stage build |

## ✅ Features

- **Investment Management** — Track and forecast investment performance
- **Property Bond Management** — Complex bond calculations with fees and duties
- **Investor Portfolio Management** — Comprehensive investor tracking
- **Mortgage Loan Management** — Full loan tracking and management
- **Payoff Acceleration** — Multiple payoff strategy planning and comparison
- **Web Interface** — Jakarta Faces (Facelets) + JSP frontend with Bootstrap

## 🏗️ Architecture

The application uses a dual-view approach:

| View Type | Technology | URL Pattern | Notes |
|-----------|-----------|-------------|-------|
| JSP views | Custom `Dispatcher` servlet | `/finance/*.jsp` | Investment, bonds, investors |
| Facelets views | `FacesServlet` (Jakarta Faces 4.0) | `/*.xhtml` | Mortgage loan, payoff accelerator |

## 🔧 Running Locally

### Prerequisites
- Docker Desktop 24+
- Java 25 (Eclipse Temurin recommended)

### Docker (recommended)

```powershell
# Clone and start
git clone https://github.com/mdzivhani/phoenix-finance.git
cd phoenix-finance
docker compose up --build -d
```

The app will be available at `http://localhost:8080/phoenix_investment_finance/`

PostgreSQL starts on port `5432` with default credentials (see `docker-compose.yml`).

### Verify deployment (smoke tests)

```powershell
$b = "http://localhost:8080/phoenix_investment_finance"
Invoke-WebRequest "$b/finance/enterInvestmentDetails.jsp" -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "$b/finance/enterInvestorNumber.jsp"    -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "$b/mortgageLoanManagement.xhtml"       -UseBasicParsing | Select-Object StatusCode
Invoke-WebRequest "$b/mortgagePayoffAccelerator.xhtml"    -UseBasicParsing | Select-Object StatusCode
# All must return 200
```

## 🧪 Tests

```powershell
cd phoenix_investment_finance
.\mvnw.cmd clean test
# Expected: Tests run: 8, Failures: 0, Errors: 0
```

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) | Docker and local WildFly deployment |
| [MORTGAGE_LOAN_GUIDE.md](MORTGAGE_LOAN_GUIDE.md) | Mortgage loan feature reference |
| [PAYOFF_ACCELERATOR_GUIDE.md](PAYOFF_ACCELERATOR_GUIDE.md) | Payoff strategy calculator guide |
| [instructions/deployment.md](instructions/deployment.md) | Deployment rules (RULE-DEP-001 to RULE-DEP-012) |

## 🔒 Security

- PostgreSQL JDBC Driver 42.7.7 (no known CVEs)
- Jakarta EE 10 security model (CDI, EJB transaction management)
- Input validation via Jakarta Bean Validation 3.0
- Database credentials injected via environment variables — never hardcoded

## 👥 Team

Craig, Mulalo, Phomolo, Celokushe

## 📄 License

Apache License 2.0
