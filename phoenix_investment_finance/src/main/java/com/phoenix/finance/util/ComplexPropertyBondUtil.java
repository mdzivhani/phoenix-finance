package com.phoenix.finance.util;

import java.math.BigDecimal;
import java.math.MathContext;

import com.phoenix.finance.entity.Money;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.bond.PropertyBondForecast;

public class ComplexPropertyBondUtil {

	public static Money calculateComplicatedMonthlyPayment(PropertyBondForecast propertyBondForecast) {
		return new Money(getMonthlyPaymentPercentage(propertyBondForecast.getBond())
				.multiply(propertyBondForecast.getComplexOpeningBalance().getValue()));
	}

	private static BigDecimal getMonthlyPaymentPercentage(PropertyBond propertyBond) {
		BigDecimal monthlyBondRate = propertyBond.getMonthlyInterestRate();
		BigDecimal base = BigDecimal.ONE.add(monthlyBondRate);
		base = BigDecimal.ONE.divide(base, MathContext.DECIMAL128);
		base = base.pow(propertyBond.getTerm(), MathContext.DECIMAL128);
		base = BigDecimal.ONE.subtract(base, MathContext.DECIMAL128);
		monthlyBondRate = monthlyBondRate.divide(base, MathContext.DECIMAL128);
		return monthlyBondRate;
	}

}
