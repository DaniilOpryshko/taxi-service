<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    <%@include file='/WEB-INF/views/css/table_dark.css' %>
</style>
<html>
<head>
    <title>Add driver to car</title>
</head>
<body>
<%@include file="/WEB-INF/views/header.jsp"%>
<form method="post" id="car" action="${pageContext.request.contextPath}/cars/drivers/add"></form>
<h1 class="table_dark">Add driver to car:</h1>
<table border="1" class="table_dark">
    <tr>
        <th>Car ID</th>
        <th>Driver ID</th>
        <th>Add</th>
    </tr>
    <tr>
        <td>
            <select name="carId" form="car" required>
                <c:forEach items="${cars}" var="car">
                    <option name="carId" value="${car.id}"><c:out value="${car.id}"/>.<c:out value="${car.model}"/></option>
                </c:forEach>
            </select>
        </td>
        <td>
            <select name="driverId" form="car" required>
                <c:forEach items="${drivers}" var="driver">
                    <option name="driverId" value="${driver.id}"><c:out value="${driver.id}"/>.<c:out value="${driver.name}"/></option>
                </c:forEach>
            </select>
        </td>
        <td>
            <input type="submit" name="add" form="car">
        </td>
    </tr>
</table>
</body>
</html>
