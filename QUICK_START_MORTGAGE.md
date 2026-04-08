# Quick Start - Phoenix Finance with Mortgage Loans

## What's New?
Your Phoenix Finance application now includes **Mortgage Loan Management** - a complete system to track your mortgage loan details, payments, and forecasts.

## Your Mortgage Loan Information (from statement)
- **Account #**: 80-9262-8868
- **Outstanding Balance**: R1,007,819.33
- **Total Installment**: R20,650.10
- **Interest Rate**: 10.03% p.a.
- **Remaining Term**: 16 Years 11 Months
- **Equity Available**: R24.97

## Get Started in 5 Minutes

### Step 1: Start the Application
```bash
cd c:\workbench\personal_projects\phoenix-finance
docker-compose up -d
```
*Or manually start WildFly if Docker isn't available*

### Step 2: Access the Application
Open your browser: `http://localhost:8080/phoenix_investment_finance/`

### Step 3: Navigate to Mortgage Loans
Click **"Mortgage Loans"** in the navigation menu

### Step 4: Add Your Loan
1. Click the **"Add New Loan"** tab
2. Enter your investor number
3. Fill in these details:
   ```
   Account Number:    80-9262-8868
   Principal Amount:  1000000.00
   Interest Rate:     10.03
   Issued Date:       2025-09-26
   Maturity Date:     2042-08-26
   Monthly Payment:   20650.10
   Remaining Months:  203
   ```
4. Click **"Save Loan"**

### Step 5: View Your Loan
1. Click **"View Loans"** tab
2. Enter your investor number
3. Click **"Load Loans"**
4. Your loan will display with all details

### Step 6: Search Your Loan
1. Click **"Search by Account"** tab
2. Enter: `80-9262-8868`
3. View complete loan details and calculations

## Key Features

✅ **Track Multiple Loans** - Manage all your mortgages in one place
✅ **Automatic Calculations** - Interest and balance calculated automatically
✅ **Payment History** - Record and track individual payments
✅ **Loan Status** - Track Active, Prepaid, Completed, or Defaulted loans
✅ **Quick Search** - Find loans by account number instantly
✅ **Investor Integration** - Link loans to investor profiles

## New Entities Created

1. **MortgageLoan** - Main loan record
2. **LoanPayment** - Individual payment tracking
3. **LoanStatus** - Enum (ACTIVE, PREPAID, COMPLETED, DEFAULTED, SUSPENDED)

## Services Available

**MortgageLoanService** provides:
- Create, read, update, delete loans
- Calculate remaining balance
- Calculate monthly interest
- Search by various criteria

## File Structure

```
phoenix_investment_finance/
├── src/main/java/com/phoenix/finance/
│   ├── entity/
│   │   ├── MortgageLoan.java          [NEW]
│   │   ├── LoanPayment.java           [NEW]
│   │   └── loan_enum/
│   │       └── LoanStatus.java        [NEW]
│   ├── service/
│   │   ├── MortgageLoanService.java        [NEW]
│   │   └── MortgageLoanServiceImpl.java     [NEW]
│   └── web/
│       └── MortgageLoanController.java     [NEW]
├── src/main/webapp/WEB-INF/view/
│   └── mortgageLoanManagement.jsp     [NEW]
└── resources/META-INF/
    └── persistence.xml                [UPDATED]
```

## Building from Source

```bash
cd c:\workbench\personal_projects\phoenix-finance\phoenix_investment_finance

# Build the project
mvnw.cmd clean package -DskipTests -DskipWildFlyDeploy=true

# WAR file created at:
# target/phoenix_investment_finance.war
```

## Troubleshooting

### Issue: Docker daemon not running
**Solution**: Start Docker Desktop or use local WildFly installation

### Issue: Can't find loans for investor
**Solution**: Ensure you enter a valid investor number that exists in the system

### Issue: Page shows blank/error
**Solution**: 
1. Check WildFly logs: `%JBOSS_HOME%\standalone\log\server.log`
2. Verify PostgreSQL is running
3. Clear browser cache (Ctrl+Shift+Delete)

## Next Steps

1. ✅ Add your mortgage loan information
2. ✅ Track loan payments as you make them
3. ✅ Monitor interest calculations
4. ✅ View loan forecast and payoff dates
5. ✅ Compare with your investment portfolio

## Support

For detailed information, see: `MORTGAGE_LOAN_GUIDE.md`

For project setup: `README.md`

---
**Mortgage Loan Module v1.0** - Ready to use!
