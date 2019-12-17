<%@page import="com.phoenix.finance.entity.bond.BondFund"%>

<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>
<div class="container">
	<div class="row text-center">
		<h3>Enter Property Bond Details</h3>
	</div>
	<br>
	<div class="row">
		<div class="col-md-4 col-md-offset-4">
			<form action="bondForecast/forecast">
				<div class="form-group">
					<label for="bondType">Property Bond Type</label> <select
						class="form-control" id="bondType" name="bondType">
						<option>Simple</option>
						<option>Complex</option>
					</select>
				</div>
				<div class="form-group">
					<label for="principal">Principal</label>
					<div class="input-group">
						<div class="input-group-addon">R</div>
						<input type="number" class="form-control" id="principal"
							name="principal" placeholder="e.g. 1000000" min="1" step="any"
							required>
						<div class="input-group-addon">.00</div>
					</div>
				</div>
				<div class="form-group">
					<label for="interestRate">Interest</label>
					<div class="input-group">
						<input type="number" class="form-control" id="interestRate"
							name="interestRate" placeholder="e.g. 12" min="1" max="100"
							step="any" required>
						<div class="input-group-addon">%</div>
					</div>
				</div>
				<div class="form-group">
					<label for="term">Term</label> <input type="number"
						class="form-control" id="term" name="term" placeholder="e.g. 240"
						min="1" step="1" required>
				</div>
				<div class="form-group">
					<label for="bondFund">Bond Provider</label> <select
						class="form-control" id="bondFund" name="bondFund">
						<%
						  for (BondFund bankFund : BondFund.values()) {
						%>
						<option><%=bankFund.getBank()%></option>
						<%
						  }
						%>
					</select>
				</div>
				<button type="submit" value="submit" class="btn btn-default">Forecast</button>
			</form>
		</div>
	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>