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
	<h2>Options</H2> 
	<h3><strong>Server Options:</strong></h3>
		<s:form action="saveOptions" theme="simple" method="POST" enctype="multipart/form-data">
			<s:token/>
			<div class="form_settings">
				<p>						
				    <s:submit name="restartBtn" value="Restart Server" cssClass="submit"/> 
				    <s:submit name="stopBtn" value="Stop Server" cssClass="submit"/>
				</p>			
				<h3><strong>Configuration</strong></h3>	
				<strong>Serial Port:</strong> <br/>
				<s:textfield name="config.serialPort" size="25" maxlength="300"/><br/> 
				<strong>Ping Workers:</strong> <br/>
				<s:textfield name="config.checkWorkers" size="5" maxlength="3"/> Minutes
				<img src="<%=request.getContextPath()%>/images/info.png" alt="Information icon" width="16" height="16" 
          		 				title="Verify every defined number of minutes if the worker is alive (working)." style="height: 16px;width: 16px;" /> <br/>
				
				
				<strong>Activate Warning</strong> <s:checkbox name="config.activateWarning" />
				<img src="<%=request.getContextPath()%>/images/info.png" alt="Information icon" width="16" height="16" 
          		 				title="Send warnings through the e-mail when any workers
          		 				 become non reacheable or anything else." style="height: 16px;width: 16px;" /> <br/>
				<s:hidden name="config.configId"/>
			
				<h3><strong>Warning email server</strong></h3>		
				<s:hidden name="showPasswordBox"/>
				<strong>Google email:</strong><br/>
				<s:textfield name="config.emailUserName" size="30"/> 
				<s:if test="showPasswordBox">
					<br/>
					<strong>Google email password: </strong><br/>
					<s:password name="config.emailPassword" size="15"/> <s:submit name="testEmailBtn" value="Test Account" cssClass="submit"/><br/>
				</s:if>
				<s:else>
					<s:submit name="changeEmailBtn" value="Change email password" cssClass="submit"/> <br/>
				</s:else>
				<h3><strong>Server Software Update</strong></h3>
					<strong>New WAR file:</strong> <br/>
					<s:file name="serverFile"/>
					<s:submit name="newWarFileBtn" value="Update Server"  cssClass="submit"/> <br/>
					
					
				<br/>
				<s:submit cssClass="submit" name="saveOptionsBtn" value="Save"/>
			</div>
		</s:form>	
</div>
</div>
    
<jsp:include page="/jsp/footer.jsp" />