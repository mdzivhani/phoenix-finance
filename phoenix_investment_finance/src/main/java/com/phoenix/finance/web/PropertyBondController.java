package com.phoenix.finance.web;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.bond.PropertyBondForecast;
import com.phoenix.finance.qualifier.Controller;
import com.phoenix.finance.service.PropertyBondForecastService;
import com.phoenix.finance.service.PropertyBondService;

@Controller
@ApplicationScoped
public class PropertyBondController implements com.phoenix.finance.web.Controller {

	@Inject
	private PropertyBondService bondService;

	@Inject
	private PropertyBondForecastService propertyBondForecastService;

	public void addPropertyBond(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PropertyBond bond = getModel(req);
		int clientNum = getClientNum(req);
		bondService.addPropertyBond(bond, clientNum);
		req.setAttribute("message", "Succcessfully added bond");
		req.getServletContext().getRequestDispatcher("/finance/success.jsp").forward(req, resp);
	}

	public void getPropertyBonds(HttpServletRequest req, HttpServletResponse resp) {
		try {
			int clientNum = Integer.parseInt(req.getParameter("clientNum"));
			List<PropertyBond> propertyBonds = bondService.getPropertyBonds(clientNum);
			req.setAttribute("propertyBonds", propertyBonds);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher("/finance/viewBonds.jsp");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void getPropertyBond(HttpServletRequest req, HttpServletResponse resp) {
		try {
			int propertyBondNum = Integer.parseInt(req.getParameter("propertyBondNum"));
			PropertyBond propertyBond = bondService.getPropertyBond(propertyBondNum);
			PropertyBondForecast forecast = new PropertyBondForecast(propertyBond);
			propertyBondForecastService.generateForecast(forecast);
			req.setAttribute("bondForecast", forecast);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher("/finance/bondDetails.jsp");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PropertyBond getModel(HttpServletRequest req) {
		PropertyBondForecast propertyBondForecast = (PropertyBondForecast) req.getSession().getAttribute("bondForecast");
		return propertyBondForecast.getBond();
	}
	// Money principleAmount = new Money(new
	// BigDecimal(req.getParameter("bond")));
	// BigDecimal interestRate = new
	// BigDecimal(req.getParameter("interestRate"));
	// int period = Integer.parseInt(req.getParameter("period"));
	// return new PropertyBond(principleAmount, interestRate, period);

	private int getClientNum(HttpServletRequest req) {
		int investorNum = Integer.parseInt(req.getParameter("clientNum"));
		return investorNum;
	}
}
