package com.phoenix.finance.service;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.resource.InvestmentResource;
import com.phoenix.finance.resource.PropertyBondResource;

@Stateless
@Dependent
public class InvestorServiceImpl implements InvestorService {

	@Inject
	private InvestmentResource investmentResource;

	@Inject
	private PropertyBondResource bondResource;

	@Override
	public void addInvestor(Investor investor, Investment firstInvestment, PropertyBond propertyBond) {
		if (firstInvestment != null) {
			firstInvestment.setInvestor(investor);
			investmentResource.addInvestment(firstInvestment);
		} else if(propertyBond != null){
			propertyBond.setInvestor(investor);
			bondResource.addPropertyBond(propertyBond);
		}
	}

}
