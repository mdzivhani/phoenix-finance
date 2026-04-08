package com.phoenix.finance.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.MortgageLoan;
import com.phoenix.finance.entity.loan_enum.LoanStatus;
import com.phoenix.finance.service.InvestorService;
import com.phoenix.finance.service.MortgageLoanService;

/**
 * JSF Managed Bean for handling mortgage loan management operations.
 * Supports viewing, creating, and updating mortgage loans.
 */
@Named
@ViewScoped
public class MortgageLoanController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private MortgageLoanService mortgageLoanService;

	@Inject
	private InvestorService investorService;

	private MortgageLoan currentLoan;
	private List<MortgageLoan> allLoans;
	private List<MortgageLoan> investorLoans;
	private Investor selectedInvestor;
	private int selectedInvestorNum;
	private boolean editMode;
	private LoanStatus[] loanStatuses;

	@PostConstruct
	public void init() {
		allLoans = mortgageLoanService.getAllActiveLoans();
		loanStatuses = LoanStatus.values();
		currentLoan = new MortgageLoan();
		editMode = false;
	}

	/**
	 * Loads loans for the selected investor.
	 */
	public void loadInvestorLoans() {
		if (selectedInvestorNum > 0) {
			investorLoans = mortgageLoanService.getLoansByInvestor(selectedInvestorNum);
			selectedInvestor = investorService.getInvestorByNumber(selectedInvestorNum);
		}
	}

	/**
	 * Creates or updates a mortgage loan.
	 */
	public void saveLoan() {
		try {
			if (currentLoan.getInvestor() == null && selectedInvestorNum > 0) {
				selectedInvestor = investorService.getInvestorByNumber(selectedInvestorNum);
				currentLoan.setInvestor(selectedInvestor);
			}

			if (currentLoan.getLoanId() == null) {
				mortgageLoanService.createLoan(currentLoan);
				addMessage(FacesMessage.SEVERITY_INFO, "Success", "Mortgage loan created successfully");
			} else {
				mortgageLoanService.updateLoan(currentLoan);
				addMessage(FacesMessage.SEVERITY_INFO, "Success", "Mortgage loan updated successfully");
			}

			reset();
			loadInvestorLoans();
			allLoans = mortgageLoanService.getAllActiveLoans();
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error saving mortgage loan: " + e.getMessage());
		}
	}

	/**
	 * Initiates edit mode for a loan.
	 */
	public void editLoan(MortgageLoan loan) {
		currentLoan = loan;
		selectedInvestorNum = loan.getInvestor().getInvestorNum();
		selectedInvestor = loan.getInvestor();
		editMode = true;
	}

	/**
	 * Deletes a mortgage loan.
	 */
	public void deleteLoan(Long loanId) {
		try {
			mortgageLoanService.deleteLoan(loanId);
			addMessage(FacesMessage.SEVERITY_INFO, "Success", "Mortgage loan deleted successfully");
			loadInvestorLoans();
			allLoans = mortgageLoanService.getAllActiveLoans();
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error deleting mortgage loan: " + e.getMessage());
		}
	}

	/**
	 * Retrieves loan details by account number.
	 */
	public void viewLoanByAccountNumber(String accountNumber) {
		try {
			currentLoan = mortgageLoanService.getLoanByAccountNumber(accountNumber);
			if (currentLoan != null) {
				selectedInvestor = currentLoan.getInvestor();
				selectedInvestorNum = currentLoan.getInvestor().getInvestorNum();
				editMode = true;
			} else {
				addMessage(FacesMessage.SEVERITY_WARN, "Not Found", "Loan account not found");
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error retrieving loan: " + e.getMessage());
		}
	}

	/**
	 * Calculates remaining balance for the current loan.
	 */
	public BigDecimal getRemainingBalance() {
		if (currentLoan != null) {
			return mortgageLoanService.calculateRemainingBalance(currentLoan);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * Calculates monthly interest for the current loan.
	 */
	public BigDecimal getMonthlyInterest() {
		if (currentLoan != null) {
			return mortgageLoanService.calculateMonthlyInterest(currentLoan);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * Resets the current loan and form state.
	 */
	public void reset() {
		currentLoan = new MortgageLoan();
		selectedInvestorNum = 0;
		editMode = false;
	}

	// Getters and Setters
	public MortgageLoan getCurrentLoan() {
		return currentLoan;
	}

	public void setCurrentLoan(MortgageLoan currentLoan) {
		this.currentLoan = currentLoan;
	}

	public List<MortgageLoan> getAllLoans() {
		return allLoans;
	}

	public void setAllLoans(List<MortgageLoan> allLoans) {
		this.allLoans = allLoans;
	}

	public List<MortgageLoan> getInvestorLoans() {
		return investorLoans;
	}

	public void setInvestorLoans(List<MortgageLoan> investorLoans) {
		this.investorLoans = investorLoans;
	}

	public Investor getSelectedInvestor() {
		return selectedInvestor;
	}

	public void setSelectedInvestor(Investor selectedInvestor) {
		this.selectedInvestor = selectedInvestor;
	}

	public int getSelectedInvestorNum() {
		return selectedInvestorNum;
	}

	public void setSelectedInvestorNum(int selectedInvestorNum) {
		this.selectedInvestorNum = selectedInvestorNum;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public LoanStatus[] getLoanStatuses() {
		return loanStatuses;
	}

	/**
	 * Helper method to add FacesMessage.
	 */
	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}
}
