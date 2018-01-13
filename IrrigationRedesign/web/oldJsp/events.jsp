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

	<h2>Events <s:if test="workerId > 0"> for Worker <s:property value="workerId"/></s:if></h2> 
		<s:form theme="simple" action="loadEvents" onsubmit="document.body.style.cursor='wait';">
			<div class="form_settings">
				<strong>From:</strong><sj:datepicker id="date1" name="fromDate" label="From Date" size="11" displayFormat="yy-mm-dd" /> &nbsp <strong>To: </strong>
				<sj:datepicker id="date2" name="toDate" label="To Date" size="11" displayFormat="yy-mm-dd" /> <br/>
				<strong>By Worker</strong> <br/>
				<s:select name="workerId"  list="workers" listKey="getWorkId()" listValue="getWorkId()"   headerKey="" headerValue=" "  style="width: 100px"/> <br/>
				<s:submit value="Refresh" cssClass="submit"/>
			 </div>
		</s:form>
		<table>
			<tr>
				<th>Worker</th>
				<th>Event Description</th>
				<th>Recorded date</th>
			</tr>
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
		</table>	
 </div>
    </div>

    <jsp:include page="/jsp/footer.jsp" />