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
    <title>商城高并发抢购-抢购结果页-异步MQ限流</title>
    <%@include file="head.jsp" %>
</head>
<body>
<input id="killId" value="${killId}" type="hidden"/>
<input id="userId" value="${userId}" type="hidden"/>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h2 id="waitResult">等待抢购结果...............................</h2>
            <h4 align="center" id="executeResult"></h4>
        </div>
    </div>
</div>
</body>
<script src="${ctx}/static/plugins/jquery.js"></script>
<script src="${ctx}/static/plugins/bootstrap-3.3.0/js/bootstrap.min.js"></script>
<script src="${ctx}/static/plugins/jquery.cookie.min.js"></script>
<script src="${ctx}/static/plugins/jquery.countdown.min.js"></script>
<script type="text/javascript">

    $(function () {
        //等待一定的时间再查询显示结果-给后端赢得足够的时间
        setTimeout(showResult,5000);
    });

    function showResult() {
        var killId=$("#killId").val();
        var userId=$("#userId").val();

        $.ajax({
            type: "GET",
            url: "${ctx}/kill/execute/mq/result?killId="+killId+"&userId="+userId,
            success: function(res){
                if (res.code==0) {
                    $("#executeResult").html(res.data.executeResult);
                    $("#waitResult").html("");
                }else{
                    $("#executeResult").html(res.msg);
                }
            },
            error: function (message) {
                alert("提交数据失败！");
                return;
            }
        });
    }
</script>
</html>
















