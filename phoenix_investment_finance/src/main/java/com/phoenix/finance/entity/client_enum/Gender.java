package com.phoenix.finance.entity.client_enum;

public enum Gender {
	MALE("Male"), FEMALE("Female"), OTHER("Other");

	private String gender;

	private Gender(String gender) {
		this.gender = gender;
	}

	public String getGender() {
		return gender;
	}
}
