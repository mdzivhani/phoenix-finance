<%@page import="com.phoenix.finance.entity.Investor"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  String message = request.getAttribute("message").toString();
%>
<jsp:include page="includes/header.jsp"></jsp:include>
<jsp:include page="includes/navigation.jsp"></jsp:include>
<div class="container">
	<div class="row">
		<div class="col-md-8">
			<h2 style="color: red"><%=message%></h2>
		</div>
	</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>