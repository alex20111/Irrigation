<%@ taglib prefix="s" uri="/struts-tags" %>

<!-- START Head -->
<jsp:include page="/jsp/header.jsp" />

</head>
<!-- END Head -->
<body>
	<div id="wrapper">
		<!-- Navigation -->
        <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
<!-- START Top Nav Menu -->
<jsp:include page="/jsp/topMenu.jsp" />
<!-- END Top Nav Menu -->

<!-- START Side Nav Menu -->
<jsp:include page="/jsp/sideMenu.jsp" />
<!-- END Side Nav Menu -->
		</nav>
		
		<div id="page-wrapper"> 
		<!-- MAIN CONTENT START HERER-->
		
			<%-- Header --%>
			<div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Software update</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />			
			
			<%-- main form  --%>
			<s:form action="updateSoftware" theme="simple" method="POST" enctype="multipart/form-data" cssClass="submit-once"> 	
			<s:hidden name="page"/>	
			<div class="row">
                <div class="col-lg-4">                	
                	<div class="panel panel-primary">
                        <div class="panel-heading">
                            <h4>Update</h4>
                        </div>
                        <div class="panel-body">
                        	<div class="form-group">
                        		<label>New WAR file:</label> 
       							<s:file name="serverFile" cssClass="form-control-file"/>								
				  			 </div> 
				  			 <div class="form-group">
				  			 	<s:submit type="button" name="newWarFileBtn" value="Update Server"  cssClass="btn btn-primary btn-sm" id="updServerId" onclick="buttonSpinner('updServerId');"/>
				  			 </div>
                        </div>                       
                    </div>             
                </div>
			</div>
			<div class="row">
                <div class="col-lg-4">                	
                	<div class="panel panel-primary">
                        <div class="panel-heading">
                            <h4>Update Library files (JARs)</h4>
                        </div>
                        <div class="panel-body">
                        	<div class="form-group">
                        		<label>New JAR file(s):</label> 
       							<s:file name="libraryFiles" cssClass="form-control-file"/>
       							<s:file name="libraryFiles" cssClass="form-control-file"/>	
       							<s:file name="libraryFiles" cssClass="form-control-file"/>									
				  			 </div> 
				  			 <div class="form-group">
				  			 	<s:submit type="button" name="newJarsFileBtn" value="Update Library"  cssClass="btn btn-primary btn-sm" id="updJarsId" onclick="buttonSpinner('updJarsId');"/>
				  			 </div>
                        </div>                       
                    </div>             
                </div>
			</div>    	    	
      
      		</s:form>		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>	
	 </div>
    <!-- /#wrapper -->
<!-- START Top Nav Menu -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->





