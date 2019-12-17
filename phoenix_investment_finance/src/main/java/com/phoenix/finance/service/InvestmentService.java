package com.phoenix.finance.service;

import java.util.List;

import javax.ejb.Local;

import com.phoenix.finance.entity.investment.Investment;

@Local
public interface InvestmentService {

	void addInvestment(Investment investment, int investorNum);

	List<Investment> getInvestments(int investorNum);

	Investment getInvestment(int investmentNum);
}
