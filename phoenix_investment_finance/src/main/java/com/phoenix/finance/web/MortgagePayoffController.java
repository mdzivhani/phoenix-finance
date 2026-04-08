package com.phoenix.finance.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.phoenix.finance.entity.MortgageLoan;
import com.phoenix.finance.service.MortgageLoanService;
import com.phoenix.finance.util.MortgagePayoffCalculator;
import com.phoenix.finance.util.MortgagePayoffCalculator.PaymentBreakdown;
import com.phoenix.finance.util.MortgagePayoffCalculator.PayoffScenario;

/**
 * JSF Managed Bean for mortgage loan payoff acceleration and early repayment calculations.
 * Allows users to model various payment strategies and see interest savings.
 */
@Named
@ViewScoped
public class MortgagePayoffController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private MortgageLoanService mortgageLoanService;

	private Long selectedLoanId;
	private MortgageLoan selectedLoan;
	private List<MortgageLoan> allLoans;

	// User input for payoff scenarios
	private BigDecimal customExtraPayment = BigDecimal.ZERO;
	private int targetPayoffYears = 10;

	// Calculated scenarios
	private PayoffScenario standardPaymentScenario;
	private PayoffScenario customPaymentScenario;
	private PayoffScenario biweeklyScenario;
	private PayoffScenario acceleratedScenario;
	private PayoffScenario targetDateScenario;
	private List<PayoffScenario> comparisonScenarios;

	// Payment schedule display
	private List<PaymentBreakdown> selectedPaymentSchedule;
	private boolean showPaymentSchedule = false;

	// Summary calculations
	private BigDecimal totalRemainingInterest;
	private int remainingMonths;
	private LocalDate standardPayoffDate;

	@PostConstruct
	public void init() {
		allLoans = mortgageLoanService.getAllActiveLoans();
	}

	/**
	 * Loads a specific loan and calculates all payoff scenarios
	 */
	public void loadLoan() {
		if (selectedLoanId == null || selectedLoanId <= 0) {
			addMessage(FacesMessage.SEVERITY_WARN, "Selection Required", "Please select a valid loan");
			return;
		}

		try {
			selectedLoan = mortgageLoanService.getLoanById(selectedLoanId);
			if (selectedLoan == null) {
				addMessage(FacesMessage.SEVERITY_ERROR, "Loan Not Found", "The selected loan could not be found");
				return;
			}

			// Calculate summary information
			calculateSummaryInfo();

			// Generate all scenarios
			generateAllScenarios();

			addMessage(FacesMessage.SEVERITY_INFO, "Loan Loaded", "Loan loaded successfully. Review payoff scenarios below.");
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error loading loan: " + e.getMessage());
		}
	}

	/**
	 * Calculates summary information for the loan
	 */
	private void calculateSummaryInfo() {
		totalRemainingInterest = MortgagePayoffCalculator.calculateTotalRemainingInterest(selectedLoan);
		remainingMonths = selectedLoan.getRemainingMonths();
		standardPayoffDate = selectedLoan.getMaturityDate() != null 
			? selectedLoan.getMaturityDate().toLocalDate()
			: LocalDate.now().plusMonths(remainingMonths);
	}

	/**
	 * Generates all payoff scenarios
	 */
	private void generateAllScenarios() {
		try {
			// Standard payment (no extra)
			standardPaymentScenario = MortgagePayoffCalculator.calculatePayoffWithExtraPayment(selectedLoan,
					BigDecimal.ZERO);

			// Custom extra payment
			if (customExtraPayment != null && customExtraPayment.compareTo(BigDecimal.ZERO) > 0) {
				customPaymentScenario = MortgagePayoffCalculator.calculatePayoffWithExtraPayment(selectedLoan,
						customExtraPayment);
			}

			// Biweekly scenario
			biweeklyScenario = MortgagePayoffCalculator.calculateBiweeklyPayoffScenario(selectedLoan);

			// Accelerated scenario (extra month per year)
			acceleratedScenario = MortgagePayoffCalculator.calculateAcceleratedPayoffScenario(selectedLoan);

			// Target date scenario
			int targetMonths = targetPayoffYears * 12;
			if (targetMonths < remainingMonths) {
				targetDateScenario = MortgagePayoffCalculator.calculatePayoffByTargetDate(selectedLoan,
						targetMonths);
			}

			// Comparison scenarios
			comparisonScenarios = MortgagePayoffCalculator.generateComparisonScenarios(selectedLoan);
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Calculation Error", "Error generating scenarios: " + e.getMessage());
		}
	}

	/**
	 * Calculates payoff scenarios when custom extra payment is changed
	 */
	public void calculateCustomPayoff() {
		if (selectedLoan == null) {
			addMessage(FacesMessage.SEVERITY_WARN, "No Loan Selected", "Please select a loan first");
			return;
		}

		if (customExtraPayment == null || customExtraPayment.compareTo(BigDecimal.ZERO) <= 0) {
			customPaymentScenario = null;
			addMessage(FacesMessage.SEVERITY_WARN, "Invalid Amount", "Extra payment must be greater than zero");
			return;
		}

		try {
			customPaymentScenario = MortgagePayoffCalculator.calculatePayoffWithExtraPayment(selectedLoan,
					customExtraPayment);
			addMessage(FacesMessage.SEVERITY_INFO, "Calculated",
					String.format("Payoff in %d months with R%.2f extra payment", customPaymentScenario.monthsToPayoff,
							customExtraPayment));
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error calculating payoff: " + e.getMessage());
		}
	}

	/**
	 * Calculates payoff scenario for target date
	 */
	public void calculateTargetDatePayoff() {
		if (selectedLoan == null) {
			addMessage(FacesMessage.SEVERITY_WARN, "No Loan Selected", "Please select a loan first");
			return;
		}

		if (targetPayoffYears <= 0 || targetPayoffYears * 12 >= remainingMonths) {
			addMessage(FacesMessage.SEVERITY_WARN, "Invalid Target", "Target must be less than current term");
			return;
		}

		try {
			int targetMonths = targetPayoffYears * 12;
			targetDateScenario = MortgagePayoffCalculator.calculatePayoffByTargetDate(selectedLoan, targetMonths);
			if (targetDateScenario != null) {
				addMessage(FacesMessage.SEVERITY_INFO, "Calculated",
						String.format("Pay R%.2f extra/month to finish in %d years", 
							targetDateScenario.additionalMonthlyPayment, targetPayoffYears));
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error calculating target date: " + e.getMessage());
		}
	}

	/**
	 * Displays the full payment schedule for a scenario
	 */
	public void showPaymentSchedule(PayoffScenario scenario) {
		if (scenario != null && scenario.paymentSchedule != null) {
			selectedPaymentSchedule = scenario.paymentSchedule;
			showPaymentSchedule = true;
		}
	}

	/**
	 * Hides the payment schedule
	 */
	public void hidePaymentSchedule() {
		showPaymentSchedule = false;
		selectedPaymentSchedule = null;
	}

	/**
	 * Generates a summary comparison string
	 */
	public String generateComparisonSummary() {
		if (standardPaymentScenario == null || customPaymentScenario == null) {
			return "";
		}

		int monthsSaved = standardPaymentScenario.monthsToPayoff - customPaymentScenario.monthsToPayoff;
		BigDecimal interestSaved = standardPaymentScenario.totalInterestPaid
				.subtract(customPaymentScenario.totalInterestPaid);

		return String.format(
				"By adding R%.2f/month, you save R%.2f in interest and pay off %d months (%d years) earlier!",
				customExtraPayment, interestSaved, monthsSaved, monthsSaved / 12);
	}

	/**
	 * Gets the current monthly interest amount
	 */
	public BigDecimal getCurrentMonthlyInterest() {
		if (selectedLoan == null) {
			return BigDecimal.ZERO;
		}
		return MortgagePayoffCalculator.calculateMonthlyInterest(selectedLoan);
	}

	/**
	 * Gets the current monthly principal amount
	 */
	public BigDecimal getCurrentMonthlyPrincipal() {
		if (selectedLoan == null) {
			return BigDecimal.ZERO;
		}
		return MortgagePayoffCalculator.calculateMonthlyPrincipal(selectedLoan);
	}

	/**
	 * Suggests the extra payment needed to pay off in X years
	 */
	public BigDecimal suggestExtraPaymentForYears(int years) {
		if (targetDateScenario != null) {
			return targetDateScenario.additionalMonthlyPayment;
		}
		return BigDecimal.ZERO;
	}

	// Getters and Setters
	public Long getSelectedLoanId() {
		return selectedLoanId;
	}

	public void setSelectedLoanId(Long selectedLoanId) {
		this.selectedLoanId = selectedLoanId;
	}

	public MortgageLoan getSelectedLoan() {
		return selectedLoan;
	}

	public void setSelectedLoan(MortgageLoan selectedLoan) {
		this.selectedLoan = selectedLoan;
	}

	public List<MortgageLoan> getAllLoans() {
		return allLoans;
	}

	public void setAllLoans(List<MortgageLoan> allLoans) {
		this.allLoans = allLoans;
	}

	public BigDecimal getCustomExtraPayment() {
		return customExtraPayment;
	}

	public void setCustomExtraPayment(BigDecimal customExtraPayment) {
		this.customExtraPayment = customExtraPayment;
	}

	public int getTargetPayoffYears() {
		return targetPayoffYears;
	}

	public void setTargetPayoffYears(int targetPayoffYears) {
		this.targetPayoffYears = targetPayoffYears;
	}

	public PayoffScenario getStandardPaymentScenario() {
		return standardPaymentScenario;
	}

	public PayoffScenario getCustomPaymentScenario() {
		return customPaymentScenario;
	}

	public PayoffScenario getBiweeklyScenario() {
		return biweeklyScenario;
	}

	public PayoffScenario getAcceleratedScenario() {
		return acceleratedScenario;
	}

	public PayoffScenario getTargetDateScenario() {
		return targetDateScenario;
	}

	public List<PayoffScenario> getComparisonScenarios() {
		return comparisonScenarios != null ? comparisonScenarios : new ArrayList<>();
	}

	public List<PaymentBreakdown> getSelectedPaymentSchedule() {
		return selectedPaymentSchedule;
	}

	public void setSelectedPaymentSchedule(List<PaymentBreakdown> selectedPaymentSchedule) {
		this.selectedPaymentSchedule = selectedPaymentSchedule;
	}

	public boolean isShowPaymentSchedule() {
		return showPaymentSchedule;
	}

	public void setShowPaymentSchedule(boolean showPaymentSchedule) {
		this.showPaymentSchedule = showPaymentSchedule;
	}

	public BigDecimal getTotalRemainingInterest() {
		return totalRemainingInterest;
	}

	public int getRemainingMonths() {
		return remainingMonths;
	}

	public LocalDate getStandardPayoffDate() {
		return standardPayoffDate;
	}

	/**
	 * Helper method to add FacesMessage
	 */
	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}
}
