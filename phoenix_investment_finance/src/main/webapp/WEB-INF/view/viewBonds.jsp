<%@page import="java.util.List"%>
<%@page import="com.phoenix.finance.entity.bond.PropertyBond"%>
<%@page import="com.phoenix.finance.entity.investment.Investment"%>
<%@page import="com.phoenix.finance.util.MoneyFormatter"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>

<div class="container">
	<div class="row">
		<h3>Property Bonds</h3>
	</div>
	<br>
	<div class="row">
		<div class="col-md-10">
			<table class="table table table-bordered table-striped">
				<thead class="thead-default">
					<tr>
						<th class="money">Property Bond</th>
						<th class="money">Principal Amount (R)</th>
						<th class="money">Interest Rate (%)</th>
						<th class="money">Term (Months)</th>
						<th class="money">Monthly Payment (R)</th>
						<th class="money">View</th>
					</tr>
				</thead>
				<tbody>
					<%
					  @SuppressWarnings("unchecked")
					  List<PropertyBond> propertyBonds = (List<PropertyBond>) request.getAttribute("propertyBonds");
					  int counter = 1;
					  for (PropertyBond propertyBond : propertyBonds) {
					%>
					<tr>
						<td class="money"><%=counter%></td>
						<td class="money"><%=MoneyFormatter.format(propertyBond.getPrincipal())%></td>
						<td class="money"><%=propertyBond.getAnnualInterestRate()%></td>
						<td class="money"><%=propertyBond.getTerm()%></td>
						<td class="money"><%=MoneyFormatter.format(propertyBond.getMonthlyPayment())%></td>
						<td><a class="btn btn-default" style="width: 100%"
							href="../bond/getPropertyBond?propertyBondNum=<%=propertyBond.getPropertyBondNum()%>">Details</a></td>
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