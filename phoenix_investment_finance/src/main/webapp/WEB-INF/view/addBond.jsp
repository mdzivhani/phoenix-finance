<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>
<div class="container-fluid">
	<div class="row">
		<div class="col-md-8">
			<form class="form-horizontal" action="bond/add">
				<div class="form-group">
					<label for="bond" class="col-sm-2 control-label">Bond (R)</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="bond" name="bond"
							placeholder="E.g. 1000000">
					</div>
				</div>
				<div class="form-group">
					<label for="interestRate" class="col-sm-2 control-label">Interest
						Rate (%)</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="interestRate"
							name="interestRate" placeholder="E.g. 8%">
					</div>
				</div>
				<div class="form-group">
					<label for="period" class="col-sm-2 control-label">Period
						(months)</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="period" name="period"
							placeholder="E.g. 240">
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<button type="submit" value="submit" class="btn btn-default">Submit</button>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>