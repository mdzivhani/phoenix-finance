<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>
<div class="container">
	<div class="row text-center">
		<h3>Enter your Investor Number</h3>
	</div>
	<div class="row">
		<div class="col-md-offset-2 col-md-10">
			<form class="form-inline" action="investment/get">
				<div class="form-group">
					type="text" class="form-control" id="investorNum" <label
						for="investorNum" class="sr-only">Investor Number</label> <input
						name="investorNum" placeholder="e.g. 123" required>
				</div>
				<button type="submit" class="btn btn-default">Submit</button>
			</form>
		</div>
	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>