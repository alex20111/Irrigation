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
                    <h1 class="page-header">Notification</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
			
			<%-- main form  --%>
			<s:form action="saveNotification" theme="simple" method="POST"  cssClass="submit-once"> 
			<s:hidden name="page"/>	
			<s:token/>
			
			<div class="form-group"> 
				<label class="form-check-label">
					  <s:checkbox name="config.activateWarning"  cssClass="form-check-input" id="actWarningId"/>
					  Activate Warning 									  
				</label>&nbsp;<span class="fa fa-info-circle" style="cursor:pointer;color:#337a9f" onclick="toggleHelpText('warningId')" ></span>	
					  <p class="help-block" style="display:none" id="warningId">
								Send warnings through the e-mail when any workers become non reachable or anything else.	
					</p>						
			</div>			
			
			
		
			<div class="row">
                <div class="col-lg-4">			
					
					<div class="panel panel-primary">
                        <div class="panel-heading">
                           <h3> Notifications</h3>
                        </div>
                        <div class="panel-body">                  	
                        
							
							<div class="form-group"> 
								<label class="form-check-label">
									  <s:checkbox name="config.notiBattLow"  cssClass="form-check-input" id="battLowChkId"/>
									  Battery Low warning.								  
								</label>&nbsp;<span class="fa fa-info-circle" style="cursor:pointer;color:#337a9f" onclick="toggleHelpText('battLowTxtId')" ></span>	
									  <p class="help-block" style="display:none" id="battLowTxtId">
												Send warnings through e-mail when the battery is low (Every 30min). Only for battery powered Workers.	
										</p>						
							</div>
							<div class="form-group"> 
								<label class="form-check-label">
									  <s:checkbox name="config.notiRainNotWatering"  cssClass="form-check-input" id="rainChkId"/>
									  No watering because of rain									  
								</label>&nbsp;<span class="fa fa-info-circle" style="cursor:pointer;color:#337a9f" onclick="toggleHelpText('noWaterRainId')" ></span>	
									  <p class="help-block" style="display:none" id="noWaterRainId">
												Send notification through e-mail that the worker did not water because it was raining.	
										</p>						
							</div>   
							<div class="form-group"> 
								<label class="form-check-label">
									  <s:checkbox name="config.notiUserLogin"  cssClass="form-check-input" id="usrLogChkId"/>
									  User login 									  
								</label>&nbsp;<span class="fa fa-info-circle" style="cursor:pointer;color:#337a9f" onclick="toggleHelpText('userLoginId')" ></span>	
									  <p class="help-block" style="display:none" id="userLoginId">
												Send notification through e-mail everytime a user login.
										</p>						
							</div>   
							<div class="form-group"> 
								<label class="form-check-label">
									  <s:checkbox name="config.notiErrors"  cssClass="form-check-input" id="errorsChkId"/>
									  Errors								  
								</label>&nbsp;<span class="fa fa-info-circle" style="cursor:pointer;color:#337a9f" onclick="toggleHelpText('errorId')" ></span>	
									  <p class="help-block" style="display:none" id="errorId">
												Send notification through e-mail when an error occurs.
										</p>						
							</div>      
							                     	
                        </div>
                       
                    </div>
                    
          
                    
                    
				               	
                	<div class="panel panel-primary">
                        <div class="panel-heading">
                            <h3>Warning email server</h3>
                        </div>
                        <div class="panel-body">
                        	<s:hidden name="showPasswordBox"/>
                        
                        	<%-- email Provider --%>
		                	<div class="form-group">                			
		                		<label for="providerId">Email provider:</label>
					                <s:if test="fieldErrors['provider'] != null" >
										<section class="alert alert-danger">
										   	<p>
												<s:iterator value="%{fieldErrors['provider']}" >
										   			<s:property />
										   		</s:iterator>
											</p> 
										</section>
								  	</s:if>	
		                		<s:select id="emailProviderId"  
			                			  name="config.emailProvider" list="@net.project.enums.EmailProvider@values()" 
			                			  cssClass="form-control" 
			                			  listValue="getEmailClient()"	             			 
			                			  />    
		                	</div>		                        	
                        	
                        	<div class="form-group">
                        		<label >Email:</label>  
                        		 <s:if test="fieldErrors['emailUser'] != null" >
									<section class="alert alert-danger">
									   	<p>
											<s:iterator value="%{fieldErrors['emailUser']}" >
									   			<s:property />
									  		</s:iterator>
										</p> 
									</section>
								  </s:if>	
								<s:textfield name="config.emailUserName" size="30" cssClass="form-control" type="email" id="emailNameId" placeholder="Enter email"/> 
							</div> 
							<div class="form-group">
								<s:if test="showPasswordBox">
									<label>Email password:</label>
									<s:if test="fieldErrors['emailPass'] != null" >
										<section class="alert alert-danger">
										   	<p>
												<s:iterator value="%{fieldErrors['emailPass']}" >
										   			<s:property />
										  		</s:iterator>
											</p> 
										</section>
									  </s:if>	
									<s:password name="config.emailPassword" size="15" cssClass="form-control" id="emailPassId"/> 
									<s:submit type="button"  name="testEmailBtn" value="Test Account" cssClass="btn btn-primary btn-sm" id="tstEmailId" onclick="buttonSpinner('tstEmailId');"/>
								</s:if>
								<s:else>
									<s:submit type="button" name="changeEmailBtn" value="Change email password" cssClass="btn btn-primary btn-sm"  id="notiChPassId" onclick="buttonSpinner('notiChPassId');"/> <br/>
								</s:else>
				  			 </div> 
                        </div>                       
                    </div>
                	
                	
                	
                	<s:submit type="button" cssClass="btn btn-primary" value="Save" id="saveNotiId" onclick="buttonSpinner('saveNotiId');"/>
                </div>
			</div>    	
      
      		</s:form>		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>	
	 </div>
	 
	 <script>
	 
	$(document).ready(function() {
		 
		 //disabe the buttons if the weather is not enabled.
		<s:if test="config == null || !config.activateWarning">
			toggleDisable();
		</s:if>
	 });
	 
	 $("#actWarningId").click(function(){		 
		 //toggle between enabled and disabled.
		 toggleDisable();	
		 
	 });
	     
	 function toggleDisable(){
		 $('#battLowChkId').prop('disabled', function(i, v) { return !v; });
		 $('#usrLogChkId').prop('disabled', function(i, v) { return !v; });
		 $('#rainChkId').prop('disabled', function(i, v) { return !v; });
		 $('#errorsChkId').prop('disabled', function(i, v) { return !v; });
		 
		 $('#emailProviderId').prop('disabled', function(i, v) { return !v; });
		 $('#emailNameId').prop('disabled', function(i, v) { return !v; });
		 $('#emailPassId').prop('disabled', function(i, v) { return !v; });
		 
	 };	
	 
	 </script>
	 
    <!-- /#wrapper -->
<!-- START Top Nav Menu -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->





