package com.phoenix.finance.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.resource.InvestmentResource;
import com.phoenix.finance.resource.InvestorResource;

@Stateless
@Dependent
public class InvestmentServiceImpl implements InvestmentService {

	@Inject
	private InvestmentResource investmentResource;

	@Inject
	private InvestorResource investorResource;

	public void addInvestment(Investment investment, int investorNum) {
		Investor investor = investorResource.getInvestor(investorNum);
		investment.setInvestor(investor);
		investmentResource.addInvestment(investment);
	}

	@Override
	public List<Investment> getInvestments(int investorNum) {
		return investmentResource.getInvestments(investorNum);
	}

	@Override
	public Investment getInvestment(int investmentNum) {
		return investmentResource.getInvestment(investmentNum);
	}

}
