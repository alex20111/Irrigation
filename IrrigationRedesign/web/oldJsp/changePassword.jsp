<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>


<jsp:include page="/jsp/header.jsp" />

<jsp:include page="/jsp/sideMenu.jsp" />


<div class="content">
      
      <h1><Strong>Change Password</Strong> </h1>

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
      <s:form theme="simple" action="savepassword" onsubmit="document.body.style.cursor='wait';">
      	<s:token/> 
			<div class="form_settings">								
				<s:if test="fieldErrors.newpass != null" >
					<div style="color: Red;">						
						- <s:property value="fieldErrors.newpass.get(0)"  />					
					</div>
				</s:if>
				New Password: <br/>
				<s:password name="newPassword" size="20" cssStyle="margin-bottom: 10px;"/> <br/>
				
				<s:if test="fieldErrors.confpass != null" >
					<div style="color: Red;">						
						- <s:property value="fieldErrors.confpass.get(0)"/>						
					</div>					
				</s:if>
				Confirm New Password: <br/>
				<s:password name="confirmPassword" size="20" cssStyle="margin-bottom: 10px;"/> <br/>
				<s:submit value="Save" cssClass="submit"/> <br/><br/>				
			</div> 
	  </s:form>

      </div> <%-- do not delete --%>
    </div>

        <jsp:include page="/jsp/footer.jsp" />