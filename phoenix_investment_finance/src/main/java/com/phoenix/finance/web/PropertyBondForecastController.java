package com.phoenix.finance.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.entity.Event;
import com.phoenix.finance.entity.Money;
import com.phoenix.finance.entity.bond.BondFund;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.bond.PropertyBondForecast;
import com.phoenix.finance.qualifier.Controller;
import com.phoenix.finance.service.PropertyBondForecastService;
import com.phoenix.finance.util.ComplexPropertyBondUtil;

@Controller
@ApplicationScoped
public class PropertyBondForecastController implements com.phoenix.finance.web.Controller {

	@Inject
	private PropertyBondForecastService bondForecastService;

	public void forecastPropertyBond(HttpServletRequest req, HttpServletResponse resp) {
		try {
			PropertyBondForecast bondForecast = getModel(req);
			List<Event> events = getEvents(req, bondForecast);
			PropertyBond bond = bondForecast.getBond();
			bond.setEvents(events);

			bondForecastService.generateForecast(bondForecast);
			req.setAttribute("bondForecast", bondForecast);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher(Controller.JSP_PATH + "propertyBondForecast.jsp");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PropertyBondForecast getModel(HttpServletRequest req) {
		if (isFirstTime(req)) {
			Money principal = new Money(new BigDecimal(req.getParameter("principal")));
			BigDecimal interestRate = new BigDecimal(req.getParameter("interestRate"));
			int term = Integer.parseInt(req.getParameter("term"));
			BondFund bondFund = getBondFund(req.getParameter("bondFund"));
			String bondType = req.getParameter("bondType");
			PropertyBond bond = new PropertyBond(principal, interestRate, term, bondFund, bondType);
			PropertyBondForecast propertyBondForecast = new PropertyBondForecast(bond);

			if (bondType.equals("Complex")) {
				bond.setMonthlyPayment(ComplexPropertyBondUtil.calculateComplicatedMonthlyPayment(propertyBondForecast));
			}
			return propertyBondForecast;
		} else {
			String parameter = req.getParameter("basePrincipal");
			Money principal = new Money(new BigDecimal(parameter));
			BigDecimal interestRate = new BigDecimal(req.getParameter("baseInterestRate"));
			int term = Integer.parseInt(req.getParameter("baseTerm"));
			BondFund bondFund = getBondFund(req.getParameter("baseBondFund"));
			String bondType = req.getParameter("baseBondType");
			PropertyBond bond = new PropertyBond(principal, interestRate, term, bondFund, bondType);
			PropertyBondForecast propertyBondForecast = new PropertyBondForecast(bond);
			
			if (bondType.equals("Complex")) {
				bond.setMonthlyPayment(ComplexPropertyBondUtil.calculateComplicatedMonthlyPayment(propertyBondForecast));
			}
			return propertyBondForecast;
		}
	}

	private BondFund getBondFund(String parameter) {
		for (BondFund bondFund : BondFund.values()) {
			if (bondFund.getBank().equals(parameter)) {
				return bondFund;
			}
		}
		return null;
	}

	private boolean isFirstTime(HttpServletRequest req) {
		String baseContribution = req.getParameter("baseContribution");
		String baseInterestRate = req.getParameter("baseInterestRate");
		String baseTerm = req.getParameter("baseTerm");
		if (baseContribution == null && baseInterestRate == null && baseTerm == null) {
			return true;
		}
		return false;
	}

	private List<Event> getEvents(HttpServletRequest req, PropertyBondForecast propertyBondForecast) {
		PropertyBond bond = propertyBondForecast.getBond();
		List<Event> events = new ArrayList<>();
		int totalPossibleEvents = 5;

		events.add(new Event(1, bond.getMonthlyPayment(), bond.getAnnualInterestRate()));

		for (int i = 1; i <= totalPossibleEvents; i++) {
			int month = getMonth(req.getParameter("month" + i));
			Money contribution = getContribution(req.getParameter("contribution" + i));
			BigDecimal interestRate = getInterestRate(req.getParameter("interestRate" + i));

			if (!isEventEmpty(month, contribution, interestRate)) {
				Event event = new Event(month, contribution, interestRate);
				cleanEvent(event, events);
				events.add(event);
			}
		}
		return events;
	}

	private int getMonth(String monthParameter) {
		if (monthParameter == null) {
			return 0;
		}
		if (monthParameter.isEmpty()) {
			return 0;
		} else {
			return Integer.parseInt(monthParameter);
		}
	}

	private Money getContribution(String contributionParameter) {
		if (contributionParameter == null) {
			return null;
		}
		if (contributionParameter.isEmpty()) {
			return null;
		} else {
			return new Money(new BigDecimal(contributionParameter));
		}
	}

	private BigDecimal getInterestRate(String interestRateParameter) {
		if (interestRateParameter == null) {
			return null;
		}
		if (interestRateParameter.isEmpty()) {
			return null;
		} else {
			return new BigDecimal(interestRateParameter);
		}
	}

	private boolean isEventEmpty(int month, Money contribution, BigDecimal interestRate) {
		if (month == 0) {
			return true;
		}
		if (contribution == null && interestRate == null) {
			return true;
		}
		return false;
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

}
