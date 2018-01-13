<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>


<jsp:include page="/jsp/header.jsp" />
<jsp:include page="/jsp/sideMenu.jsp" />

<div class="content">
 	<s:if test="hasActionErrors()" >
	   	<div class="errors">
			<s:actionerror escape="false" />
		</div>
	</s:if>
	<s:if test="hasActionMessages()" >
		<div class="success">
			<s:actionmessage escape="false" />
		</div>
	</s:if>
		
		
	<p>	
	<h2>Worker <s:property value="workerId"/> Charts & Status</H2>
		<s:form theme="simple" action="loadChart" onsubmit="document.body.style.cursor='wait';">
			<div class="form_settings">
				<s:hidden name="workerId"/>
				<strong>From:</strong><sj:datepicker id="date1" name="fromDate" label="From Date" size="11" displayFormat="yy-mm-dd" /> &nbsp <strong>To: </strong>
				 <sj:datepicker id="date1" name="toDate" label="To Date" size="11" displayFormat="yy-mm-dd" /> <br/><br/>
				 <s:submit value="Refresh" cssClass="submit"/>
			 </div>
		</s:form>
	<table>
		
		<tr>
			<th>Recorded Date</th>
			<th>Connected</th>
			<th>Watering</th>
			<th>Light Status</th>
			<th>Rain Status</th>
			<th>Battery Level</th>
			<th>Water consumption</th>
			<th>System comment</th>
		</tr>
		<s:iterator value="workerStatusList">
			<tr>
				<td><s:date name="recordedDate" format="MMM dd HH:mm.ss" /></td>
				<td><s:property value="connected"/></td>
				<td><s:property value="workerWatering"/></td>
				<td><s:property value="lightStatus"/></td>
				<td><s:property value="rainStatus"/></td>
				<td><s:property value="batteryLevel"/></td>
				<td><s:property value="waterConsumption"/></td>
				<td><s:property value="systemComment"/></td>
			</tr>
		</s:iterator>
	</table>		
  </div>
</div>

    <jsp:include page="/jsp/footer.jsp" />
  