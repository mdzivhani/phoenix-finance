package com.phoenix.finance.entity;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.persistence.Embeddable;

@Embeddable
public class Money {

	private BigDecimal value;

	public Money(double value) {
		this.value = new BigDecimal(value);
		this.value.setScale(2, BigDecimal.ROUND_UP);
	}

	public Money() {
	}

	public Money(BigDecimal value) {
		this.value = value;
		this.value.setScale(2, BigDecimal.ROUND_UP);
	}

	public Money add(Money money) {
		return new Money(value.add(money.getValue()));
	}

	public Money subtract(Money money) {
		return new Money(value.subtract(money.getValue()));
	}

	public Money multiply(Money money) {
		return new Money(value.multiply(money.getValue()));
	}

	public Money divide(Money money) {
		return new Money(value.divide(money.getValue(), MathContext.DECIMAL128));
	}

	public BigDecimal getValue() {
		return value.setScale(2, BigDecimal.ROUND_UP);
	}

	@Override
	public String toString() {
		return value.setScale(2, BigDecimal.ROUND_UP).toString();
	}

}
