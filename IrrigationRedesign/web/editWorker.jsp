<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

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
                    <h1 class="page-header">Edit Worker <s:property value="worker.workId" /></h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />
			
			
			<s:form name="form1" action="updateWorker" theme="simple" cssClass="submit-once">
			
			<s:token/>
			<s:hidden name="worker.workId"/>
			<s:hidden name="worker.scheduleRunning"/>
			
			<div class="row">
                <div class="col-lg-4">
                	
                	<%-- Worker Name --%>
                	<div class="form-group">                		
                		<label for="workerName1">Name:</label>
                		<s:if test="fieldErrors['name'] != null" >
							<section class="alert alert-danger">
							   	<p>
							   		<s:fielderror escape="false">
										<s:param>name</s:param>
									</s:fielderror>
								</p> 
							</section>
					  	</s:if>		
                		<s:textfield name="worker.name" id="workerName1" cssClass="form-control"/>
                	</div>
                
                	<%-- Description --%>
                	<div class="form-group">                		
                		<label for="Description1">Description:</label>                		
                		<sj:textarea resizable="true"
								resizableGhost="true"
								resizableHelper="ui-state-highlight"
								id="Description1"
								name="worker.description"
								rows="7"
								cols="80"
								cssClass="form-control"/>  
                	</div>
                	<%-- Schedule type --%>
                	<div class="form-group">                		
                		<label for="schedType1">Schedule Type:</label>   
                		<s:select name="worker.schedType" list="@net.project.enums.SchedType@values()" id="schedType1" cssClass="form-control" listValue="getDesc()" />
                	</div>
                	<%-- Schedule Time --%>
                	<div class="form-group input-group">  
                		<label for="schedTime1">Schedule time (start at):</label> <br/>
                		<sj:datepicker id="schedTime1" timepicker="true" name="worker.schedStartTime" timepickerOnly="true" label="Schedule Time" size="6" class="input-group-addon"/>                	
                	</div>
                	<%-- Watering time --%>
                	<div class="form-group">                		
                		<label for="waterTime1">How long to water (in minutes):</label>
                		<s:if test="fieldErrors['elapseTime'] != null" >
							<section class="alert alert-danger">
							   	<p>
							   		<s:fielderror escape="false">
										<s:param>elapseTime</s:param>
									</s:fielderror>
								</p> 
							</section>
					  	</s:if>		   
                		<s:textfield name="worker.waterElapseTime" size="5" id="waterTime1" cssClass="form-control" />
                	</div>           
                	
                	
                	<%-- Override --%>
                	<div class="form-check">
						<label class="form-check-label">
						 <s:checkbox name="worker.doNotWater" cssClass="form-check-input"/> 
						  Override, do not water
						</label>
					  </div>                	
                
                	<s:submit  type="button" value="Save" cssClass="btn btn-primary" id="saveEditWorkerId" onclick="buttonSpinner('saveEditWorkerId');"></s:submit> 
 
                </div>
            </div>
  		</s:form>
			
			
		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>
			
		
	 </div>
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->

