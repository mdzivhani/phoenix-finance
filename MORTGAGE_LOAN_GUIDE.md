# Phoenix Finance - Mortgage Loan Feature

## Overview
The Phoenix Finance application has been enhanced with comprehensive mortgage loan management capabilities. This feature allows you to track, manage, and forecast mortgage loans alongside your investment portfolio.

## New Features Added

### 1. **Mortgage Loan Management**
   - **Track Multiple Loans**: Manage multiple mortgage loans per investor
   - **Account Management**: Store detailed account information including account number, principal, and balance
   - **Interest Calculations**: Automatic monthly interest calculations based on current balance and rate
   - **Payment Tracking**: Record and monitor individual loan payments

### 2. **Loan Management Entities**
The following new database entities have been created:

#### MortgageLoan
Represents a mortgage loan with the following fields:
- **Loan ID**: Unique identifier (auto-generated)
- **Account Number**: Unique account identifier (e.g., "80-9262-8868")
- **Investor**: Associated investor
- **Principal Amount**: Original loan amount
- **Current Balance**: Outstanding balance
- **Interest Rate**: Annual interest rate (percentage)
- **Issued Date**: Loan start date
- **Maturity Date**: Loan end date
- **Monthly Payment**: Regular payment amount
- **Remaining Months**: Number of months left in the loan term
- **Equity Available**: Available equity in the property
- **Status**: Loan status (ACTIVE, PREPAID, COMPLETED, DEFAULTED, SUSPENDED)
- **Notes**: Additional notes

#### LoanPayment
Tracks individual payments made on a loan:
- **Payment ID**: Unique identifier
- **Loan**: Associated mortgage loan
- **Payment Date**: When payment was made
- **Payment Amount**: Total payment amount
- **Principal Paid**: Portion applied to principal
- **Interest Paid**: Portion applied to interest
- **Balance After Payment**: Outstanding balance after payment
- **Payment Method**: How payment was made
- **Notes**: Additional notes

### 3. **Services**
#### MortgageLoanService
Business logic service providing:
- `createLoan(MortgageLoan)` - Create new loan
- `getLoanById(Long)` - Retrieve specific loan
- `getLoansByInvestor(int)` - Get all loans for investor
- `getLoanByAccountNumber(String)` - Search by account number
- `getAllActiveLoans()` - Get active loans
- `updateLoan(MortgageLoan)` - Update loan details
- `deleteLoan(Long)` - Delete loan record
- `calculateRemainingBalance(MortgageLoan)` - Calculate balance
- `calculateMonthlyInterest(MortgageLoan)` - Calculate interest

### 4. **Web Interface**
#### Mortgage Loan Management Page
Located at: `/WEB-INF/view/mortgageLoanManagement.jsp`

**Features:**
- **View Loans Tab**: Display all loans for a selected investor
  - Select investor by number
  - View complete loan details in a table
  - Edit or delete loans
  - Display investor information

- **Search Tab**: Find loans by account number
  - Quick search functionality
  - Display detailed loan information
  - Show payment information and forecasts

- **Add New Loan Tab**: Create new mortgage loans
  - Enter investor number
  - Input account number
  - Set principal amount and interest rate
  - Define loan term dates and monthly payment
  - Specify remaining payment months

## Using Your Mortgage Loan Data

Based on your mortgage statement provided:
- **Account Number**: 80-9262-8868
- **Principal Amount**: R1,000,000.00 (outstanding balance)
- **Interest Rate**: 10.03%
- **Monthly Payment**: R20,650.10
- **Remaining Term**: 16 years 11 months

### To Add Your Loan:

1. Navigate to **Mortgage Loans** in the application menu
2. Click the **"Add New Loan"** tab
3. Enter your investor number
4. Fill in the following information:
   - **Account Number**: 80-9262-8868
   - **Principal Amount**: 1000000.00
   - **Interest Rate**: 10.03
   - **Issued Date**: 2025/09/26 (or your actual issue date)
   - **Maturity Date**: 2042/08/31 (approximately 16 years 11 months from issue)
   - **Monthly Payment**: 20650.10
   - **Remaining Months**: 203 (approximately 16 years * 12 months + 11 months)
