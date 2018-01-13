
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
                    <h1 class="page-header">Logs View</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
			
			<s:form name="form1" action="showLog" theme="simple" onsubmit="document.body.style.cursor='wait';"> 
				<s:hidden name="selectedTab"/>
				    
				<div class="row">
               	 <div class="col-sm-12">   
				    
				 
					  <ul class="nav nav-pills">
					    <li  <s:if test="selectedTab == 0"> class="active" </s:if> ><a data-toggle="pill" href="#info">Info Logs</a></li>
					    <li  <s:if test="selectedTab == 1"> class="active" </s:if> ><a data-toggle="pill" href="#errors">Error Logs</a></li>
		
					  </ul>
					  
					  <div class="tab-content">
					    <div id="info" class="tab-pane fade <s:if test="selectedTab == 0"> in active </s:if> ">
					    	<div class="form-group " style="margin-top:10px">
						    	<s:select name="selInfoLogs" list="#session.infoLogFiles" listKey="getPath()" listValue="getName()" cssClass="form-control"/>
						    </div>
						    <div class="form-group">
						    	<s:submit type="button" value="Show" name = "btnInfoShow" cssClass="btn btn-primary btn-sm" id="showInfoLogsId" onclick="buttonSpinner('showInfoLogsId');"/>
						    </div>
			      			<div class="form-group">
						    	<s:textarea name="infoLogText" readonly="true" cols="75" rows="50" theme="simple" cssClass="form-control"></s:textarea>
						    </div>
					    </div>
					    <div id="errors" class="tab-pane fade  <s:if test="selectedTab == 1"> in active </s:if> ">
					    	<div class="form-group" style="margin-top:10px">
					    		<s:select name="selErrorLogs" list="#session.errorLogFiles" listKey="getPath()" listValue="getName()" cssClass="form-control"/>
					    	</div>
					    	<div class="form-group"> 			    		
		      					<s:submit type="button" value="Show" name="btnErrorShow" cssClass="btn btn-primary btn-sm" id="showErrorLogsId" onclick="buttonSpinner('showErrorLogsId');"/> 
		      				</div>
		      				<div class="form-group">
				    			<s:textarea name="errorLogText" readonly="true" cols="75" rows="50" theme="simple" cssClass="form-control"></s:textarea> 
				    		</div>				      
					    </div>			  
					  </div>
				
				</div>
				</div>
			</s:form>
			
		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>
			
		
	 </div>
	 
	 <script>
	 </script>
	 
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->

