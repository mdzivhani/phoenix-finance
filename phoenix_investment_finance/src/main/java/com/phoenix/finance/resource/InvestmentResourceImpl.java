package com.phoenix.finance.resource;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.persistence.Query;

import com.phoenix.finance.entity.investment.Investment;
import com.phoenix.finance.qualifier.Resource;

@Resource
@RequestScoped
public class InvestmentResourceImpl extends BaseResource implements InvestmentResource {

	public void addInvestment(Investment investment) {
		entityManager.persist(investment);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Investment> getInvestments(int investorNum) {
		Query query = entityManager
				.createQuery("SELECT inv FROM Investment inv WHERE inv.investor.investorNum = :investorNum");
		query.setParameter("investorNum", investorNum);
		List<Investment> investments = query.getResultList();
		return investments;
	}

	@Override
	public Investment getInvestment(int investmentNum) {
		Query query = entityManager
				.createQuery("SELECT inv FROM Investment inv WHERE inv.investmentNum = :investmentNum");
		query.setParameter("investmentNum", investmentNum);
		Investment Investment = (Investment) query.getSingleResult();
		return Investment;
	}

}
