package com.phoenix.finance.resource;

import com.phoenix.finance.entity.Investor;

public class InvestorResourceImpl extends BaseResource implements InvestorResource {

	@Override
	public void addInvestor(Investor investor) {
		entityManager.persist(investor);
	}

	@Override
	public Investor getInvestor(int investorNum) {
		Investor investor = entityManager.find(Investor.class, investorNum);
		return investor;
	}

}
