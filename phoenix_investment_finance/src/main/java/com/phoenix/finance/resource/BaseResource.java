package com.phoenix.finance.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.phoenix.finance.qualifier.Resource;

@Resource
@RequestScoped
public class BaseResource {

	@PersistenceContext(unitName="phoenixPersistence")
 	protected EntityManager entityManager;
	
}
