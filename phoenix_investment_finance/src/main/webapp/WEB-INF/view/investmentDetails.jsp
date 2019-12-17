<%@page import="com.phoenix.finance.util.MoneyFormatter"%>
<%@page import="com.phoenix.finance.web.Controller"%>
<%@page contentType="text/html; charset=ISO-8859-1"
	import="java.util.List, java.math.BigDecimal"%>
<%@page import="com.phoenix.finance.entity.investment.*"%>
<%@page import="com.phoenix.finance.entity.*"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  InvestmentForecast forecast = (InvestmentForecast) request.getAttribute("forecast");
  List<ForecastItem> monthEndBalances = forecast.getForecastItems();
%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>
<div class="container">
	<div class="row text-center">
		<h3>Investment Details</h3>
	</div>
	<div class="row">
		<div class="col-md-4">
			<div class="form-group table-responsive nomargin nopadding">
				<h4 class="card-title" style="text-align: center">Events</h4>
				<br>
				<table class="table table-bordered table-striped">
					<thead class="thead">
						<tr>
							<th class="money">Month</th>
							<th class="money">Contribution</th>
							<th class="money">Rate</th>
						</tr>
					</thead>
					<tbody>
						<%
						  for (Event event : forecast.getInvestment().getEvents()) {
								if (event.getMonth() == 1) {
								  continue;
								}
						%>
						<tr>
							<td class="money"><%=event.getMonth()%></td>
							<td class="money"><%=MoneyFormatter.format(event.getContribution())%></td>
							<td class="money"><%=event.getInterestRate()%></td>
						</tr>
						<%
						  }
						%>
					</tbody>
				</table>
			</div>
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
							<td class="money"><b>Monthly Contribution</b></td>
							<td class="money">R<%=MoneyFormatter.format(forecast.getInvestment().getContribution())%></td>
						</tr>
						<tr>
							<td class="money"><b>Interest Rate </b></td>
							<td class="money"><%=forecast.getInvestment().getInterestRate()%>%</td>
						</tr>
						<tr>
							<td class="money"><b>Term </b></td>
							<td class="money"><%=forecast.getInvestment().getTerm()%>
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
							<td class="money"><b>Contributions Total </b></td>
							<td class="money">R<%=MoneyFormatter.format(forecast.totalContributions())%></td>
						</tr>
						<tr>
							<td class="money"><b>Interest Earned Total</b></td>
							<td class="money">R<%=MoneyFormatter.format(forecast.totalInterestEarned())%></td>
						</tr>
						<tr>
							<td class="money"><b>Future Value</b></td>
							<td class="money">R<%=MoneyFormatter.format(forecast.futureValue())%></td>
						</tr>
						<tr>
							<td class="money"><b>Fund Manager</b></td>
							<td class="money"><%=forecast.getInvestment().getFund()%></td>
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
						<th class="money">Interest Rate (%)</th>
						<th class="money">Interest Amount (R)</th>
						<th class="money">Contribution (R)</th>
						<th class="money">Closing Balance (R)</th>
					</tr>
				</thead>
				<tbody>
					<%
					  for (ForecastItem forecastItem : monthEndBalances) {
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