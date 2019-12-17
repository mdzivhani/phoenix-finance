<nav class="navbar navbar-default nopadding nomargin">
	<div class="container-fluid">
		<div class="navbar-header">
			<a class="navbar-brand" href="<%=request.getContextPath()%>/index.jsp">Phoenix Finance</a>
		</div>
		<ul  class="nav navbar-nav">
   			<li>
      			<a href="<%=request.getContextPath()%>/finance/enterInvestmentDetails.jsp">
      				Forecast Investment
      			</a>
   			</li>
  			<li>
      			<a href="<%=request.getContextPath()%>/finance/enterInvestorNumber.jsp">
      				View Investments
      			</a>
   			</li>
   			<li>
      			<a href="<%=request.getContextPath()%>/finance/enterBondDetails.jsp">
     			Forecast Property Bond
      			</a>
   			</li>
   			<li><a href="<%=request.getContextPath()%>/finance/enterClientNumber.jsp">
      			View Property Bonds
      			</a>
  			</li>
		</ul>
	</div>
</nav>