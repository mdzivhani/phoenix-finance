package com.phoenix.finance.web;

import static org.junit.Assert.*;

import org.junit.Test;

import com.phoenix.finance.entity.investment.InvestmentFund;

public class ForecastControllerImplTest {

	private InvestmentForecastController forecast = new InvestmentForecastController();

	@Test
	public void testGetFund() {
		assertEquals(InvestmentFund.ALLAN_GRAY, forecast.getFund("Allan Gray"));
		assertEquals(InvestmentFund.CORONATION, forecast.getFund("Coronation"));
		assertEquals(InvestmentFund.INVESTEC, forecast.getFund("Investec"));
	}

}
