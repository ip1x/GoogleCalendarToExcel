<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">



<html >
<head>
   <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
  <style>
.clearfix{*zoom:1;}.clearfix:before,.clearfix:after{display:table;content:"";line-height:0;}
.clearfix:after{clear:both;}
.hide-text{font:0/0 a;color:transparent;text-shadow:none;background-color:transparent;border:0;}
.inputAlign{    margin: auto;    width: 190px;}
.alignCenter{text-align:center}


</style>
</head>
<body>

<div class="container" style="margin-top:100px">
  <div class="jumbotron alignCenter" style="background-color:7C7B20">
    
  <h2>Gcalendar2XLS</h2>
  <p><form action="/GoogleCalendarToExcel/csv" method="post" enctype="multipart/form-data">
        <div style="color: #FF0000;">${errorMessage}</div>
		<input id="data" type="file" name="file" size="50" class="inputAlign" /> <br /> 
		<input type="submit" class="btn btn-success"/>
	</form>
  </p>
<p>   </p>
</div>

</body>
</html>