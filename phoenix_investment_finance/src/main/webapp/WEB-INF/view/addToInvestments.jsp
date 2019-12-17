<%@page import="com.phoenix.finance.entity.investment.Investment"%>
<%@page import="com.phoenix.finance.entity.bond.*"%>
<%@page import="com.phoenix.finance.util.MoneyFormatter"%>
<%@page
	import="com.phoenix.finance.entity.investment.InvestmentForecast"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>

<%
  InvestmentForecast investmentForecast = (InvestmentForecast) request.getSession().getAttribute("forecast");
  Investment investment = null;

  PropertyBondForecast propertyBondForecast = (PropertyBondForecast) request.getSession()
      .getAttribute("bondForecast");
  PropertyBond propertyBond = null;
  String controllerUrl = null;
  String userType = null;

  if (investmentForecast != null) {
		investment = investmentForecast.getInvestment();
		controllerUrl = "investment/add";
		userType = "investorNum";
  }
  else {
		propertyBond = propertyBondForecast.getBond();
		controllerUrl = "bond/add";
		userType = "clientNum";
  }
%>
<div class="container">
	<div class="row">
		<h3>Enter your Investor Number</h3>
	</div>
	<div class="row">
		<div class="col-md-3">
			<br>
			<form method="get" action="<%=controllerUrl%>">
				<div class="form-group">
					<label for="<%=userType%>">Investor Number: </label> <input
						type="text" id="<%=userType%>" name="<%=userType%>"
						placeholder="e.g. 10101" />
				</div>
				<div class="form-group">
					<button type="submit" class="btn btn-default">Create
						Product</button>
				</div>
			</form>
		</div>

		<div class="col-md-5">
			<%
			  if (investmentForecast != null) {
			%>
			<h4>Investment Profile</h4>
			<table class="table table-bordered table-striped">
				<tbody>
					<tr>
						<td class="money"><b>Monthly Contribution</b></td>
						<td class="money">R<%=MoneyFormatter.format(investment.getContribution())%></td>
					</tr>
					<tr>
						<td class="money"><b>Interest Rate </b></td>
						<td class="money"><%=investment.getInterestRate()%>%</td>
					</tr>
					<tr>
						<td class="money"><b>Term </b></td>
						<td class="money"><%=investment.getTerm()%> months</td>
					</tr>
				</tbody>
			</table>
			<%
			  }
			  else {
			%>
			<h4>Property Bond Profile</h4>
			<table class="table table-bordered table-striped">
				<tbody>
					<tr>
						<td class="money"><b>Principle Amount </b></td>
						<td class="money">R<%=MoneyFormatter.format(propertyBond.getPrincipal())%></td>
					</tr>
					<tr>
						<td class="money"><b>Interest Rate </b></td>
						<td class="money"><%=propertyBond.getAnnualInterestRate()%>%</td>
					</tr>
					<tr>
						<td class="money"><b>Term </b></td>
						<td class="money"><%=propertyBond.getTerm()%> months</td>
					</tr>
					<tr>
						<td class="money"><b>Monthly Payment </b></td>
						<td class="money">R<%=propertyBond.getMonthlyPayment()%></td>
					</tr>
				</tbody>
			</table>
			<%
			  }
			%>
		</div>

	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>