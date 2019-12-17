package com.phoenix.finance.web;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.entity.investment.InvestmentForecast;
import com.phoenix.finance.qualifier.Controller;
import com.phoenix.finance.service.InvestmentForecastService;
import com.phoenix.finance.service.InvestmentService;

@Controller
@ApplicationScoped
public class InvestmentController implements com.phoenix.finance.web.Controller {

	@Inject
	private InvestmentService investmentService;

	@Inject
	private InvestmentForecastService forecastService;

	public void addInvestment(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Investment investment = getModel(req);
		int investorID = getInvestorID(req);
		investmentService.addInvestment(investment, investorID);

		req.setAttribute("message", "Successfully added Investment <br><br> Investment Number: "
				+ investment.getInvestmentNum() + "<br> Term: " + investment.getTerm());
		req.getServletContext().getRequestDispatcher("/finance/success.jsp").forward(req, resp);
	}

	public void getInvestments(HttpServletRequest req, HttpServletResponse resp) {
		try {
			int investorNum = Integer.parseInt(req.getParameter("investorNum"));
			List<Investment> investments = investmentService.getInvestments(investorNum);
			req.setAttribute("investments", investments);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher("/finance/viewInvestments.jsp");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void getInvestmentAndForecast(HttpServletRequest req, HttpServletResponse resp) {
		try {
			int investmentNum = Integer.parseInt(req.getParameter("investmentNum"));
			Investment investment = investmentService.getInvestment(investmentNum);
			InvestmentForecast forecast = new InvestmentForecast(investment);
			forecastService.generateForecast(forecast);
			req.setAttribute("forecast", forecast);

			RequestDispatcher requestDispatcher = req.getRequestDispatcher("/finance/investmentDetails.jsp");
			requestDispatcher.forward(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	public Investment getModel(HttpServletRequest req) {
		InvestmentForecast forecast = (InvestmentForecast) req.getSession().getAttribute("forecast");
		Investment investment = forecast.getInvestment();
		return investment;
	}

	private int getInvestorID(HttpServletRequest req) {
		int investorNum = Integer.parseInt(req.getParameter("investorNum"));
		return investorNum;
	}

}
