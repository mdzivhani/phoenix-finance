package com.phoenix.finance.entity;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.phoenix.finance.entity.Money;

public class MoneyTest {

	@Test
	public void testAddWorks() {
		Money money = new Money(0);

		money.add(new Money(1000));
		money.add(new Money(1000));
		assertEquals(BigDecimal.valueOf(2000), money.getValue());
	}
	
	@Test
	public void testSubtractWorks() {
		Money money = new Money(2000);
		
		money.subtract(new Money(1000));
		assertEquals(BigDecimal.valueOf(1000), money.getValue());
	}
}
