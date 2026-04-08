package com.phoenix.finance.web;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.qualifier.Controller;

/**
 * Controller for handling Mortgage Loan Management requests.
 * Forwards requests to the JSF view.
 */
@Controller
@ApplicationScoped
public class MortgageLoanServlet {

	public void mortgageLoans(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/view/mortgageLoanManagement.jsp");
		dispatcher.forward(request, response);
	}
}
