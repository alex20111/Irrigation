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
                    <h1 class="page-header">Workers</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
			
			<div class="table-responsive">
				<table class="table table-striped table-hover">
					<thead>
						<tr>
							<th>Id</th>
							<th>status</th>
							<th>Name</th>
							<th>Schedule</th>
							<th>Watering<br/> time</th>
							<th>Override</th>
							<th>Start/Stop<br/>schedule</th>
							<th></th>								
						</tr>
					</thead>
					<tbody>
						<s:iterator value="workers" status="index">
							 <s:url id="modWokerUrl" action="modifyWorker" >
	  								<s:param name="worker.workId"><s:property value="workId"/></s:param>
	  								<s:param name="worker.managed"><s:property value="isManaged()"/></s:param>
	  						</s:url> 
	  						<s:url id="scheduleUrl" action="schedule" >
	  									<s:param name="worker.workId"><s:property value="workId"/></s:param>  									
	  									<s:param name="startStop"><s:if test="isScheduleRunning()">stop</s:if><s:else>start</s:else></s:param>
	  								
	  						</s:url> 
	  						<s:url id="chartStatUrl" action="loadChart" >
	  								<s:param name="workerId"><s:property value="workId"/></s:param>
	  						</s:url>
	  						<s:url id="deleteUrl" action="delete" >
	  								<s:param name="worker.workId"><s:property value="workId"/></s:param>
	  						</s:url>  						
						
							<tr>
								<td><s:property value="workId"/></td>
								<td><s:if test="isManaged()"> managed </s:if> <s:else>Unmanaged</s:else></td>
								<td><s:property value="name"/>
									<span id="name<s:property value="#index.count"/>desc" style="display:none" class="showHide" ><br/>
										<strong>Description:</strong> <br/>
										<s:property value="description"/>
									</span>
								</td>
								<td><s:property value="schedType.getDesc()"/><br/> <s:property value="schedStartTime"/> </td>
	
								<td><s:property value="waterElapseTime"/> Min</td>
								<td><s:if test="doNotWater" >Do Not Water</s:if><s:else>Off</s:else></td>
								<td><s:if test="isScheduleRunning() && isManaged()">
										<s:a href="%{#scheduleUrl}" title="Stop" cssClass="link-submit-once">Stop</s:a>									
									</s:if>
									<s:elseif test="!isScheduleRunning() && isManaged()">
										<s:a href="%{#scheduleUrl}" title="Start" cssClass="link-submit-once">Start</s:a>
									</s:elseif>
								</td>
									
								<td><p>
										<s:a href="%{#modWokerUrl}" title="Add or Edit worker" cssClass="btn btn-primary btn-xs link-submit-once">
						            			<s:if test="isManaged()">Edit </s:if> <s:else>Add</s:else>
							    		</s:a>					    									
										
										<s:a href="%{#chartStatUrl}" title="Chart or status" cssClass="btn btn-warning btn-xs link-submit-once">
						            			Status
							    		</s:a>
							    		
							    		<s:a href="%{#deleteUrl}" title="Delete worker" cssClass="confirmation btn btn-danger btn-xs link-submit-once">
						            			Delete
							    		</s:a>
					    			</p>
					    		</td>
							</tr>	
						</s:iterator>	
					</tbody>
				</table>
			</div>	
		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>
	 </div>
	     <script>
		$('.showSingle').click(function(){
        	$('#'+ this.id + "desc").toggle(200);
        	$('#'+ this.id).toggleClass('showSingle hideSingle');
   		});
		$('.confirmation').on('click', function () {
	        return confirm('Are you sure?');
	    });
	</script>
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->


      
  
      

    
