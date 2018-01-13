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
                    <h1 class="page-header">Events <s:if test="workerId > 0"> for Worker <s:property value="workerId"/></s:if></h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />
				
			<s:form theme="simple" action="loadEvents" onsubmit="document.body.style.cursor='wait';" cssClass="submit-once">
				<div class="row">
	                <div class="col-lg-4">
	                										               			
		                	<sj:datepicker id="from1" 
		                				   name="fromDate" 
		                				   label="From Date" 
		                				   size="11" 
		                				   displayFormat="yy-mm-dd"
		                	/> 		             
		               	                	
							<sj:datepicker id="to1" 
							name="toDate" 
							label="To Date" 
							size="11" 
							displayFormat="yy-mm-dd"/> 
		               
						<div class="form-group">
							<label for="from1">By Worker:</label>
							<s:select name="workerId"  
									  list="workers" 
									  listKey="getWorkId()" 
									  listValue="getWorkId()"   
									  headerKey="" 
									  headerValue=" " 
									  cssClass="form-control" 
									  />
						</div>
						<s:submit type="button" value="Refresh" cssClass="btn btn-primary" id="refreshIdBtn" onclick="buttonSpinner('refreshIdBtn');"/>
					</div>
				</div>			
			</s:form>
			<br/>
			<div class="panel panel-primary">
            	<div class="panel-heading">
                	Result
                </div>
                <div class="panel-body">
                	<div class="table-responsive">
						<table class="table table-striped table-hover">
							<thead>				
								<tr>
									<th>Worker</th>
									<th>Event Description</th>
									<th>Recorded date</th>
								</tr>
							</thead>
							<tbody>
								<s:if test="#session.displayEvents.size() > 0">
									<s:iterator value="#session.displayEvents">
										<tr>
											<td><s:property value="evntWorkerId"/></td>
											<td><s:property value="event"/></td>
											<td><s:date name="eventDate" format="MMM dd HH:mm.ss" /></td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr>
										<td colspan="2"> No Events for that date</td>							
									</tr>
								</s:else>
							</tbody>
						</table>
					</div>
                   </div>                       
                </div>
		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>
			
		
	 </div>
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->
