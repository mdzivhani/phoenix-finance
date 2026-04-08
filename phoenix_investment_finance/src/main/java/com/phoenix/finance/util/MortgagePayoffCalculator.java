package com.phoenix.finance.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.phoenix.finance.entity.MortgageLoan;

/**
 * Utility class for mortgage loan acceleration and early payoff calculations.
 * Provides methods to calculate payoff scenarios, interest savings, and payment schedules.
 */
public class MortgagePayoffCalculator {

	private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal(12);
	private static final BigDecimal HUNDRED = new BigDecimal(100);

	/**
	 * Represents a monthly payment breakdown
	 */
	public static class PaymentBreakdown {
		public int monthNumber;
		public LocalDate paymentDate;
		public BigDecimal principalPayment;
		public BigDecimal interestPayment;
		public BigDecimal totalPayment;
		public BigDecimal balanceAfterPayment;

		public PaymentBreakdown(int month, LocalDate date, BigDecimal principal, BigDecimal interest,
				BigDecimal total, BigDecimal balance) {
			this.monthNumber = month;
			this.paymentDate = date;
			this.principalPayment = principal;
			this.interestPayment = interest;
			this.totalPayment = total;
			this.balanceAfterPayment = balance;
		}

		@Override
		public String toString() {
			return String.format("Month %d (%s): Principal=%.2f, Interest=%.2f, Total=%.2f, Balance=%.2f",
					monthNumber, paymentDate, principalPayment, interestPayment, totalPayment, balanceAfterPayment);
		}
	}

	/**
	 * Represents a payoff scenario with timeline and savings
	 */
	public static class PayoffScenario {
		public BigDecimal additionalMonthlyPayment;
		public int monthsToPayoff;
		public LocalDate payoffDate;
		public BigDecimal totalInterestPaid;
		public BigDecimal interestSaved;
		public BigDecimal totalAmountPaid;
		public List<PaymentBreakdown> paymentSchedule;

		public PayoffScenario() {
			this.paymentSchedule = new ArrayList<>();
		}

		@Override
		public String toString() {
			return String.format(
					"Additional Payment: R%.2f | Payoff in %d months (%s) | Interest Saved: R%.2f | Total Paid: R%.2f",
					additionalMonthlyPayment, monthsToPayoff, payoffDate, interestSaved, totalAmountPaid);
		}
	}

