package com.phoenix.finance.entity.investment;

import java.util.List;

import com.phoenix.finance.entity.ForecastItem;
import com.phoenix.finance.entity.Money;

public class InvestmentForecast {

	private Investment investment;

	private List<ForecastItem> forecastItems;

	public InvestmentForecast(Investment investment) {
		this.investment = investment;
	}

	public Investment getInvestment() {
		return investment;
	}

	public List<ForecastItem> getForecastItems() {
		return forecastItems;
	}

	public void setForecastItems(List<ForecastItem> forecastItems) {
		this.forecastItems = forecastItems;
	}

	public Money totalContributions() {
		Money totalContribution = new Money(0);
		for (ForecastItem forecastItem : forecastItems) {
			totalContribution = totalContribution.add(forecastItem.getContribution());
		}
		return totalContribution;
	}

	public Money totalInterestEarned() {
		Money totalInterestEarned = new Money(0);
		for (ForecastItem forecastItem : forecastItems) {
			totalInterestEarned = totalInterestEarned.add(forecastItem.getInterestAmount());
		}
		return totalInterestEarned;
	}

	public Money futureValue() {
		int lastForecastItem = forecastItems.size() - 1;
		return forecastItems.get(lastForecastItem).getClosingBalance();
	}

}
