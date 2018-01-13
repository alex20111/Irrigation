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
                    <h1 class="page-header">Configuration</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
			
			<%-- main form  --%>
			<s:form action="saveConfiguration" theme="simple" method="POST" enctype="multipart/form-data"  cssClass="submit-once"> 
			<s:hidden name="page"/>
			<div class="form-group">			
				<s:submit type="button" cssClass="btn btn-primary"  value="Save" id="configSaveBtnIdTop" onclick="buttonSpinner('configSaveBtnIdTop');"/>
			</div>
			<div class="row">
                <div class="col-lg-4">
									
					<div class="panel panel-primary">
                        <div class="panel-heading">
                           <h3> Configuration</h3>
                        </div>
                        <div class="panel-body">
							<div class="form-group">
                        		<label for="userName1">Application Name:</label>   
                        		<s:if test="fieldErrors['appTitle'] != null" >
									<section class="alert alert-danger">
									   	<p>
									   		<s:fielderror escape="false">
												<s:param>appTitle</s:param>
											</s:fielderror>
										</p> 
									</section>
							  	</s:if>		                   
								<s:textfield name="config.applicationTitle" size="50" maxlength="50" cssClass="form-control"/>
							</div>
                        	
                        	<div class="form-group">
                        		<label for="userName1">Serial Port:</label>   
                        		<s:if test="fieldErrors['serial'] != null" >
									<section class="alert alert-danger">
									   	<p>
									   		<s:fielderror escape="false">
												<s:param>serial</s:param>
											</s:fielderror>
										</p> 
									</section>
							  	</s:if>		                   
								<s:textfield name="config.serialPort" size="25" maxlength="300" cssClass="form-control"/>
							</div>
							<div class="form-group"> 
								<label for="userName1">Ping Workers ( in minutes ):</label> 
									<div class="input-group">								
										<s:if test="fieldErrors['checkWorker'] != null" >
											<section class="alert alert-danger">
											   	<p>
											   		<s:fielderror escape="false">
														<s:param>checkWorker</s:param>
													</s:fielderror>
												</p> 
											</section>
									  	</s:if>	
										<s:textfield name="config.checkWorkers" size="5" maxlength="3" cssClass="form-control"/>
											<span class="input-group-addon"> 
												<span class="fa fa-info-circle" style="cursor:pointer; color:#337a9f" onclick="toggleHelpText('impacProgHelpId')" ></span>												
											</span>																				
			          		 		</div>
			          		 		<p class="help-block" style="display:none" id="impacProgHelpId">
												Verify every defined number of minutes if the worker is alive (working).
												It will bypass any workers on save power mode.
									</p>
			          		</div>						
						                      	
                        </div>
                       
                    </div> 
                	
                </div>
			</div>    	
      
			<div class="row">
                <div class="col-lg-4">
									
					<div class="panel panel-primary">
                        <div class="panel-heading">
                           <h3> Power saving<span class="fa fa-info-circle" style="cursor:pointer;color:#337a9f" onclick="toggleHelpText('powerSaveHelpId')" ></span> </h3>
                        </div>
                        <div class="panel-body">	
							<p class="help-block" style="display:none" id="powerSaveHelpId">
								Enter the from and to time that the worker will sleep for.<br/> 
							   To stop power saving, put the FROM and TO time to "select time"
							</p>							
							
							<div class="table-responsive">	
								<table class="table table-hover">
									<thead>
										<tr>
											<th>Name</th>
											<th>From</th>
											<th>To</th>											
										</tr>
									</thead>
									<tbody>
										<s:if test="!workers.isEmpty()">
											<s:iterator value="workers" status="idx">											
												<tr>
													<s:hidden name="workers[%{#idx.index}].workId"/>
													<td><s:property value="name"/></td>									
													<td><s:select name="workers[%{#idx.index}].startSleepTime" 							
														 cssClass="form-control"
														 list="#session.generatedTime"
														 listValue="value"
														 listKey="key"
														 headerKey="-1"
														 headerValue="Select Time"
														 />
													</td>
													<td><s:select name="workers[%{#idx.index}].stopSleepTime" 						
														 cssClass="form-control"
														 list="#session.generatedTime"
														 listValue="value"
														 listKey="key"
														 headerKey="-1"
														 headerValue="Select Time"
														 />
													<td>												
												</tr>
											</s:iterator>	
										</s:if>
										<s:else>
											<tr>
												<td colspan="3">No Workers </td>
											</tr>
										</s:else>
									</tbody>
								</table>
							</div>			
						</div>
					</div>
				</div>
			</div>
		<s:submit type="button" cssClass="btn btn-primary"  value="Save" id="configSaveBtnIdBtm" onclick="buttonSpinner('configSaveBtnIdBtm');"/>
	  
      		</s:form>		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>	
	 </div>
    <!-- /#wrapper -->
<!-- START Top Nav Menu -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->





