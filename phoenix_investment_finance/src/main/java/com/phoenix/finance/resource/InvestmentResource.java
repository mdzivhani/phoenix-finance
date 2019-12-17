package com.phoenix.finance.resource;

import java.util.List;

import com.phoenix.finance.entity.investment.Investment;

public interface InvestmentResource {

	void addInvestment(Investment investment);
	
	List<Investment> getInvestments(int investorNum);
	
	Investment getInvestment(int investmentNum);

}
