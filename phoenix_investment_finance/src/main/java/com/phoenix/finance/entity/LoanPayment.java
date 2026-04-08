package com.phoenix.finance.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

/**
 * Represents a single payment made on a mortgage loan.
 * Tracks payment date, amount, principal/interest breakdown, and outstanding balance after payment.
 */
@Entity
@SequenceGenerator(name = "loan_payment_seq", sequenceName = "loan_payment_id_seq", allocationSize = 1)
public class LoanPayment implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_payment_seq")
	private Long paymentId;

	@ManyToOne
	@JoinColumn(name = "loan_id", nullable = false)
	private MortgageLoan mortgageLoan;

	@Column(nullable = false)
	private Date paymentDate;

	@Column(nullable = false)
	private BigDecimal paymentAmount;

	@Column(nullable = false)
	private BigDecimal principalPaid;

	@Column(nullable = false)
	private BigDecimal interestPaid;

	@Column(nullable = false)
	private BigDecimal balanceAfterPayment;

	@Column
	private String paymentMethod;

	@Column
	private String notes;

	// Constructors
	public LoanPayment() {
	}

	public LoanPayment(MortgageLoan mortgageLoan, Date paymentDate, BigDecimal paymentAmount,
			BigDecimal principalPaid, BigDecimal interestPaid, BigDecimal balanceAfterPayment) {
		this.mortgageLoan = mortgageLoan;
		this.paymentDate = paymentDate;
		this.paymentAmount = paymentAmount;
		this.principalPaid = principalPaid;
		this.interestPaid = interestPaid;
		this.balanceAfterPayment = balanceAfterPayment;
	}

	// Getters and Setters
	public Long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}

	public MortgageLoan getMortgageLoan() {
		return mortgageLoan;
	}

	public void setMortgageLoan(MortgageLoan mortgageLoan) {
		this.mortgageLoan = mortgageLoan;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	public BigDecimal getPrincipalPaid() {
		return principalPaid;
	}

	public void setPrincipalPaid(BigDecimal principalPaid) {
		this.principalPaid = principalPaid;
	}

	public BigDecimal getInterestPaid() {
		return interestPaid;
	}

	public void setInterestPaid(BigDecimal interestPaid) {
		this.interestPaid = interestPaid;
	}

	public BigDecimal getBalanceAfterPayment() {
		return balanceAfterPayment;
	}

	public void setBalanceAfterPayment(BigDecimal balanceAfterPayment) {
		this.balanceAfterPayment = balanceAfterPayment;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return "LoanPayment [paymentId=" + paymentId + ", paymentDate=" + paymentDate + ", paymentAmount="
				+ paymentAmount + ", principalPaid=" + principalPaid + ", interestPaid=" + interestPaid + "]";
	}
}
