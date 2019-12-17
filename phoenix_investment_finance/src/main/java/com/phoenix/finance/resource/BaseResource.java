package com.phoenix.finance.resource;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.phoenix.finance.qualifier.Resource;

@Resource
@RequestScoped
public class BaseResource {

	@PersistenceContext(unitName="phoenixPersistence")
 	protected EntityManager entityManager;
	
}
