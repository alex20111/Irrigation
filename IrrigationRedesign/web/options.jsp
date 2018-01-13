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
                    <h1 class="page-header">Options</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
			
			<%-- main form  --%>
			<s:form action="saveOptions" theme="simple" method="POST"  cssClass="submit-once"> 
				<s:hidden name="page"/>	
				<s:token/>
			
				<div class="row">
	                <div class="col-lg-4">
	
						<div class="panel panel-primary">
	                        <div class="panel-heading">
	                            <h3>Server Options</h3>
	                        </div>
	                        <div class="panel-body">
	                        	<div class="form-group">
	                            	<s:submit name="restartBtn" value="Restart Server" cssClass="btn btn-primary btn-sm"/> 
					  			  	<s:submit name="stopBtn" value="Stop Server" cssClass="btn btn-primary btn-sm"/>
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





