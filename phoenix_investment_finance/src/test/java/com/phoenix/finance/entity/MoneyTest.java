package com.phoenix.finance.entity;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.phoenix.finance.entity.Money;

public class MoneyTest {

	@Test
	public void testAddWorks() {
		Money money = new Money(0);

		money = money.add(new Money(1000));
		money = money.add(new Money(1000));
		assertEquals(new BigDecimal("2000.00"), money.getValue());
	}
	
	@Test
	public void testSubtractWorks() {
		Money money = new Money(2000);
		
		money = money.subtract(new Money(1000));
		assertEquals(new BigDecimal("1000.00"), money.getValue());
	}
}
