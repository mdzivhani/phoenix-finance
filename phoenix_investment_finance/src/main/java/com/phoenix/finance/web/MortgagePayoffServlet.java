package com.phoenix.finance.web;

import java.io.IOException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.phoenix.finance.qualifier.Controller;

/**
 * Controller for handling Mortgage Payoff Accelerator requests.
 * Forwards requests to the JSF view.
 */
@Controller
@ApplicationScoped
public class MortgagePayoffServlet {

	public void mortgagePayoff(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/view/mortgagePayoffAccelerator.jsp");
		dispatcher.forward(request, response);
	}
}
