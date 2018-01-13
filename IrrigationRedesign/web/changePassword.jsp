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
                    <h1 class="page-header">Change Password</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
		<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />

			<%-- main form --%>
		
			<s:form theme="simple" action="savepassword" cssClass="submit-once">
				<s:token/> 
				
				<div class="row">
                	<div class="col-lg-4">
                	
	                	<%-- New Password --%>
	                	<div class="form-group">	                			
	                		<label for="newPass1">New Password:</label>
	                		<s:if test="fieldErrors['newpass'] != null" >
								<section class="alert alert-danger">
								   	<p>
								   		<s:fielderror escape="false">
											<s:param>newpass</s:param>
										</s:fielderror>
									</p> 
								</section>
						  	</s:if>	
	                		<s:password name="newPassword" id="newPass1" cssClass="form-control"/>
	                	</div>
	                	
	                	<%-- Confirm New Password --%>
	                	<div class="form-group">	                			
	                		<label for="confnewPass1">Confirm New Password:</label>
	                		<s:if test="fieldErrors['confpass'] != null" >
								<section class="alert alert-danger">
								   	<p>
								   		<s:fielderror escape="false">
											<s:param>confpass</s:param>
										</s:fielderror>
									</p> 
								</section>
						  	</s:if>	
	                		<s:password name="confirmPassword" id="confnewPass1" cssClass="form-control"/>
	                	</div>               	
	            
	                	<s:submit type="button"  value="Save" cssClass="btn btn-primary" id="chPassId" onclick="buttonSpinner('chPassId');"/>
                	
                	</div>
                </div>				
			</s:form>
		<!-- MAIN CONTENT STOP HERE-->	
		</div>	
	 </div>
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->

