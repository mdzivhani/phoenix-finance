package com.phoenix.finance.entity;

import java.math.BigDecimal;

public class ForecastItem {

	private int month;

	private Money openingBalance;

	private BigDecimal interestRate;

	private Money interestAmount;

	private Money contribution;

	private Money closingBalance;

	public ForecastItem(int month, Money openingBalance, BigDecimal interestRate, Money interestAmount,
			Money contribution, Money closingBalance) {
		this.month = month;
		this.openingBalance = openingBalance;
		this.interestRate = interestRate;
		this.interestAmount = interestAmount;
		this.contribution = contribution;
		this.closingBalance = closingBalance;
	}

	public int getMonth() {
		return month;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public Money getOpeningBalance() {
		return openingBalance;
	}

	public Money getContribution() {
		return contribution;
	}

	public Money getInterestAmount() {
		return interestAmount;
	}

	public Money getClosingBalance() {
		return closingBalance;
	}
}
