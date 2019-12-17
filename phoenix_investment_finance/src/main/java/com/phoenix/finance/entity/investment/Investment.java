package com.phoenix.finance.entity.investment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.phoenix.finance.entity.Event;
import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.Money;

@Entity
public class Investment {

	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Id
	@Column(unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int investmentNum;

	@Column
	@Embedded
	@AttributeOverrides(value = { @AttributeOverride(name = "value", column = @Column(name = "contribution")) })
	private Money contribution;

	@Column
	private BigDecimal interestRate;

	@Column
	private int term;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "investmentNum")
	private List<Event> events = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "investorNum")
	private Investor investor;

	@Enumerated(EnumType.STRING)
	private InvestmentFund fund;

	public Investment(Money contribution, BigDecimal interestRate, int term, InvestmentFund fund) {
		this.contribution = contribution;
		this.interestRate = interestRate;
		this.term = term;
		this.fund = fund;
	}

	public Investment() {
	}

	public void addEvent(Event event) {
		events.add(event);
	}

	public int getInvestmentNum() {
		return investmentNum;
	}

	public int getTerm() {
		return term;
	}

	public Money getContribution() {
		return contribution;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInvestmentNum(int investmentNum) {
		this.investmentNum = investmentNum;
	}

	public Investor getInvestor() {
		return investor;
	}

	public void setContribution(Money contribution) {
		this.contribution = contribution;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public List<Event> getEvents() {
		return events;
	}

	public int getId() {
		return id;
	}

	public void setInvestor(Investor investor) {
		this.investor = investor;
	}

	public InvestmentFund getFund() {
		return fund;
	}

}
