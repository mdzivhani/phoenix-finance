package com.phoenix.finance.entity.bond;

public enum BondFund {
	UNSPECIFIED("Unspecified"), ABSA("Absa (Pty) Ltd"), CAPITEC("Capitec Bank"), FNB(
			"First National Bank"), STANDARD_BANK("Standard Bank"), NEDBANK("Nedbank");

	private String bank;

	private BondFund(String bank) {
		this.bank = bank;
	}

	public String getBank() {
		return bank;
	}
}
