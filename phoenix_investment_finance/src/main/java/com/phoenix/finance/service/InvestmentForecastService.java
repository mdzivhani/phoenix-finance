package com.phoenix.finance.service;

import javax.ejb.Local;

import com.phoenix.finance.entity.Event;
import com.phoenix.finance.entity.investment.InvestmentForecast;

@Local
public interface InvestmentForecastService {

	void generateForecast(InvestmentForecast forecast);

	void addEvent(Event event, InvestmentForecast forecast);

}