	/**
	 * Calculates the current monthly interest portion of the standard payment
	 */
	public static BigDecimal calculateMonthlyInterest(MortgageLoan loan) {
		if (loan == null || loan.getInterestRate() == null || loan.getCurrentBalance() == null) {
			return BigDecimal.ZERO;
		}

		// Monthly interest = (Balance * Annual Rate) / 12 / 100
		BigDecimal monthlyRate = loan.getInterestRate().divide(MONTHS_PER_YEAR, 6, RoundingMode.HALF_UP)
				.divide(HUNDRED, 6, RoundingMode.HALF_UP);
		return loan.getCurrentBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Calculates the principal portion of the standard payment
	 */
	public static BigDecimal calculateMonthlyPrincipal(MortgageLoan loan) {
		if (loan == null || loan.getMonthlyPayment() == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal monthlyInterest = calculateMonthlyInterest(loan);
		return loan.getMonthlyPayment().subtract(monthlyInterest).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Calculates total interest that will be paid over remaining loan term
	 */
	public static BigDecimal calculateTotalRemainingInterest(MortgageLoan loan) {
		if (loan == null || loan.getMonthlyPayment() == null || loan.getRemainingMonths() == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal totalPayments = loan.getMonthlyPayment()
				.multiply(new BigDecimal(loan.getRemainingMonths()));
		return totalPayments.subtract(loan.getCurrentBalance()).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Generates a payoff scenario with additional monthly payment
	 */
	public static PayoffScenario calculatePayoffWithExtraPayment(MortgageLoan loan,
			BigDecimal extraMonthlyPayment) {
		if (loan == null || extraMonthlyPayment == null || extraMonthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
			return null;
		}

		PayoffScenario scenario = new PayoffScenario();
		scenario.additionalMonthlyPayment = extraMonthlyPayment;

		BigDecimal balance = loan.getCurrentBalance();
		BigDecimal monthlyPayment = loan.getMonthlyPayment().add(extraMonthlyPayment);
		BigDecimal annualRate = loan.getInterestRate();
		BigDecimal monthlyRateDecimal = annualRate.divide(MONTHS_PER_YEAR.multiply(HUNDRED), 8,
				RoundingMode.HALF_UP);

		LocalDate currentDate = loan.getIssuedDate() != null 
			? loan.getIssuedDate().toLocalDate().plusMonths(loan.getPrincipalAmount().compareTo(loan.getCurrentBalance()) > 0 
				? loan.getPrincipalAmount().subtract(loan.getCurrentBalance()).divide(loan.getMonthlyPayment(), 0, RoundingMode.DOWN).intValue() 
				: 0)
			: LocalDate.now();
		
		int month = 0;
		BigDecimal totalInterestPaid = BigDecimal.ZERO;

		while (balance.compareTo(BigDecimal.ZERO) > 0 && month < 600) { // Safety limit: 50 years
			month++;
			BigDecimal interestPayment = balance.multiply(monthlyRateDecimal).setScale(2, RoundingMode.HALF_UP);
			BigDecimal principalPayment = monthlyPayment.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);

			if (principalPayment.compareTo(BigDecimal.ZERO) <= 0) {
				break; // Payment doesn't cover interest
			}

			if (principalPayment.compareTo(balance) > 0) {
				principalPayment = balance;
			}

			BigDecimal totalPayment = principalPayment.add(interestPayment);
			balance = balance.subtract(principalPayment).setScale(2, RoundingMode.HALF_UP);
			totalInterestPaid = totalInterestPaid.add(interestPayment);

			LocalDate paymentDate = currentDate.plusMonths(month - 1);
			PaymentBreakdown breakdown = new PaymentBreakdown(month, paymentDate, principalPayment,
					interestPayment, totalPayment, balance.max(BigDecimal.ZERO));
			scenario.paymentSchedule.add(breakdown);
		}

		scenario.monthsToPayoff = month;
		scenario.payoffDate = currentDate.plusMonths(month);
		scenario.totalInterestPaid = totalInterestPaid;
		scenario.totalAmountPaid = loan.getCurrentBalance().add(totalInterestPaid);

		BigDecimal standardTotalInterest = calculateTotalRemainingInterest(loan);
		scenario.interestSaved = standardTotalInterest.subtract(totalInterestPaid).setScale(2,
				RoundingMode.HALF_UP);

		return scenario;
	}

	/**
	 * Calculates biweekly payment equivalent (26 payments/year = 13 monthly payments)
	 * Results in approximately 1 extra monthly payment per year
	 */
	public static PayoffScenario calculateBiweeklyPayoffScenario(MortgageLoan loan) {
		if (loan == null || loan.getMonthlyPayment() == null) {
			return null;
		}

		// Biweekly = Monthly * 26 / 24 (approximation for 26 biweekly payments = 13 monthly)
		BigDecimal biweeklyAmount = loan.getMonthlyPayment().multiply(new BigDecimal("26"))
				.divide(new BigDecimal("24"), 2, RoundingMode.HALF_UP);
		BigDecimal extraPayment = biweeklyAmount.subtract(loan.getMonthlyPayment()).multiply(new BigDecimal("12"))
				.divide(new BigDecimal("26"), 2, RoundingMode.HALF_UP);

		return calculatePayoffWithExtraPayment(loan, extraPayment);
	}

	/**
	 * Calculates payoff scenario with accelerated annual payment
	 * (One extra month's payment per year)
	 */
	public static PayoffScenario calculateAcceleratedPayoffScenario(MortgageLoan loan) {
		if (loan == null || loan.getMonthlyPayment() == null) {
			return null;
		}

		// Extra payment = monthly payment / 12 (to accumulate to 1 extra payment/year)
		BigDecimal extraPayment = loan.getMonthlyPayment().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
		return calculatePayoffWithExtraPayment(loan, extraPayment);
	}

	/**
	 * Finds the monthly extra payment needed to pay off loan by target date
	 */
	public static PayoffScenario calculatePayoffByTargetDate(MortgageLoan loan, int targetMonths) {
		if (loan == null || targetMonths <= 0) {
			return null;
		}

		// Binary search for the right extra payment amount
		BigDecimal minExtra = BigDecimal.ZERO;
		BigDecimal maxExtra = loan.getCurrentBalance();
		BigDecimal tolerance = new BigDecimal("0.01");

		for (int iteration = 0; iteration < 20; iteration++) {
			BigDecimal midExtra = minExtra.add(maxExtra).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
			PayoffScenario scenario = calculatePayoffWithExtraPayment(loan, midExtra);

			if (scenario == null) {
				break;
			}

			if (scenario.monthsToPayoff < targetMonths) {
				maxExtra = midExtra;
			} else {
				minExtra = midExtra;
			}

			if (maxExtra.subtract(minExtra).compareTo(tolerance) < 0) {
				return scenario;
			}
		}

		return calculatePayoffWithExtraPayment(loan, minExtra);
	}

	/**
	 * Calculates months saved by making extra payment
	 */
	public static int calculateMonthsSaved(MortgageLoan loan, BigDecimal extraPayment) {
		if (loan == null || loan.getRemainingMonths() == null) {
			return 0;
		}

		PayoffScenario scenario = calculatePayoffWithExtraPayment(loan, extraPayment);
		if (scenario == null) {
			return 0;
		}

		return loan.getRemainingMonths() - scenario.monthsToPayoff;
	}

	/**
	 * Calculates payoff date with extra payment
	 */
	public static LocalDate calculatePayoffDate(MortgageLoan loan, BigDecimal extraPayment) {
		PayoffScenario scenario = calculatePayoffWithExtraPayment(loan, extraPayment);
		return scenario != null ? scenario.payoffDate : null;
	}

	/**
	 * Generates multiple payoff scenarios for comparison
	 */
	public static List<PayoffScenario> generateComparisonScenarios(MortgageLoan loan) {
		List<PayoffScenario> scenarios = new ArrayList<>();

		if (loan == null || loan.getMonthlyPayment() == null) {
			return scenarios;
		}

		// Standard payment (no extra)
		scenarios.add(calculatePayoffWithExtraPayment(loan, BigDecimal.ZERO));

		// Small extra payment (R100)
		scenarios.add(calculatePayoffWithExtraPayment(loan, new BigDecimal("100")));

		// Medium extra payment (R500)
		scenarios.add(calculatePayoffWithExtraPayment(loan, new BigDecimal("500")));

		// Large extra payment (Monthly payment / 2)
		scenarios.add(calculatePayoffWithExtraPayment(loan, loan.getMonthlyPayment().divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP)));

		// Biweekly
		scenarios.add(calculateBiweeklyPayoffScenario(loan));

		// Accelerated (extra month/year)
		scenarios.add(calculateAcceleratedPayoffScenario(loan));

		return scenarios;
	}
}
