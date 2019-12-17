package com.phoenix.finance.web;

import java.sql.Date;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.bond.PropertyBondForecast;
import com.phoenix.finance.entity.client_enum.Gender;
import com.phoenix.finance.entity.client_enum.Title;
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.entity.investment.InvestmentForecast;
import com.phoenix.finance.qualifier.Controller;
import com.phoenix.finance.service.InvestorService;

@Controller
@ApplicationScoped
public class InvestorController implements com.phoenix.finance.web.Controller {

	@Inject
	private InvestorService investorService;

	public void addInvestor(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Investor investor = getModel(req);
		Investment firstInvestment = getFirstInvestment(req);
		PropertyBond firstPropertyBond = getFirstPropertyBond(req);
		investorService.addInvestor(investor, firstInvestment, firstPropertyBond);

		String message = investor.getTitle().getTitle() + ". " + investor.getFirstName() + " " + investor.getLastName()
				+ "<br><br>";
		message += "Your Account and First Product have been successfully created.<br>";
		message += "Investor Number : <b>" + investor.getInvestorNum() + "</b>";
		req.setAttribute("message", message);
		req.getServletContext().getRequestDispatcher("/finance/success.jsp").forward(req, resp);
	}

	@Override
	public Investor getModel(HttpServletRequest req) {
		int investorNum = generateInvestorNumber();
		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		Investor investor = new Investor(investorNum, firstname, lastname);

		String dob = req.getParameter("dob");
		Date dateOfBirth = produceDate(dob);
		investor.setDateOfBirth(dateOfBirth);

		Title title = getTitleOption(req.getParameter("title"));
		investor.setTitle(title);

		String cellphone = req.getParameter("cellphone");
		investor.setCellphone(cellphone);

		String email = req.getParameter("email");
		investor.setEmail(email);

		String employer = req.getParameter("employer");
		investor.setEmployer(employer);

		String occupation = req.getParameter("occupation");
		investor.setOccupation(occupation);

		Gender gender = getGenderOption(req.getParameter("gender"));
		investor.setGender(gender);

		return investor;
	}

	private Gender getGenderOption(String parameter) {
		for (Gender gender : Gender.values()) {
			if (gender.getGender().equals(parameter)) {
				return gender;
			}
		}
		return null;
	}

	private Title getTitleOption(String parameter) {
		for (Title title : Title.values()) {
			if (title.getTitle().equals(parameter)) {
				return title;
			}
		}
		return null;
	}

	private Investment getFirstInvestment(HttpServletRequest req) {
		InvestmentForecast forecast = (InvestmentForecast) req.getSession().getAttribute("forecast");
		if (forecast == null) {
			return null;
		}
		return forecast.getInvestment();
	}

	public PropertyBond getFirstPropertyBond(HttpServletRequest req) {
		PropertyBondForecast forecast = (PropertyBondForecast) req.getSession().getAttribute("bondForecast");
		if (forecast == null) {
			return null;
		}
		return forecast.getBond();
	}

	private int generateInvestorNumber() {
		Random random = new Random();
		int rand1 = random.nextInt(1001);
		int rand2 = random.nextInt(999);
		int rand3 = random.nextInt(1001);
		return (rand1 + rand2 + rand3);
	}

	private Date produceDate(String dob) {
		if ((!dob.equals("")) && (dob != null)) {
			String[] dateElements = dob.split("-");
			int year = Integer.valueOf(dateElements[0]);
			int month = Integer.valueOf(dateElements[1]);
			int day = Integer.valueOf(dateElements[2]);
			return Date.valueOf(LocalDate.of(year, month, day));
		} else {
			return null;
		}
	}
}
