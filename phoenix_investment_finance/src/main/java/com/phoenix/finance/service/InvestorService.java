package com.phoenix.finance.service;

import jakarta.ejb.Local;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.investment.Investment;

@Local
public interface InvestorService {

	void addInvestor(Investor investor, Investment firstInvestment, PropertyBond propertyBond);
	
	/**
	 * Find an investor by their investor number.
	 * @param investorNumber the investor number
	 * @return the Investor if found, null otherwise
	 */
	Investor getInvestorByNumber(int investorNumber);
}
