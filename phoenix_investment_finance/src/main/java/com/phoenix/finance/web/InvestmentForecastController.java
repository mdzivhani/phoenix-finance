package com.phoenix.finance.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.entity.Event;
import com.phoenix.finance.entity.Money;
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.entity.investment.InvestmentForecast;
import com.phoenix.finance.entity.investment.InvestmentFund;
import com.phoenix.finance.qualifier.Controller;
import com.phoenix.finance.service.InvestmentForecastService;

@Controller
@ApplicationScoped
public class InvestmentForecastController implements com.phoenix.finance.web.Controller {

	@Inject
	private InvestmentForecastService forecastService;

	public void generateForecast(HttpServletRequest req, HttpServletResponse resp) {
		try {
			InvestmentForecast forecast = getModel(req);
			forecastService.generateForecast(forecast);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher(Controller.JSP_PATH + "investmentForecast.jsp");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addEvent(HttpServletRequest req, HttpServletResponse resp) {
		try {
			InvestmentForecast forecast = getModel(req);
			Event event = getEvent(req);
			forecastService.addEvent(event, forecast);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher("../forecast/generate");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public InvestmentForecast getModel(HttpServletRequest req) {
		InvestmentForecast forecast = (InvestmentForecast) req.getSession().getAttribute("forecast");
		if (forecast == null) {
			Money amount = new Money(new BigDecimal(req.getParameter("amount")));
			BigDecimal interestRate = new BigDecimal(req.getParameter("interestRate"));
			int term = Integer.parseInt(req.getParameter("term"));
			String fundName = req.getParameter("fund");
			InvestmentFund fund = getFund(fundName);
			Investment investment = new Investment(amount, interestRate, term, fund);
			investment.addEvent(new Event(1, investment.getContribution(), investment.getInterestRate()));
			forecast = new InvestmentForecast(investment);
			req.getSession().setAttribute("forecast", forecast);
		}
		return forecast;
	}

	private Event getEvent(HttpServletRequest req) {
		int month = Integer.parseInt(req.getParameter("month"));
		Money contribution = getContribution(req.getParameter("contribution"));
		BigDecimal interestRate = getInterestRate(req.getParameter("interestRate"));
		return new Event(month, contribution, interestRate);
	}

	public InvestmentFund getFund(String fundName) {
		Map<String, InvestmentFund> funds = new HashMap<>();
		funds.put("Allan Gray", InvestmentFund.ALLAN_GRAY);
		funds.put("Coronation", InvestmentFund.CORONATION);
		funds.put("Investec", InvestmentFund.INVESTEC);
		return funds.get(fundName);
	}

	private Money getContribution(String contributionParameter) {
		if (contributionParameter.equals("")) {
			return null;
		}
		return new Money(new BigDecimal(contributionParameter));
	}

	private BigDecimal getInterestRate(String interestRateParameter) {
		if (interestRateParameter.equals("")) {
			return null;
		}
		return new BigDecimal(interestRateParameter);
	}

}
