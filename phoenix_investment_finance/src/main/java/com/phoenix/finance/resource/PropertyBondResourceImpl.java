package com.phoenix.finance.resource;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.Query;

import com.phoenix.finance.entity.bond.PropertyBond;
import com.phoenix.finance.qualifier.Resource;

@Resource
@RequestScoped
public class PropertyBondResourceImpl extends BaseResource implements PropertyBondResource {

	@Override
	public void addPropertyBond(PropertyBond bond) {
		entityManager.persist(bond);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PropertyBond> getPropertyBonds(int clientNum) {
		Query query = entityManager.createQuery(
				"SELECT propertyBond FROM PropertyBond propertyBond WHERE propertyBond.investor.investorNum = :clientNum");
		query.setParameter("clientNum", clientNum);
		return (List<PropertyBond>) query.getResultList();
	}

	@Override
	public PropertyBond getPropertyBond(int propertyBondNum) {
		return entityManager.find(PropertyBond.class, propertyBondNum);
	}

}
