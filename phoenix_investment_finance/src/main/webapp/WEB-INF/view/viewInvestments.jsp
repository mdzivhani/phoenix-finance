<%@page import="com.phoenix.finance.entity.investment.Investment"%>
<%@page import="java.util.List"%>
<%@page import="com.phoenix.finance.util.MoneyFormatter"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>

<div class="container">
	<div class="row">
		<h3>Investments</h3>
	</div>
	<br>
	<div class="row">
		<div class="col-md-10">
			<table class="table table table-bordered table-striped">
				<thead class="thead-default">
					<tr>
						<th class="money">Investment</th>
						<th class="money">Monthly Contribution (R)</th>
						<th class="money">Interest Rate (%)</th>
						<th class="money">Term (Months)</th>
						<th class="money">Fund Manager</th>
						<th class="money">View</th>
					</tr>
				</thead>
				<tbody>
					<%
					  @SuppressWarnings("unchecked")
					  List<Investment> investments = (List<Investment>) request.getAttribute("investments");
					  int counter = 1;
					  for (Investment investment : investments) {
					%>
					<tr>
						<td class="money"><%=counter%></td>
						<td class="money"><%=MoneyFormatter.format(investment.getContribution())%></td>
						<td class="money"><%=investment.getInterestRate()%></td>
						<td class="money"><%=investment.getTerm()%></td>
						<td class="money"><%=investment.getFund()%></td>
						<td><a class="btn btn-default" style="width: 100%"
							href="../investment/getAndForecast?investmentNum=<%=investment.getInvestmentNum()%>">Details</a></td>
					</tr>
					<%
					  counter++;
					  }
					%>
				</tbody>
			</table>

		</div>
	</div>
</div>

<jsp:include page="includes/footer.jsp"></jsp:include>