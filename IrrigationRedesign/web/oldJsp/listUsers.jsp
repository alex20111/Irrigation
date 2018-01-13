<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

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
      
      	<h1><Strong>List Users</Strong> </h1>
 	   	
 	   	<i>Login block happend when a user tries to log in with the wrong password to many times (3 times)</i>
 	   	<table>
 	   		<tr>
 	   			<th>User Name</th>
 	   			<th>Access Level</th>
 	   			<th>Login Blocked</th>
 	   			<th>Last Login</th>
 	   			<th></th>
 	   		</tr>
 	   		<s:iterator value="userList" var="usr">
 	   			<tr>
 	   				<td><s:url id="editUserUrl" action="addEditUser" >
 	   						<s:param name="userId"><s:property value="#usr.id"/></s:param>
 	   					</s:url>    	   				
 	   					<s:a href="%{#editUserUrl}" >
			            	<s:property value="#usr.userName" />
				    	</s:a>
 	   				</td>
 	   				<td><s:property value="#usr.access.getAccess()" /></td>
 	   				<td><s:url id="userAccess" action="blockUnBlockUser" >
  								<s:param name="userId"><s:property value="#usr.id"/></s:param>
  						</s:url> 	   				
 	   					<s:if test="#usr.nbOfTries < 3">
 	   						<s:a href="%{#userAccess}" title="Click to block access to the user">
			            		No
				    		</s:a> 
 	   					</s:if>
 	   					<s:else> 	   						
	  						<s:a href="%{#userAccess}" title="Click to unblock access to the user">
				            	Yes
					    	</s:a> 	   						
 	   					</s:else>	   				
 	   				</td>
 	   				<td><s:date name="#usr.lastLogin" format="yyyy-MM-dd HH:mm.ss" /></td>
 	   				<td><s:url id="deleteUserUrl" action="deleteUser" >
 	   						<s:param name="userId"><s:property value="#usr.getId()"/></s:param>
 	   					</s:url>
 	   					<s:a href="%{deleteUserUrl}" title="Delete User" id="del" onclick="return confirm('Are you sure?');document.body.style.cursor='wait';">Delete</s:a>
 	   				</td>
 	   			</tr>
 	   		</s:iterator>
 	   	</table>
      </div> <%-- do not delete --%>
    </div>
    
    <jsp:include page="/jsp/footer.jsp" />