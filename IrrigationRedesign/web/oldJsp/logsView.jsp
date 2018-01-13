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
      
      <h1><Strong>Logs View</Strong> </h1>  
		<s:form name="form1" action="showLog" theme="simple" onsubmit="document.body.style.cursor='wait';">      
       		<s:hidden name="selectedTab"/>
       
	        <sj:tabbedpanel id="localtabs"  selectedTab="%{selectedTab}"  >
	      	<sj:tab id="tab1" target="infoLogs" label="Info Logs"/>
	      	<sj:tab id="tab2" target="errorLogs" label="Error Logs"  />
	      		<div id="infoLogs">
	      			<s:select name="selInfoLogs" list="#session.infoLogFiles" listKey="getPath()" listValue="getName()"/> <br/>
	      			<s:submit value="Show" name = "btnInfoShow"/> <br/>
			    	<s:textarea name="infoLogText" readonly="true" cols="75" rows="20" theme="simple"></s:textarea> <br/>	
	      		</div>
	      		<div id="errorLogs">
	      			<s:select name="selErrorLogs" list="#session.errorLogFiles" listKey="getPath()" listValue="getName()"/> <br/>
	      			<s:submit value="Show" name="btnErrorShow"/> <br/>
			    	<s:textarea name="errorLogText" readonly="true" cols="75" rows="20" theme="simple"></s:textarea> <br/>
	      		</div>   			     
	       </sj:tabbedpanel>
       </s:form>
      </div> <%-- do not delete --%>
    </div>
    
    <jsp:include page="/jsp/footer.jsp" />