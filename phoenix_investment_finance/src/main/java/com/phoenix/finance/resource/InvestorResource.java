package com.phoenix.finance.resource;

import com.phoenix.finance.entity.Investor;

public interface InvestorResource {

	void addInvestor(Investor investor);
	
	Investor getInvestor(int investorNum);
}
