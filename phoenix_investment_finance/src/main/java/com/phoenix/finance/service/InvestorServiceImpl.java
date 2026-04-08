package com.phoenix.finance.service;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.phoenix.finance.entity.Investor;
import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.resource.InvestmentResource;
import com.phoenix.finance.resource.PropertyBondResource;

@Stateless
@Dependent
public class InvestorServiceImpl implements InvestorService {

	@PersistenceContext(unitName = "phoenixPersistence")
	private EntityManager entityManager;

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

	@Override
	public Investor getInvestorByNumber(int investorNumber) {
		try {
			return entityManager.createQuery(
				"SELECT i FROM Investor i WHERE i.investorNumber = :number", 
				Investor.class)
				.setParameter("number", investorNumber)
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
