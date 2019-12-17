package com.phoenix.finance.entity;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Test;

import com.phoenix.finance.entity.Investor;

public class InvestorTest {

  private EntityManagerFactory entityFactory;

  @Before
  public void before() {
	entityFactory = Persistence.createEntityManagerFactory("phoenixPersistence");
  }

  @Test
  public void testPersistAndRetrieve() {
	int investorNumber = generateInvestorNumber();
	Investor investor = new Investor(investorNumber, "first", "last");
	EntityManager entityManager = entityFactory.createEntityManager();
	entityManager.getTransaction().begin();
	entityManager.persist(investor);
	entityManager.getTransaction().commit();
	Investor persistedInvestor = retrieveInvestor(investorNumber);
	assertEquals("first", persistedInvestor.getFirstName());
	assertEquals("last", persistedInvestor.getLastName());
  }

  private Investor retrieveInvestor(int investorNumber) {
	EntityManager entityManager = null;
	try {
	  entityManager = entityFactory.createEntityManager();
	  entityManager.getTransaction().begin();
	  Investor investor = entityManager.find(Investor.class, investorNumber);
	  entityManager.getTransaction().commit();

	  return investor;
	} finally {
	  entityManager.close();
	}
  }

  private int generateInvestorNumber() {
	return (int) Math.random() * 100000;
  }
}
