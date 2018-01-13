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
                    <h1 class="page-header">User Manager</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
			
			<%-- main form  --%>
			<s:form theme="simple" action="saveUser" cssClass="submit-once"> 
						
			<s:token/>
	      	<s:hidden name="userId"/>	
			
			<div class="row">
                <div class="col-lg-4">
                	
                	<%-- User Name --%>
                	<div class="form-group">                		
                		<label for="userName1">User Name:</label>
                		<s:if test="fieldErrors['userName'] != null" >
							<section class="alert alert-danger">
							   	<p>
							   		<s:iterator value="%{fieldErrors['userName']}" >
							   			<s:property />
							   		</s:iterator>						  
								</p> 
							</section>
					  	</s:if>		
                		<s:textfield name="userName" id="userName1" cssClass="form-control"/>
                	</div>
                	<%-- First Name --%>
                	<div class="form-group">                			
                		<label for="firstName1">First Name:</label>
                		<s:if test="fieldErrors['firstName'] != null" >
							<section class="alert alert-danger">
							   	<p>
							   		<s:iterator value="%{fieldErrors['firstName']}" >
							   			<s:property />
							   		</s:iterator>	
							   
								</p> 
							</section>
					  	</s:if>	
                		<s:textfield name="firstName" id="firstName1" cssClass="form-control"/>
                	</div>
                	<%-- Last Name --%>
                	<div class="form-group">                			
                		<label for="lastName1">Last Name:</label>
                		<s:textfield name="lastName" id="lastName1"  cssClass="form-control"/>
                	</div>
                	<%-- E-mail --%>
                	<div class="form-group">                			
                		<label for="email1">E-Mail:</label>
                		<s:if test="fieldErrors['email'] != null" >
							<section class="alert alert-danger">
							   	<p>							   		
									<s:iterator value="%{fieldErrors['email']}" >
							   			<s:property />
							   		</s:iterator>	
								</p> 
							</section>
					  	</s:if>	
                		<s:textfield type="email" name="email" size="25" placeholder="Email" id="email1" cssClass="form-control"/>
                	</div>
                	<%-- Access Type --%>
                	<div class="form-group">                			
                		<label for="accessType1">Access Type:</label>
                		<s:if test="fieldErrors['access'] != null" >
							<section class="alert alert-danger">
							   	<p>
									<s:iterator value="%{fieldErrors['access']}" >
							   			<s:property />
							   		</s:iterator>
								</p> 
							</section>
					  	</s:if>	
					  	
					  	<s:if test="#session.user.isAdministrator()">
					  	
	                		<s:select id="accessType1" theme="simple" 
	                				  name="access" list="@net.project.enums.AccessEnum@values()" 
	                				  cssClass="form-control" 
	                				  listValue="getAccess()"/>
                		</s:if>
                		<s:else>
                			<s:select id="accessType1" theme="simple" 
	                				  name="access" list="@net.project.enums.AccessEnum@values()" 
	                				  cssClass="form-control" 
	                				  listValue="getAccess()" disabled="true"/>
	                				  
	                		<s:hidden name="access"/>
                		
                		</s:else>
                	</div>
                	<s:submit type="button" name="btnSave" value="Save" cssClass="btn btn-primary" id="saveUseId" onclick="buttonSpinner('saveUseId');"/>
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

