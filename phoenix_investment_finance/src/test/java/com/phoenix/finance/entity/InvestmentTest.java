package com.phoenix.finance.entity;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Test;

import com.phoenix.finance.entity.investment.Investment;

public class InvestmentTest {

	private EntityManagerFactory entityFactory = Persistence.createEntityManagerFactory("phoenixPersistence");

	@Before
	public void before() {
	}

	@Test
	public void testAddInvestmentWorks() {
		Investment inv = retrieveInvestment();
		int amount = 1000;
		double rate = 8.0;
		int term = 24;
		assertEquals(amount, inv.getContribution());
		assertEquals(rate, inv.getInterestRate());
		assertEquals(term, inv.getTerm());

	}

	private Investment retrieveInvestment() {
		EntityManager entityManager = null;
		try {
			entityManager = entityFactory.createEntityManager();
			entityManager.getTransaction().begin();
			Investment inv = entityManager.find(Investment.class, 100);
			entityManager.getTransaction().commit();
			return inv;
		} finally {
			entityManager.close();
		}
	}

}
