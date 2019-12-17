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
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.entity.investment.InvestmentForecast;

@Stateless
@Dependent
public class InvestmentForecastServiceImpl implements InvestmentForecastService {

	@Override
	public void generateForecast(InvestmentForecast forecast) {
		List<ForecastItem> forecastItems = new LinkedList<>();
		Investment investment = forecast.getInvestment();
		Event event = null;
		Money openingBalance = new Money(0);
		Money contribution = null;
		BigDecimal interestRate = null;
		Money interestEarned = null;
		Money closingBalance = null;

		for (int month = 1; month <= investment.getTerm(); month++) {
			if (isNewEvent(investment.getEvents(), month)) {
				event = getNewEvent(investment.getEvents(), month);
			}
			interestRate = event.getInterestRate();
			interestEarned = calcuteInterestGained(openingBalance, event);
			contribution = event.getContribution();
			closingBalance = forecastClosingBalance(openingBalance, event);
			ForecastItem forecastItem = new ForecastItem(month, openingBalance, interestRate, interestEarned, contribution,
					closingBalance);
			forecastItems.add(forecastItem);
			openingBalance = closingBalance;
		}
		forecast.setForecastItems(forecastItems);
	}

	@Override
	public void addEvent(Event event, InvestmentForecast forecast) {
		cleanEvent(event, forecast.getInvestment().getEvents());
		forecast.getInvestment().addEvent(event);
	}

	public Money forecastClosingBalance(Money openingBalance, Event event) {
		BigDecimal monthlyRateAsPercentage = getMonthlyRateAsPercentage(event.getInterestRate());
		Money compoundingMoney = openingBalance.add(event.getContribution());
		Money interestGained = new Money(monthlyRateAsPercentage.multiply(compoundingMoney.getValue()));
		Money closingBalance = openingBalance.add(event.getContribution()).add(interestGained);
		return closingBalance;
	}

	public Money calcuteInterestGained(Money monthStartBalance, Event event) {
		BigDecimal monthlyRateAsPercentage = getMonthlyRateAsPercentage(event.getInterestRate());
		Money balanceAndMonthContribution = monthStartBalance.add(event.getContribution());
		Money interestGained = new Money(monthlyRateAsPercentage.multiply(balanceAndMonthContribution.getValue()));
		return interestGained;
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

	private void cleanEvent(Event event, List<Event> events) {
		Event previousEvent = getLastEvent(event, events);
		if (event.getContribution() == null) {
			event.setContribution(previousEvent.getContribution());
		}
		if (event.getInterestRate() == null) {
			BigDecimal interestRate = previousEvent.getInterestRate();
			event.setInterestRate(interestRate);
		}
	}

	private Event getLastEvent(Event event, List<Event> events) {
		return events.get(events.size() - 1);
	}

	public BigDecimal getMonthlyRateAsPercentage(BigDecimal interestRate) {
		BigDecimal rateAsPercentage = interestRate.divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
		return rateAsPercentage.divide(BigDecimal.valueOf(12), MathContext.DECIMAL128);
	}

}
