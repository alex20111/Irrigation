<%@ taglib prefix="s" uri="/struts-tags" %>

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
                    <h1 class="page-header">List Users</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
		
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />	
			<i>Login block happen when a user tries to log in with the wrong password to many times (3 times)</i>
			
			<div class="table-responsive">
				<table class="table table-striped table-hover">
					<thead>
						<tr>
			 	   			<th>User Name</th>
			 	   			<th>Access Level</th>
			 	   			<th>Login Blocked</th>
			 	   			<th>Last Login</th>
			 	   			<th></th>
			 	   		</tr>
					</thead>
					<tbody>
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
			 	   						<s:a href="%{#userAccess}"
											cssClass="btn btn-success btn-xs"			 	   							
			 	   							 title="Click to block access to the user">
						            		No
							    		</s:a> 
			 	   					</s:if>
			 	   					<s:else> 	   						
				  						<s:a href="%{#userAccess}"
				  							cssClass="btn btn-danger btn-xs"	
				  						 	title="Click to unblock access to the user">
							            	Yes
								    	</s:a> 	   						
			 	   					</s:else>	   				
			 	   				</td>
			 	   				<td><s:date name="#usr.lastLogin" format="yyyy-MM-dd HH:mm.ss" /></td>
			 	   				<td><s:url id="deleteUserUrl" action="deleteUser" >
			 	   						<s:param name="userId"><s:property value="#usr.getId()"/></s:param>
			 	   					</s:url>		 	   				
			 	   					
			 	   					<s:a href="%{deleteUserUrl}" title="Delete User" id="del" 
			 	   						cssClass="btn btn-danger btn-xs"
			 	   						onclick="return confirm('Are you sure?');buttonSpinner('del');">
			 	   						Delete
			 	   					</s:a>
			 	   				</td>
			 	   			</tr>
			 	   		</s:iterator>					
					</tbody>
				</table>
			</div>		
		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>
			
		
	 </div>
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->