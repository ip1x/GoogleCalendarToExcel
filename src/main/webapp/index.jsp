<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta http-equiv="x-ua-compatible" content="ie=edge">

    <title>Redisoft Pilot Project</title>
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.6.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="http://mdbootstrap.com/wp-content/themes/mdbootstrap4/css/compiled.min.css?ver=4.5.3">

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <!-- Material Design Bootstrap -->
    <link href="css/mdb.min.css" rel="stylesheet">
    <!-- Your custom styles (optional) -->
    <link href="css/style.css" rel="stylesheet">
</head>

<body>


    <main>
        <!--Main layout-->
        <div class="container" style="min-height:570px;">

            <!-- First row -->
            <div class="row">
                <div class="col-md-12">
                    <div style="width:50%; margin:30px auto;">
                        <div class="col-md-12">
                             <!--Form with header-->
                                <div class="card carHeight">
                                    <div class="card-block">
                                        <!--Header-->
                                        <div class="form-header purple darken-4">
                                            <h3> Redisoft pilot project</h3>
                                        </div>
                                        <!--Body-->
                                        <form action="/GoogleCalendarToExcel/csv" method="post" enctype="multipart/form-data">
                                        <div class="md-form">

                                            <div class="file_input_div">
                                                <div class="file_input">
                                                    <label class="image_input_button mdl-button mdl-js-button mdl-button--fab mdl-button--mini-fab mdl-js-ripple-effect mdl-button--colored">
         <i class="fa fa-chrome bgBox" aria-hidden="true" title="Choose CSV file only"></i> Choose File
        <input id="file_input_file" class="none" type="file" name="file" />
      </label>
                                                </div>
                                                <div style="color: #FF0000;">${errorMessage}</div>
                                                <div id="file_input_text_div" class="mdl-textfield mdl-js-textfield textfield-demo">
                                                    <input class="file_input_text mdl-textfield__input" type="text" disabled readonly id="file_input_text" />
                                                    <label class="mdl-textfield__label" for="file_input_text"></label>
                                                </div>
                                            </div>

                                        </div>

                                        <span class="col-md-6"><button type="submit" class="btn btn-info btn-lg">SUBMIT</button></span>
                                        <span class="col-md-6" style="text-align:right;"><input type="button" class="btn btn-info btn-lg" onclick="window.close()" value="CLOSE"/></span>
										</form>

                                    </div>

                                </div>
                                <!--/Form with header-->
                           
                        </div>
<div class="col-md-12">&nbsp;</div>
                       
						<div class="col-md-12">
                            <div class="card" style="box-shadow:none">
                                <!--Card content-->
                                <div class="card-block">
                                    <!--Title-->
                                    <h4 class="card-title">Steps how to use</h4>
                                    <!--Text-->
                                    <ol>
                                        <li>Click "Browse" button to choose the input CSV file</li>
                                        <li>Click "Submit" to upload this file" </li>
                                        <li>The output file will be downloaded</li>
                                        <li>Click the file to open it</li>
                                    </ol>
                                </div>
                                <!--/.Card content-->
                            </div>
                            <!--/.Card-->
                        </div>
						

                    </div>

                </div>


            </div>
            <!--/.Main layout-->
    </main>

    <!--Footer-->
    <footer class="page-footer center-on-small-only">
        <!--Footer Links-->

        <!--/.Footer Links-->
        <!--Copyright-->
        <div class="footer-copyright">
            <div class="container-fluid" style="font-size:12px; text-align:right">
                Developed by Damco Team
            </div>
        </div>
        <!--/.Copyright-->
    </footer>
    <!--/.Footer-->

    <!-- SCRIPTS -->

    <!-- Custom Js -->
    <script type="text/javascript" src="js/custom.js"></script>



</body>

</html>