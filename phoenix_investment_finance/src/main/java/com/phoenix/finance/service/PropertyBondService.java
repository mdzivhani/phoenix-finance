package com.phoenix.finance.service;

import java.util.List;

import com.phoenix.finance.entity.bond.PropertyBond;;

public interface PropertyBondService {

	void addPropertyBond(PropertyBond bond, int clientNum);

	List<PropertyBond> getPropertyBonds(int clientNum);

	PropertyBond getPropertyBond(int propertyBondNum);
}
