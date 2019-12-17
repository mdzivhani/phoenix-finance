package com.phoenix.finance.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.phoenix.finance.entity.Event;
import com.phoenix.finance.entity.ForecastItem;
import com.phoenix.finance.entity.Money;
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.entity.investment.InvestmentForecast;
import com.phoenix.finance.entity.investment.InvestmentFund;

public class ForecastServiceTest {

	private InvestmentForecastService forecastService;

	private InvestmentForecast forecast;

	@Before
	public void before() {
		forecastService = new InvestmentForecastServiceImpl();
		Money contribution = new Money(1000);
		BigDecimal interestRate = BigDecimal.valueOf(8);
		int term = 12;
		Investment investment = new Investment(contribution, interestRate, term, InvestmentFund.ALLAN_GRAY);
		investment.addEvent(new Event(1, contribution, interestRate));
		forecast = new InvestmentForecast(investment);
	}

	@Test
	public void testCalculateClosingBalance() {
		InvestmentForecastServiceImpl forecastServiceImpl = (InvestmentForecastServiceImpl) forecastService;
		Money openingBalance = new Money(11449.92);
		Money expectedClosingBalance = new Money(12532.92);
		Money closingBalance = forecastServiceImpl.forecastClosingBalance(openingBalance,
				forecast.getInvestment().getEvents().get(0));
		assertEquals(expectedClosingBalance.getValue(), closingBalance.getValue());
	}

	@Test
	public void testCalcuteInterestGained() {
		InvestmentForecastServiceImpl forecastServiceImpl = (InvestmentForecastServiceImpl) forecastService;
		Money openingBalance = new Money(11449.92);
		Money interestGained = forecastServiceImpl.calcuteInterestGained(openingBalance,
				forecast.getInvestment().getEvents().get(0));
		Money expectedInterestGained = new Money(82.999);
		assertEquals(expectedInterestGained.getValue(), interestGained.getValue());
	}

	@Test
	public void generateForecast() {
		for (int i = 2; i <= 12; i++) {
			forecastService.addEvent(new Event(i, new Money(i * 1000), null), forecast);
		}

		forecastService.generateForecast(forecast);

		for (ForecastItem forescastItem : forecast.getForecastItems()) {
			System.out.println("month " + forescastItem.getMonth() + ": " + forescastItem.getContribution() + " "
					+ forescastItem.getClosingBalance());
		}
	}

}
