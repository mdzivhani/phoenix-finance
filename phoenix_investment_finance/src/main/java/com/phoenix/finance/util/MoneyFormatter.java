package com.phoenix.finance.util;

import com.phoenix.finance.entity.Money;

public class MoneyFormatter {

	public static String format(Money money) {
		return String.format("%,.2f", money.getValue());
	}

}
