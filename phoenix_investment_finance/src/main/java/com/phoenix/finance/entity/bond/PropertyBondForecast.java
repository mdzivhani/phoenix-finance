package com.phoenix.finance.entity.bond;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.phoenix.finance.entity.ForecastItem;
import com.phoenix.finance.entity.Money;

public class PropertyBondForecast {

  private PropertyBond propertyBond;

  private List<ForecastItem> bondForecastItems;

  public PropertyBondForecast(PropertyBond bond) {
	this.propertyBond = bond;
  }

  public Money getPaymentsTotal() {
	Money paymentsTotal = new Money(0);
	for (ForecastItem forecastItem : bondForecastItems) {
	  paymentsTotal = paymentsTotal.add(forecastItem.getContribution());
	}
	return paymentsTotal;
  }

  public Money getInterestAmountTotal() {
	Money interestAmountTotal = new Money(0);
	for (ForecastItem forecastItem : bondForecastItems) {
	  interestAmountTotal = interestAmountTotal.add(forecastItem.getInterestAmount());
	}
	return interestAmountTotal;
  }

  public Money getBondRegistrationFee() {
	return new Money(5000.0);
  }

  public Money getLegalFees() {
	BigDecimal transferDutyRate = new BigDecimal("0.8").divide(new BigDecimal("100.0"), MathContext.DECIMAL32);
	Money fixedTransferDuty = new Money(15_000);
	Money percentageLegalFee = new Money(transferDutyRate.multiply(propertyBond.getPrincipal().getValue()));

	if (fixedTransferDuty.getValue().compareTo(percentageLegalFee.getValue()) < 0) {
	  return percentageLegalFee;
	}
	else if (fixedTransferDuty.getValue().compareTo(percentageLegalFee.getValue()) == 0) {
	  return fixedTransferDuty;
	}
	else if (fixedTransferDuty.getValue().compareTo(transferDutyRate) > 0) {
	  return fixedTransferDuty;
	}
	return null;
  }

  public Money getTransferDuty() {
	double principalValue = propertyBond.getPrincipal().getValue().doubleValue();
	if (principalValue <= 900_000) {
	  return new Money(BigDecimal.ZERO);
	}
	else if ((principalValue > 900_000) && (principalValue <= 1_250_000)) {
	  Money taxableAmount = propertyBond.getPrincipal().subtract(new Money(900_000));
	  Money calculatedAmount = taxableAmount.multiply(new Money(0.03));
	  System.out.println(calculatedAmount);
	  return calculatedAmount;
	}
	else if ((principalValue > 1_250_000) && (principalValue <= 1_750_000)) {
	  Money taxableAmount = propertyBond.getPrincipal().subtract(new Money(1_250_000));
	  Money taxCalculation = taxableAmount.multiply(new Money(0.06));
	  return taxCalculation.add(new Money(10_500));
	}
	else if ((principalValue > 1_750_000) && (principalValue <= 2_250_000)) {
	  Money taxableAmount = propertyBond.getPrincipal().subtract(new Money(1_750_000));
	  Money taxCalculation = taxableAmount.multiply(new Money(0.08));
	  return taxCalculation.add(new Money(40_500));
	}
	else if ((principalValue > 2_250_000) && (principalValue <= 10_000_000)) {
	  Money taxableAmount = propertyBond.getPrincipal().subtract(new Money(2_250_000));
	  Money taxCalculation = taxableAmount.multiply(new Money(0.11));
	  return taxCalculation.add(new Money(80_500));
	}
	else if (principalValue > 10_000_000) {
	  Money taxableAmount = propertyBond.getPrincipal().subtract(new Money(10_000_000));
	  Money taxCalculation = taxableAmount.multiply(new Money(0.13));
	  return taxCalculation.add(new Money(993_000));
	}
	return null;
  }

  public PropertyBond getBond() {
	return propertyBond;
  }

  public List<ForecastItem> getBondForecastItems() {
	return bondForecastItems;
  }

  public void setBondForecastItems(List<ForecastItem> bondForecastItems) {
	this.bondForecastItems = bondForecastItems;
  }

  public Money getComplexOpeningBalance() {
	Money basicPrincipal = propertyBond.getPrincipal();
	Money complexOpeningPayment = basicPrincipal.add((getLegalFees()).add(getTransferDuty()).add(getBondRegistrationFee()));
	return complexOpeningPayment;
  }
}
