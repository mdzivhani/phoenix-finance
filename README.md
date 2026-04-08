# Phoenix Finance

**Investment and Property Bond Management System with Mortgage Loan Acceleration**

A production-ready Java EE / Jakarta EE 8 web application for managing investments, property bonds, investor portfolios, and mortgage loan payoff acceleration planning.

## 📚 Documentation

| Document | Purpose | Read Time |
|----------|---------|-----------|
| **[QUICK_START_MORTGAGE.md](QUICK_START_MORTGAGE.md)** | Get running in 5 minutes | 5 min |
| **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** | Deploy with Docker or local WildFly | 20 min |
| **[MORTGAGE_LOAN_GUIDE.md](MORTGAGE_LOAN_GUIDE.md)** | Mortgage loan features reference | 15 min |
| **[PAYOFF_ACCELERATOR_GUIDE.md](PAYOFF_ACCELERATOR_GUIDE.md)** | Accelerated payoff strategies | 10 min |

## 🚀 Quick Start

```bash
# Build project
cd phoenix_investment_finance
.\mvnw.cmd clean package -DskipTests

# Run with Docker (recommended)
cd ..
docker-compose up -d

# Access application
# http://localhost:8080/phoenix_investment_finance/
```

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for detailed deployment options.

## 📋 Tech Stack

- **Java**: 11 LTS
- **Framework**: Jakarta EE 8 (JSF 2.3, CDI, JPA, JAX-RS)
- **Server**: WildFly 26+
- **Database**: PostgreSQL 12+
- **Build**: Maven 3.6+ (wrapper included)
- **Frontend**: Bootstrap 4, jQuery

## ✅ Features

✓ **Investment Management** - Track and forecast investment performance  
✓ **Property Bond Management** - Complex bond calculations with fees and duties  
✓ **Investor Portfolio Management** - Comprehensive investor tracking  
✓ **Mortgage Loan Management** - Full loan tracking and management  
✓ **Payoff Acceleration** - Multiple payoff strategy planning and comparison  
✓ **RESTful API** - JAX-RS based REST endpoints  
✓ **Web Interface** - JSF/JSP frontend with responsive Bootstrap design  

## 🔒 Security

- PostgreSQL JDBC Driver 42.7.7 (fixed CVE-2025-49146)
- Java 11 LTS
- Input validation on all endpoints
- Database transactions with JPA/Hibernate

## 👥 Team

Craig, Mulalo, Phomolo, Celokushe

## 📄 License

Apache License 2.0
