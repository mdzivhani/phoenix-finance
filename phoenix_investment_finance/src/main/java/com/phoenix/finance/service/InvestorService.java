package com.phoenix.finance.service;

import javax.ejb.Local;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.investment.Investment;

@Local
public interface InvestorService {

	void addInvestor(Investor investor, Investment firstInvestment, PropertyBond propertyBond);
}
