<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<jsp:include page="/jsp/header.jsp" />

<%-- this jsp does not follow the same pattern as the other pages. --%>
<body>
<div id="bg">
    <img src="images/background.jpg" alt="home">
</div>
  <div id="main">
    <header>
      <div id="logo">
        <div id="logo_text">
          <!-- class="logo_colour", allows you to change the colour of the text -->
          <h1>Login</h1>          
        </div>
      </div>
      
    </header>
    <div id="site_content_login">	
    	<s:if test="actionErrors.size > 0" >    
    		<p style="padding-left: 25px;color: Red;"><s:actionerror escape="false"/></p>
		</s:if> 
		<fieldset style="display: inline-block;padding-top: 20px;padding-right: 50px; padding-bottom: 25px; padding-left: 25px;margin-left: 50px;">
			<legend style="color:white;">Login</legend>
			<s:form action="loginUser" theme="simple" namespace="/" onsubmit="document.body.style.cursor='wait';">
				<div class="form_settings">
								
					<s:if test="fieldErrors.userName != null" >
						<div style="color: Red;">
							<ul><li>
								<s:property value="fieldErrors.userName.get(0)"  />
								</li>
							</ul>
						</div>
					</s:if>
					User Name: <br/>
					<s:textfield id="usrName" name="userName" size="20" cssStyle="margin-bottom: 10px;"/> <br/>
					
					<s:if test="fieldErrors.password != null" >
						<div style="color: Red;">
							<ul><li>
								<s:property value="fieldErrors.password.get(0)"/>
								</li>
							</ul>
						</div>
						
					</s:if>
					Password: <br/>
					<s:password name="password" size="20" cssStyle="margin-bottom: 10px;"/> <br/>
					<s:submit value="Login" cssClass="submit"/> <br/><br/>
				</div> 
			</s:form>
		</fieldset>
    
    </div>

  </div>
  <!-- javascript at the bottom for fast page loading -->
  <script type="text/javascript" src="js/jquery.js"></script>
  <script type="text/javascript" src="js/jquery.easing-sooper.js"></script>
  <script type="text/javascript" src="js/jquery.sooperfish.js"></script>
  <script type="text/javascript">
    $(document).ready(function() {
      $('ul.sf-menu').sooperfish();
      $('.top').click(function() {$('html, body').animate({scrollTop:0}, 'fast'); return false;});
      $("#usrName").focus();
    });
  </script>
</body>
</html>