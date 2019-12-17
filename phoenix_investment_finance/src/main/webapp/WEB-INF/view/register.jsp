<%@page import="com.phoenix.finance.entity.bond.PropertyBond"%>
<%@page import="com.phoenix.finance.entity.bond.PropertyBondForecast"%>
<%@page import="com.phoenix.finance.util.MoneyFormatter"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="com.phoenix.finance.entity.investment.Investment"%>
<%@ page
	import="com.phoenix.finance.entity.investment.InvestmentForecast"%>
<%@ page import="com.phoenix.finance.entity.client_enum.*"%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>

<%
  InvestmentForecast investmentForecast = (InvestmentForecast) request.getSession().getAttribute("forecast");
  PropertyBondForecast propertyBondForecast = (PropertyBondForecast) request.getSession()
      .getAttribute("bondForecast");

  Investment investment = null;
  PropertyBond propertyBond = null;

  if (investmentForecast != null) {
		investment = investmentForecast.getInvestment();
  }
  else if (propertyBondForecast != null) {
		propertyBond = propertyBondForecast.getBond();
  }
%>
<div class="container">
	<div class="row">
		<h3>Client Registration</h3>
	</div>
	<br>
	<div class="row">
		<div class="col-md-5">
			<form method="get" action="investor/add">
				<div class="form-group">
					<label for="title">Title:</label> <select class="form-control"
						id="title" name="title">
						<%
						  for (Title title : Title.values()) {
						%>
						<option><%=title.getTitle()%></option>
						<%
						  }
						%>
					</select>
				</div>
				<div class="form-group">
					<label for="firstname">First Name:</label> <input type="text"
						class="form-control" id="firstname" name="firstname"
						placeholder="first name">
				</div>
				<div class="form-group">
					<label for="lastname">Last Name:</label> <input type="text"
						class="form-control" id="lastname" name="lastname"
						placeholder="last name">
				</div>
				<div class="form-group">
					<label for="gender">Gender:</label> <select class="form-control"
						id="gender" name="gender">
						<%
						  for (Gender gender : Gender.values()) {
						%>
						<option><%=gender.getGender()%></option>
						<%
						  }
						%>
					</select>
				</div>
				<div class="form-group">
					<label for="dob">Date Of Birth:</label> <input type="date"
						min="1900-01-01" class="form-control" id="dob" name="dob"
						placeholder="Click to select your Date of birth">
				</div>
				<div class="form-group">
					<label for="cellphone">Cellphone Number:</label> <input type="text"
						class="form-control" id="cellphone" name="cellphone"
						placeholder="e.g. 0813219876">
				</div>
				<div class="form-group">
					<label for="email">E-mail Number:</label> <input type="text"
						class="form-control" id="email" name="email"
						placeholder=".e.g. user@domain.co.za">
				</div>
				<div class="form-group">
					<label for="employer">Employer:</label> <input type="text"
						class="form-control" id="employer" name="employer"
						placeholder=".e.g. Psybergate">
				</div>
				<div class="form-group">
					<label for="occupation">Occupation:</label> <input type="text"
						class="form-control" id="occupation" name="occupation"
						placeholder=".e.g. Dev Intern">
				</div>
				<div class="form-group">
					<button type="submit" value="enter" class="btn btn-default">Register</button>
					<button type="reset" class="btn btn-default">Reset</button>
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
						<td class="money"><b>Interest Rate</b></td>
						<td class="money"><%=investment.getInterestRate()%>%</td>
					</tr>
					<tr>
						<td class="money"><b>Term </b></td>
						<td class="money"><%=investment.getTerm()%> months</td>
					</tr>
				</tbody>
			</table>
			<h4>Events</h4>
			<table class="table table-bordered table-striped">
				<thead>
					<tr>
						<th style="width: 20%">Month</th>
						<th style="width: 40%">Contribution (R)</th>
						<th style="width: 40%">Interest (%)</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						
					</tr>
				</tbody>
			</table>
			<%
			  }
			  else if (propertyBondForecast != null) {
			%>
			<h4>Property Bond Profile</h4>
			<table class="table table-bordered table-striped">
				<tbody>
					<tr>
						<td class="money"><b>Principle Amount</b></td>
						<td class="money">R<%=MoneyFormatter.format(propertyBond.getPrincipal())%></td>
					</tr>
					<tr>
						<td class="money"><b>Bond Type</b></td>
						<td class="money"><%=propertyBond.getBondType()%></td>
					</tr>
					<tr>
						<td class="money"><b>Bond Fund</b></td>
						<td class="money"><%=propertyBond.getBondFund().getBank()%></td>
					</tr>
					<tr>
						<td class="money"><b>Interest Rate</b></td>
						<td class="money"><%=propertyBond.getAnnualInterestRate()%>%</td>
					</tr>
					<tr>
						<td class="money"><b>Term</b></td>
						<td class="money"><%=propertyBond.getTerm()%> months</td>
					</tr>
					<tr>
						<td class="money"><b>Monthly Payment</b></td>
						<td class="money">R<%=propertyBond.getMonthlyPayment()%></td>
					</tr>
				</tbody>
			</table>
			<%
			  }
			%>
		</div>

	</div>
	<div class="row">
		<h4>Property Bond Profile</h4>
	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>
