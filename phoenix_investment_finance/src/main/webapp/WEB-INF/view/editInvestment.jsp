<%@page import="com.phoenix.finance.entity.investment.Investment"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>

<div class="container-fluid">
	<div class="row">
		<div class="col-md-6">
			<h4>Investment Details</h4>
			<br>
			<h4>Investment Number: 32398</h4>
		</div>
	</div>
	<br>
	<div class="row">
		<div class="col-md-2">
			<h5 class="investmentLabel">Balance (R)</h5>
		</div>
		<div class="col-md-2">
			<h5 class="investmentLabel">Rate (%)</h5>
		</div>
		<div class="col-md-2">
			<h5 class="investmentLabel">Contribution</h5>
		</div>
		<div class="col-md-2">
			<h5 class="investmentLabel">Start date</h5>
		</div>
		<div class="col-md-2">
			<h5 class="investmentLabel">End date</h5>
		</div>
		<div class="col-md-2">
			<h5 class="investmentLabel">Projected value</h5>
		</div>
	</div>
	<div class="row">
		<div class="col-md-2">12 446.98</div>
		<div class="col-md-2">8</div>
		<div class="col-md-2">1000</div>
		<div class="col-md-2">January 2017</div>
		<div class="col-md-2">December 2047</div>
		<div class="col-md-2">2 534 567.07</div>
	</div>
	<button type="submit" class="btn btn-default">Update</button>
</div>
<br>

<jsp:include page="includes/footer.jsp"></jsp:include>
