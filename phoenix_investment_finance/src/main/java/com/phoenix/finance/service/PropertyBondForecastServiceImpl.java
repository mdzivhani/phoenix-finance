package com.phoenix.finance.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;

import com.phoenix.finance.entity.Event;
import com.phoenix.finance.entity.ForecastItem;
import com.phoenix.finance.entity.Money;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.bond.PropertyBondForecast;

@Stateless
@Dependent
public class PropertyBondForecastServiceImpl implements PropertyBondForecastService {

	@Override
	public void generateForecast(PropertyBondForecast bondForecast) {
		List<ForecastItem> bondForecastItems = new LinkedList<>();
		PropertyBond bond = bondForecast.getBond();
		Event event = null;
		Money openingBalance = null;
		Money interestAmount = null;
		Money balance = null;
		Money contribution = new Money(0);
		Money closingBalance = null;

		if (bond.getBondType().equals("Simple")) {
			openingBalance = bond.getPrincipal();
		} else if (bond.getBondType().equals("Complex")) {
			openingBalance = bondForecast.getComplexOpeningBalance();
		}

		for (int month = 1; month <= bond.getTerm(); month++) {
			if (isNewEvent(bond.getEvents(), month)) {
				event = getNewEvent(bond.getEvents(), month);
			}

			interestAmount = new Money(
					openingBalance.getValue().multiply(getMonthlyRateAsPercentage(event.getInterestRate())));
			balance = openingBalance.add(interestAmount);
			contribution = event.getContribution();
			closingBalance = balance.subtract(contribution);

			if (closingBalance.getValue().doubleValue() < 0) {
				contribution = balance;
				closingBalance = balance.subtract(balance);
			}
			bondForecastItems.add(new ForecastItem(month, openingBalance, event.getInterestRate(), interestAmount,
					contribution, closingBalance));
			openingBalance = closingBalance;
		}
		bondForecast.setBondForecastItems(bondForecastItems);
	}

	public boolean isNewEvent(List<Event> events, int month) {
		for (Event event : events) {
			if (event.getMonth() == month) {
				return true;
			}
		}
		return false;
	}

	public Event getNewEvent(List<Event> events, int month) {
		for (Event event : events) {
			if (event.getMonth() == month) {
				return event;
			}
		}
		throw new RuntimeException("No such event.");
	}

	public BigDecimal getMonthlyRateAsPercentage(BigDecimal interestRate) {
		BigDecimal rateAsPercentage = interestRate.divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
		return rateAsPercentage.divide(BigDecimal.valueOf(12), MathContext.DECIMAL128);
	}
}
