package com.phoenix.finance.entity.client_enum;

public enum Title {
	 MR("Mr"), MRS("Mrs"), MISS("Miss"),PROF("Prof"), DR("Dr");

	private String title;

	private Title(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
