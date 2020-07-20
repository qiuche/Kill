<%--
  Created by IntelliJ IDEA.
  User: steadyjack
  Date: 2019/5/20
  Time: 11:59
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="tag.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>商城高并发抢购-用户登录</title>
    <%@include file="head.jsp" %>
</head>
<body>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h1 align="center">这是用户登录页</h1>

            <form action="${ctx}/login" method="post">
                <table align="center" border="0">
                    <tr>
                        <td>用户名:</td>
                        <td><input type="text" name="userName" value="${userName}"></td>
                    </tr>
                    <br/>
                    <tr>
                        <td>密&nbsp;&nbsp;码:</td>
                        <td><input type="password" name="password"></td>
                    </tr>
                    <tr>
                        <td><input type="submit" value="登录" name="login"></td>
                        <td><input type="reset" value="重置" name="reset"></td>
                    </tr>
                </table>
            </form>

            <h2 align="center">${userName}&nbsp;&nbsp;&nbsp;${errorMsg}</h2>
        </div>
    </div>
</div>
</body>
<script src="${ctx}/static/plugins/jquery.js"></script>
<script src="${ctx}/static/plugins/bootstrap-3.3.0/js/bootstrap.min.js"></script>
<script src="${ctx}/static/plugins/jquery.cookie.min.js"></script>
<script src="${ctx}/static/plugins/jquery.countdown.min.js"></script>
</html>
















