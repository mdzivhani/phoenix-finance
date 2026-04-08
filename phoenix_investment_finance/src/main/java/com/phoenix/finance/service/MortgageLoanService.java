package com.phoenix.finance.service;

import java.util.List;

import com.phoenix.finance.entity.MortgageLoan;

/**
 * Service interface for mortgage loan operations.
 * Provides CRUD operations and business logic for managing mortgage loans.
 */
public interface MortgageLoanService {

	/**
	 * Creates a new mortgage loan record.
	 * 
	 * @param loan the mortgage loan to create
	 * @return the created mortgage loan with generated ID
	 */
	MortgageLoan createLoan(MortgageLoan loan);

	/**
	 * Retrieves a mortgage loan by its ID.
	 * 
	 * @param loanId the loan ID
	 * @return the mortgage loan, or null if not found
	 */
	MortgageLoan getLoanById(Long loanId);

	/**
	 * Retrieves all loans for a specific investor.
	 * 
	 * @param investorNum the investor number
	 * @return list of mortgage loans for the investor
	 */
	List<MortgageLoan> getLoansByInvestor(int investorNum);

	/**
	 * Retrieves a mortgage loan by account number.
	 * 
	 * @param accountNumber the account number
	 * @return the mortgage loan, or null if not found
	 */
	MortgageLoan getLoanByAccountNumber(String accountNumber);

	/**
	 * Retrieves all active mortgage loans.
	 * 
	 * @return list of all active loans
	 */
	List<MortgageLoan> getAllActiveLoans();

	/**
	 * Updates an existing mortgage loan.
	 * 
	 * @param loan the mortgage loan to update
	 * @return the updated mortgage loan
	 */
	MortgageLoan updateLoan(MortgageLoan loan);

	/**
	 * Deletes a mortgage loan by its ID.
	 * 
	 * @param loanId the loan ID
	 */
	void deleteLoan(Long loanId);

	/**
	 * Calculates the remaining balance after making a payment.
	 * 
	 * @param loan the mortgage loan
	 * @return the remaining balance
	 */
	java.math.BigDecimal calculateRemainingBalance(MortgageLoan loan);

	/**
	 * Calculates the monthly interest based on current balance and rate.
	 * 
	 * @param loan the mortgage loan
	 * @return the monthly interest amount
	 */
	java.math.BigDecimal calculateMonthlyInterest(MortgageLoan loan);
}
