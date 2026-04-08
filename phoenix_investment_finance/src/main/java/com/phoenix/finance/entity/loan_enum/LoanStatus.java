package com.phoenix.finance.entity.loan_enum;

/**
 * Enum representing the status of a mortgage loan.
 */
public enum LoanStatus {
	ACTIVE("Active - Payments Ongoing"),
	PREPAID("Fully Prepaid"),
	COMPLETED("Fully Completed"),
	DEFAULTED("In Default"),
	SUSPENDED("Suspended");

	private final String displayName;

	LoanStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
