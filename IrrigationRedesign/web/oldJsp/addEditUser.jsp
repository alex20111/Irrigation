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
      
      <h1><Strong>User Manager</Strong> </h1>
      
      <s:form theme="simple" action="saveUser" onsubmit="document.body.style.cursor='wait';">      	
      	<div class="form_settings">
      		<s:token/>
	      	<s:hidden name="userId"/>
	      
	      	<p>
		      	<Strong>User Name:</Strong><br/>
		      	<s:if test="fieldErrors.userName != null" >
						<div style="color: Red;">
							<ul><li>
								<s:property value="fieldErrors.userName.get(0)"  />
								</li>
							</ul>
						</div>
				</s:if>
		      	<s:textfield name="userName"/><br/>
	      	</p>
	      	<p>
		      	<Strong>First Name:</Strong><br/>
		      	<s:if test="fieldErrors.firstName != null" >
						<div style="color: Red;">
							<ul><li>
								<s:property value="fieldErrors.firstName.get(0)"  />
								</li>
							</ul>
						</div>
				</s:if>
		      	<s:textfield name="firstName"/><br/>
	      	</p>
	      	<p>
		      	<Strong>Last Name:</Strong><br/>
		      	<s:textfield name="lastName"/><br/>
	      	</p>
	      	<p>
		      	<Strong>E-Mail:</Strong><br/>
				<s:if test="fieldErrors.email != null" >
						<div style="color: Red;">
							<ul><li>
								<s:property value="fieldErrors.email.get(0)"  />
								</li>
							</ul>
						</div>
				</s:if>
				<s:textfield type="email" name="email" size="25" placeholder="Email" /><br/>
	      	</p>
	      	<p>
		      	<Strong>Access Type:</Strong><br/>
		      	<s:if test="fieldErrors.access != null" >
						<div style="color: Red;">
							<ul><li>
								<s:property value="fieldErrors.access.get(0)"  />
								</li>
							</ul>
						</div>
				</s:if>
		      	<s:select theme="simple" name="access" list="@net.project.enums.AccessEnum@values()" listValue="getAccess()"/>
	      	</p>
	      	<br/>
	      	<s:submit name="btnSave" value="Save" cssClass="submit"/>
      	</div>
      
      </s:form>


      </div> <%-- do not delete --%>
    </div>

        <jsp:include page="/jsp/footer.jsp" />