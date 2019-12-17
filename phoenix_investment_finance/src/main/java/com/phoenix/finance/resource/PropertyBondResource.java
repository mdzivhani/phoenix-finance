package com.phoenix.finance.resource;

import java.util.List;

import com.phoenix.finance.entity.bond.PropertyBond;

public interface PropertyBondResource {
	
	void addPropertyBond(PropertyBond bond);
	
	List<PropertyBond> getPropertyBonds(int investorNum);

	PropertyBond getPropertyBond(int propertyBondNum);

}
