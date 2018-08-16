<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="https://v3.bootcss.com/favicon.ico">

    <title>Lucene测试</title>

    <!-- Bootstrap core CSS -->
    <link href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">

</head>

<body>

<div class="container">
    <h2 class="form-signin-heading">Lucene测试</h2>
    <hr>
    <form class="form-inline" action="${pageContext.request.contextPath}/product" method="post" >
        <div class="form-group">
            <label for="keyword">关键字</label>
            <input type="text" class="form-control" id="keyword" name="key" placeholder="电脑 手机">
        </div>
        <div class="form-group">
            <label for="num">最大结果数</label>
            <input type="text" class="form-control" id="num" name="number" placeholder="推荐数值为0-100">
        </div>
        <button type="submit" class="btn btn-primary">提交查询</button>
    </form>
</div>

</body>

</html>
