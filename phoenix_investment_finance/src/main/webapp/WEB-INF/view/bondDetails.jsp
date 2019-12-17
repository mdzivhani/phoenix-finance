<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.phoenix.finance.entity.bond.PropertyBond"%>
<%@page import="com.phoenix.finance.entity.bond.PropertyBondForecast"%>
<%@page import="com.phoenix.finance.entity.ForecastItem"%>
<%@page import="com.phoenix.finance.entity.Event"%>
<%@page import="com.phoenix.finance.util.MoneyFormatter"%>
<%@page import="java.util.List"%>

<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>

<%
  PropertyBondForecast bondForecast = (PropertyBondForecast) request.getAttribute("bondForecast");
%>
<div class="container">
	<div class="row text-center">
		<h3>Property Bond Details</h3>
	</div>
	<div class="row">
		<div class="col-md-4">
			<form action="../bondForecast/forecast">
				<div class="form-group table-responsive nomargin nopadding">
					<div class="text-center">
						<h4>Events</h4>
					</div>
					<table class="table table-hover">
						<thead>
							<tr>
								<th style="width: 20%">Month</th>
								<th style="width: 40%">Contribution (R)</th>
								<th style="width: 40%">Interest (%)</th>
							</tr>
						</thead>
						<%
						  List<Event> events = bondForecast.getBond().getEvents();
						  for (int event = 1; event <= 5; event++) {
						%>
						<tr>
							<td class="money" style="width: 20%"><input
								style="width: 100%" type="number" name="month<%=event%>" min="1"
								max="<%=bondForecast.getBond().getTerm()%>" step="any"
								<%if (event < events.size()) {%>
								value="<%=events.get(event).getMonth()%>" <%}%> /></td>
							<td class="money" style="width: 40%"><input
								style="width: 100%" type="number" name="contribution<%=event%>"
								min="1" step="any" <%if (event < events.size()) {%>
								value="<%=events.get(event).getContribution()%>" <%}%> /></td>
							<td class="money" style="width: 40%"><input
								style="width: 100%" type="number" name="interestRate<%=event%>"
								min="1" step="any" <%if (event < events.size()) {%>
								value="<%=events.get(event).getInterestRate()%>" <%}%> /></td>
						</tr>
						<%
						  }
						%>
					</table>

					<input type="hidden" name="basePrincipal"
						value="<%=bondForecast.getBond().getPrincipal()%>"> <input
						type="hidden" name="baseInterestRate"
						value="<%=bondForecast.getBond().getAnnualInterestRate()%>">
					<input type="hidden" name="baseTerm"
						value="<%=bondForecast.getBond().getTerm()%>">
				</div>
				<!-- 				<div class="col-md-offset-8 col-md-4"> -->
				<!-- 					<button type="submit" value="submit" class="btn btn-default">Forecast</button> -->
				<!-- 				</div> -->
			</form>
		</div>
		<div class="col-md-8 forecast">
			<div class="text-center">
				<h4>Summary</h4>
			</div>
			<div class="col-md-6">
				<br>
				<table class="table table-bordered table-striped">
					<tbody>
						<tr>
							<td class="money"><b>Principal Amount</b></td>
							<td class="money">R<%=MoneyFormatter.format(bondForecast.getBond().getPrincipal())%></td>
						</tr>
						<tr>
							<td class="money"><b>Interest Rate</b></td>
							<td class="money"><%=bondForecast.getBond().getAnnualInterestRate()%>%</td>
						</tr>
						<tr>
							<td class="money"><b>Term</b></td>
							<td class="money"><%=bondForecast.getBond().getTerm()%>
								months</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="col-md-6">
				<br>
				<table class="table table-bordered table-striped">
					<tbody>
						<tr>
							<td class="money"><b>Bond Registration Fee</b></td>
							<td class="money">R<%=MoneyFormatter.format(bondForecast.getBondRegistrationFee())%></td>
						</tr>
						<tr>
							<td class="money"><b>Legal Fees</b></td>
							<td class="money">R<%=MoneyFormatter.format(bondForecast.getLegalFees())%></td>
						</tr>
						<tr>
							<td class="money"><b>Transfer Duty</b></td>
							<td class="money">R<%=MoneyFormatter.format(bondForecast.getTransferDuty())%></td>
						</tr>
						<tr>
							<td class="money"><b>Total Interest Amount </b></td>
							<td class="money">R<%=MoneyFormatter.format(bondForecast.getInterestAmountTotal())%></td>
						</tr>
						<tr>
							<td class="money"><b>Payments Total</b></td>
							<td class="money">R<%=MoneyFormatter.format(bondForecast.getPaymentsTotal())%></td>
						</tr>
					</tbody>
				</table>
			</div>

			<br> <br>
			<h4 style="text-align: center;">Forecast</h4>
			<br>

			<table class="table table-bordered table-striped">
				<thead>
					<tr>
						<th class="money">Month</th>
						<th class="money">Opening Balance (R)</th>
						<th class="money">Interest Rate(%)</th>
						<th class="money">Interest Amount (R)</th>
						<th class="money">Payment (R)</th>
						<th class="money">Closing Balance (R)</th>
					</tr>
				</thead>
				<tbody>
					<%
					  for (ForecastItem forecastItem : bondForecast.getBondForecastItems()) {
					%>
					<tr>
						<td class="money"><%=forecastItem.getMonth()%></td>
						<td class="money"><%=MoneyFormatter.format(forecastItem.getOpeningBalance())%></td>
						<td class="money"><%=forecastItem.getInterestRate()%></td>
						<td class="money"><%=MoneyFormatter.format(forecastItem.getInterestAmount())%></td>
						<td class="money"><%=MoneyFormatter.format(forecastItem.getContribution())%></td>
						<td class="money"><%=MoneyFormatter.format(forecastItem.getClosingBalance())%></td>
					</tr>
					<%
					  }
					%>
				</tbody>
			</table>
		</div>
	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>