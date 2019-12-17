package com.phoenix.finance.service;

import javax.enterprise.context.Dependent;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.resource.InvestorResource;
import com.phoenix.finance.resource.PropertyBondResource;

@Stateless
@Dependent
public class PropertyBondServiceImpl implements PropertyBondService {

	@Inject
	private PropertyBondResource bondResource;

	@Inject
	private InvestorResource investorResource;

	@Override
	public void addPropertyBond(PropertyBond bond, int clientNum) {
		bond.setInvestor(investorResource.getInvestor(clientNum));
		bondResource.addPropertyBond(bond);
	}

	@Override
	public List<PropertyBond> getPropertyBonds(int clientNum) {
		return bondResource.getPropertyBonds(clientNum);
	}

	@Override
	public PropertyBond getPropertyBond(int propertyBondNum) {
		return bondResource.getPropertyBond(propertyBondNum);
	}
}
