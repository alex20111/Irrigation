<%@ taglib prefix="s" uri="/struts-tags" %>

<!-- START Head -->
<jsp:include page="/jsp/header.jsp" />

</head>
<!-- END Head -->

<body>

	<div class="container">
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <div class="login-panel panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Sign In</h3>
                    </div>
                    <div class="panel-body">
                        <s:form role="form" action="loginUser" theme="simple" namespace="/" onsubmit="document.body.style.cursor='wait';">
                            <fieldset>
                            	 <!--  message and errors jsp -->
								<jsp:include page="/jsp/MsgError.jsp" />
								
                                <div class="form-group">
                                	<s:if test="fieldErrors['userName'] != null" >
										<section class="alert alert-danger">
										   	<p>
										   		<s:fielderror escape="false">
													<s:param>userName</s:param>
												</s:fielderror>
											</p> 
										</section>
								  	</s:if>	
									<s:if test="!remember">
								  		<s:textfield cssClass="form-control" id="usrName" name="userName" placeholder="User Name" autofocus="true" />
									</s:if>				
									<s:else>
										<s:textfield cssClass="form-control" id="usrName" name="userName" placeholder="User Name"  />
									</s:else>				  		
								  	                                
                                </div>
                                <div class="form-group">
                                	<s:if test="fieldErrors['password'] != null" >
										<section class="alert alert-danger">
										   	<p>
										   		<s:fielderror escape="false">
													<s:param>password</s:param>
												</s:fielderror>
											</p> 
										</section>
								  	</s:if>	
								  	<s:if test="remember">
								  		<s:password name="password" cssClass="form-control" placeholder="Password" id="passId"/>
								  	</s:if>
								  	<s:else>
								  		<s:password name="password" cssClass="form-control" placeholder="Password" autofocus="true" id="passId"/>
								  	</s:else> 
                                </div>
                                <div class="checkbox">
                                    <label>
                                    	<s:checkbox name="remember" />Remember Me
                                    </label>
                                </div>
                                <!-- Change this to a button or input when using this as a form -->
                                <s:submit  type="button" value="Login" cssClass="btn btn-lg btn-success btn-block" id="LoginId" onclick="buttonSpinner('LoginId');"/>
                            </fieldset>
                        </s:form>
                    </div>
                </div>
            </div>
        </div>
    </div>

<script>
	$(document).ready(function() {
		
		console.log('pass');
		console.log('<s:property value="password" />');
		
		<s:if test="remember" >
			$('#passId').val('<s:property value="password" />');
		</s:if>
		
	});
</script>

<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->
