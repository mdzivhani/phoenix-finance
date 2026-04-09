package com.phoenix.finance.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;

import com.phoenix.finance.entity.loan_enum.LoanStatus;

/**
 * Represents a mortgage loan associated with an investor.
 * Tracks loan details including principal, interest rate, payment schedule, and outstanding balance.
 */
@Entity
@SequenceGenerator(name = "mortgage_loan_seq", sequenceName = "mortgage_loan_id_seq", allocationSize = 1)
public class MortgageLoan implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mortgage_loan_seq")
	private Long loanId;

	@ManyToOne
	@JoinColumn(name = "investor_num", nullable = false)
	private Investor investor;

	@Column(nullable = false, unique = true)
	private String accountNumber;

	@Column(nullable = false)
	private BigDecimal principalAmount;

	@Column(nullable = false)
	private BigDecimal currentBalance;

	@Column(nullable = false)
	private BigDecimal interestRate;

	@Column(nullable = false)
	private Date issuedDate;

	@Column(nullable = false)
	private Date maturityDate;

	@Column(nullable = false)
	private BigDecimal monthlyPayment;

	@Column(nullable = false)
	private Integer remainingMonths;

	@Column
	private BigDecimal equityAvailable;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LoanStatus status;

	@Column
	private String notes;

	// Constructors
	public MortgageLoan() {
	}

	public MortgageLoan(Investor investor, String accountNumber, BigDecimal principalAmount,
			BigDecimal interestRate, Date issuedDate, Date maturityDate, BigDecimal monthlyPayment,
			Integer remainingMonths) {
		this.investor = investor;
		this.accountNumber = accountNumber;
		this.principalAmount = principalAmount;
		this.currentBalance = principalAmount;
		this.interestRate = interestRate;
		this.issuedDate = issuedDate;
		this.maturityDate = maturityDate;
		this.monthlyPayment = monthlyPayment;
		this.remainingMonths = remainingMonths;
		this.status = LoanStatus.ACTIVE;
		this.equityAvailable = BigDecimal.ZERO;
	}

	// Getters and Setters
	public Long getLoanId() {
		return loanId;
	}

	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}

	public Investor getInvestor() {
		return investor;
	}

	public void setInvestor(Investor investor) {
		this.investor = investor;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BigDecimal getPrincipalAmount() {
		return principalAmount;
	}

	public void setPrincipalAmount(BigDecimal principalAmount) {
		this.principalAmount = principalAmount;
	}

	public BigDecimal getCurrentBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(BigDecimal currentBalance) {
		this.currentBalance = currentBalance;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	public Date getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(Date maturityDate) {
		this.maturityDate = maturityDate;
	}

	public BigDecimal getMonthlyPayment() {
		return monthlyPayment;
	}

	public void setMonthlyPayment(BigDecimal monthlyPayment) {
		this.monthlyPayment = monthlyPayment;
	}

	public Integer getRemainingMonths() {
		return remainingMonths;
	}

	public void setRemainingMonths(Integer remainingMonths) {
		this.remainingMonths = remainingMonths;
	}

	public BigDecimal getEquityAvailable() {
		return equityAvailable;
	}

	public void setEquityAvailable(BigDecimal equityAvailable) {
		this.equityAvailable = equityAvailable;
	}

	public LoanStatus getStatus() {
		return status;
	}

	public void setStatus(LoanStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return "MortgageLoan [loanId=" + loanId + ", accountNumber=" + accountNumber + ", principalAmount="
				+ principalAmount + ", currentBalance=" + currentBalance + ", interestRate=" + interestRate
				+ ", status=" + status + "]";
	}
}
