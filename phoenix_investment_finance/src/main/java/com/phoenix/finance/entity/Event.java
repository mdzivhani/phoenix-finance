package com.phoenix.finance.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column
	private int month;

	@Column
	private Money contribution;

	@Column
	private BigDecimal interestRate;

	public Event(int month, Money contribution, BigDecimal interestRate) {
		this.month = month;
		this.contribution = contribution;
		this.interestRate = interestRate;
	}

	public Event() {
	}

	public int getMonth() {
		return month;
	}

	public Money getContribution() {
		return contribution;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setContribution(Money contribution) {
		this.contribution = contribution;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

}
