package com.phoenix.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import com.phoenix.finance.entity.MortgageLoan;

/**
 * Service implementation for mortgage loan operations.
 * Provides CRUD operations and business logic for managing mortgage loans.
 */
@ApplicationScoped
@Transactional
public class MortgageLoanServiceImpl implements MortgageLoanService {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public MortgageLoan createLoan(MortgageLoan loan) {
		if (loan == null) {
			throw new IllegalArgumentException("Mortgage loan cannot be null");
		}
		entityManager.persist(loan);
		entityManager.flush();
		return loan;
	}

	@Override
	public MortgageLoan getLoanById(Long loanId) {
		if (loanId == null || loanId <= 0) {
			throw new IllegalArgumentException("Loan ID must be valid");
		}
		return entityManager.find(MortgageLoan.class, loanId);
	}

	@Override
	public List<MortgageLoan> getLoansByInvestor(int investorNum) {
		TypedQuery<MortgageLoan> query = entityManager.createQuery(
				"SELECT m FROM MortgageLoan m WHERE m.investor.investorNum = :investorNum ORDER BY m.issuedDate DESC",
				MortgageLoan.class);
		query.setParameter("investorNum", investorNum);
		return query.getResultList();
	}

	@Override
	public MortgageLoan getLoanByAccountNumber(String accountNumber) {
		if (accountNumber == null || accountNumber.trim().isEmpty()) {
			throw new IllegalArgumentException("Account number cannot be null or empty");
		}
		try {
			TypedQuery<MortgageLoan> query = entityManager.createQuery(
					"SELECT m FROM MortgageLoan m WHERE m.accountNumber = :accountNumber", MortgageLoan.class);
			query.setParameter("accountNumber", accountNumber);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<MortgageLoan> getAllActiveLoans() {
		TypedQuery<MortgageLoan> query = entityManager.createQuery(
				"SELECT m FROM MortgageLoan m WHERE m.status = com.phoenix.finance.entity.loan_enum.LoanStatus.ACTIVE ORDER BY m.issuedDate DESC",
				MortgageLoan.class);
		return query.getResultList();
	}

	@Override
	public MortgageLoan updateLoan(MortgageLoan loan) {
		if (loan == null || loan.getLoanId() == null) {
			throw new IllegalArgumentException("Mortgage loan and loan ID cannot be null");
		}
		return entityManager.merge(loan);
	}

	@Override
	public void deleteLoan(Long loanId) {
		if (loanId == null || loanId <= 0) {
			throw new IllegalArgumentException("Loan ID must be valid");
		}
		MortgageLoan loan = entityManager.find(MortgageLoan.class, loanId);
		if (loan != null) {
			entityManager.remove(loan);
		}
	}

	@Override
	public BigDecimal calculateRemainingBalance(MortgageLoan loan) {
		if (loan == null) {
			throw new IllegalArgumentException("Mortgage loan cannot be null");
		}
		return loan.getCurrentBalance();
	}

	@Override
	public BigDecimal calculateMonthlyInterest(MortgageLoan loan) {
		if (loan == null) {
			throw new IllegalArgumentException("Mortgage loan cannot be null");
		}
		if (loan.getInterestRate() == null || loan.getCurrentBalance() == null) {
			return BigDecimal.ZERO;
		}

		// Calculate monthly interest: (Balance * Annual Rate) / 12 / 100
		BigDecimal annualRate = loan.getInterestRate();
		BigDecimal balance = loan.getCurrentBalance();
		BigDecimal monthlyInterest = balance.multiply(annualRate).divide(new BigDecimal(1200), 2,
				RoundingMode.HALF_UP);

		return monthlyInterest;
	}
}
