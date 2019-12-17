package com.phoenix.finance.entity;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.phoenix.finance.entity.client_enum.Gender;
import com.phoenix.finance.entity.client_enum.Title;

@Entity
public class Investor {

  @Id
  @Column(nullable = false, unique = true)
  private int investorNum;

  @Enumerated(EnumType.STRING)
  private Title title;

  @Column
  private String firstName;

  @Column
  private String lastName;

  @Column
  private Date dateOfBirth;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column
  private String email;

  @Column
  private String cellphone;

  @Column
  private String employer;

  @Column
  private String occupation;

  public Investor(int investorNum, String firstName, String lastName) {
	this.firstName = firstName;
	this.lastName = lastName;
	this.investorNum = investorNum;
  }

  protected Investor() {
  }

  public int getInvestorNum() {
	return investorNum;
  }

  public void setInvestorNum(int investorNum) {
	this.investorNum = investorNum;
  }

  public Title getTitle() {
	return title;
  }

  public void setTitle(Title title) {
	this.title = title;
  }

  public String getFirstName() {
	return firstName;
  }

  public void setFirstName(String firstName) {
	this.firstName = firstName;
  }

  public String getLastName() {
	return lastName;
  }

  public void setLastName(String lastName) {
	this.lastName = lastName;
  }

  public Date getDateOfBirth() {
	return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
	this.dateOfBirth = dateOfBirth;
  }

  public Gender getGender() {
	return gender;
  }

  public void setGender(Gender gender) {
	this.gender = gender;
  }

  public String getEmail() {
	return email;
  }

  public void setEmail(String email) {
	this.email = email;
  }

  public String getCellphone() {
	return cellphone;
  }

  public void setCellphone(String cellphone) {
	this.cellphone = cellphone;
  }

  public String getEmployer() {
	return employer;
  }

  public void setEmployer(String employer) {
	this.employer = employer;
  }

  public String getOccupation() {
	return occupation;
  }

  public void setOccupation(String occupation) {
	this.occupation = occupation;
  }
}