5. Click **"Save Loan"**

### To View Your Loans:

1. Go to **Mortgage Loans** > **"View Loans"** tab
2. Enter your investor number
3. Click **"Load Loans"**
4. Your loans will be displayed in a table format

### To Search Specific Account:

1. Click **"Search by Account"** tab
2. Enter your account number: 80-9262-8868
3. View detailed loan information including:
   - Current balance
   - Monthly interest calculation
   - Payment schedule
   - Remaining term

## Technical Implementation

### Architecture
- **Pattern**: MVC (Model-View-Controller) with CDI and JSF
- **Database**: PostgreSQL (JPA/Hibernate)
- **Framework**: Jakarta EE 8, WildFly 25+

### Database Schema
Two new tables are created automatically:
- `MORTGAGELOAN` - Main loan records
- `LOANPAYMENT` - Individual payment history

### Service Layer
- `MortgageLoanService` interface defines contracts
- `MortgageLoanServiceImpl` provides implementation
- Injection via CDI for loose coupling

### Controller
- `MortgageLoanController` manages view logic
- JSF managed bean with @ViewScoped lifecycle
- Handles CRUD operations and calculations

## Installation & Build Instructions

### Prerequisites
- JDK 11+
- Maven 3.6+
- PostgreSQL 12+ (or use Docker Compose)
- WildFly 26+ (or use Docker)

### Building

```bash
cd phoenix_investment_finance
./mvnw clean package -DskipTests
```

This creates: `target/phoenix_investment_finance.war`

### Running with Docker Compose

```bash
cd ..
docker-compose up --build
```

Access at: `http://localhost:8080/phoenix_investment_finance/`

### Running Locally

1. **Start PostgreSQL**:
```powershell
# Windows
pwsh -File scripts/setup-postgres.ps1 -DbName phoenixdb -DbUser postgres -DbPassword postgres
```

2. **Start WildFly**:
```bash
%JBOSS_HOME%\bin\standalone.bat
```

3. **Deploy WAR**:
```bash
copy target\phoenix_investment_finance.war %JBOSS_HOME%\standalone\deployments\
```

4. **Access Application**:
Navigate to `http://localhost:8080/phoenix_investment_finance/`

## Future Enhancements

The mortgage loan module can be further enhanced with:
1. **Payment Automation**: Automatic payment tracking and reminders
2. **Amortization Schedules**: Detailed payment breakdowns
3. **Forecasts**: Loan payoff projections
4. **Comparisons**: Compare with investment returns
5. **Reports**: Monthly and annual statements
6. **Notifications**: Payment alerts and milestones
7. **Mobile Integration**: Mobile app for loan management
8. **Integration**: Combine with investment performance analysis

## Troubleshooting

### Build Issues
- Ensure Maven Wrapper has execute permissions
- Check that JDK 11+ is in PATH
- Clear Maven cache if dependency issues: `mvn clean dependency:purge-local-repository`

### Runtime Issues
- Verify PostgreSQL is running and accessible
- Check WildFly logs in `%JBOSS_HOME%\standalone\log\`
- Ensure datasource is configured in `phoenix_investment_finance-ds.xml`
- Check that persistence.xml is correctly configured

### Database Issues
- Reset database: `DROP DATABASE phoenixdb; CREATE DATABASE phoenixdb;`
- Rebuild schema: Set `hibernate.hbm2ddl.auto=update` in persistence.xml
- Check connection: Use `psql` to verify PostgreSQL connection

## Support & Documentation

- **Project Structure**: See project README.md
- **Technology Stack**: Jakarta EE 8, JSF 2.3, Hibernate/JPA
- **API**: REST endpoints available at `/api/mortgage-loans/*`

---
**Version**: 1.0
**Last Updated**: April 2026
**Created for**: Phoenix Finance Investment System
