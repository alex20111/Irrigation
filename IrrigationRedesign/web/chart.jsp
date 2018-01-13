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
                    <h1 class="page-header">Worker <s:property value="workerId"/> Charts & Status</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />
					
			<s:form theme="simple" action="loadChart" onsubmit="document.body.style.cursor='wait';">
				<div class="form-group">
					<s:hidden name="workerId"/>
					<strong>From:</strong><sj:datepicker id="date1" name="fromDate" label="From Date" size="11" displayFormat="yy-mm-dd" /> &nbsp <strong>To: </strong>
					 <sj:datepicker id="date2" name="toDate" label="To Date" size="11" displayFormat="yy-mm-dd" /> <br/><br/>
					 <s:submit type="button" value="Refresh" cssClass="btn btn-primary" id="refreshChartIdBtn" onclick="buttonSpinner('refreshChartIdBtn');"/>
				 </div>
			</s:form>
			<div class="table-responsive">
			<table class="table table-striped table-hover">		
				<thead>
					<tr>
						<th>Recorded Date</th>
						<th>Connected</th>
						<th>Watering</th>
						<th>Light Status</th>
						<th>Weather</th>
						<th>Battery Level</th>
						<th>Water consumption</th>
						<th>System comment</th>
					</tr>
				</thead>
				<tbody>
					<s:iterator value="workerStatusList">
						<tr>
							<td><s:date name="recordedDate" format="MMM dd HH:mm.ss" /></td>
							<td><s:if test="connected" > Yes </s:if> <s:else> No </s:else></td>
							<td><s:if test="workerWatering" > Yes </s:if> <s:else> No </s:else></td>
							<td><s:property value="lightStatus"/></td>
							<td><s:property value="rainStatus.getStatus()"/></td>
							<td><s:property value="batteryLevel"/></td>
							<td><s:property value="waterConsumption"/></td>
							<td><s:property value="systemComment"/></td>
						</tr>
					</s:iterator>
				</tbody>
			</table>
			</div>
		<!-- MAIN CONTENT STOP HERER-->	
		</div>	
	 </div>
    <!-- /#wrapper -->
<!-- START Top Nav Menu -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->

