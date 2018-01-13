<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<body>
<div id="bg">
    <img src="images/background.jpg" alt="home">
</div>
  <div id="main">
  
  <%-- top menu --%>
  <s:url id="signInUrl" action="displayLogin" />
  <s:url id="logOutUrl" action="logOut" />
  <s:url id="MainPageUrl" action="mainPageAction" /> ok
  <s:url id="changepass" action="changepassword" /> ok
  <s:url id="adduser" action="addEditUser" />      ok
  <s:url id="listAllUsersUrl" action="userList" /> ok
  <s:url id="logsUrl" action="loadLogs" /> ok
  <s:url id="optionsUrl" action="displayOptions" /> ok
  <s:url id="manaWorkerUrl" action="manageWorkers" /> ok
  <s:url id="eventAlertUrl" value="loadEvents"/> ok  		                   	
  
    <header>
      <div id="logo" >
        <div id="logo_text"  >
          <!-- class="logo_colour", allows you to change the colour of the text -->
          <h1>Application</h1>
          <h2><s:property value="#session.currentDate"/> </h2>          
        </div>
      </div>

      <nav>
        <div id="menu_container">
          <ul class="sf-menu" id="nav">
            <li><s:a href="%{MainPageUrl}" title="Home" onclick="document.body.style.cursor='wait';">Home</s:a></li>
            <s:if test="#session.user.access == @net.project.enums.AccessEnum@ADMIN" >
            	<li><s:a href="%{optionsUrl}" title="Home" onclick="document.body.style.cursor='wait';">Options</s:a></li>
            </s:if>
          </ul>
        </div>
      </nav>
    </header>
    <%-- end of top menu --%>
    
    <%-- Side menu --%>
    <div id="site_content">
      <div id="sidebar_container">
      	<div class="sidebar">
	  		<br/>
	  		<s:if test="#session.user == null">
				Welcome Guest
			</s:if>
			<s:else>
				Welcome <s:property value="#session.user.firstName"/>
			</s:else>
			<br/>
		</div>
		<%-- user that has not signed in yet --%>
      	<br/>	
      	<s:if test="#session.user == null" >
       		<div class="sidebar">
				<s:a href="%{signInUrl}" title="Sign In" onclick="document.body.style.cursor='wait';"> 
						Sign in
				</s:a>				
			</div>  
        </s:if>   
      	
      	<%-- users with regular access that can do things. --%>
      	<s:if test="#session.user.access == @net.project.enums.AccessEnum@REGULAR || #session.user.access == @net.project.enums.AccessEnum@ADMIN" >
	        <div class="sidebar">
	          <h4>Menu Options</h4>
	          <ul>
				<li> <s:a href="%{manaWorkerUrl}" title="Manage workers" onclick="document.body.style.cursor='wait';">
					   	Manage workers
				    </s:a>
				</li>
				<li> <s:a href="%{eventAlertUrl}" title="Manage workers" onclick="document.body.style.cursor='wait';">
					   	Events
				    </s:a>
				</li>

	          </ul>	           
	        </div>        
	        
        </s:if>
        <s:if test="#session.user.access == @net.project.enums.AccessEnum@ADMIN" >
	        <div class="sidebar">
	          <h4>Admin options</h4>
	          <ul>
	            <li><s:a href="%{listAllUsersUrl}" title="List all users" onclick="document.body.style.cursor='wait';">
	            		List Users
	            	</s:a>
	            </li>
	            <li><s:a href="%{adduser}" title="Add new User" onclick="document.body.style.cursor='wait';">
	            		Add user
	            	</s:a>
	            </li>
	            <li><s:a href="%{logsUrl}" title="Browse Logs" onclick="document.body.style.cursor='wait';">
	            		Browse logs
	            	</s:a>
	            </li>           
	          </ul>
	        </div>
        </s:if>      
        <s:if test="#session.user != null" >
	        <div class="sidebar">
		       	<s:a href="%{changepass}" title="Change Password" onclick="document.body.style.cursor='wait';"> 
						Change Password
				</s:a> <br/>  <br/>      	
	
				<s:a href="%{logOutUrl}" title="Log out" onclick="document.body.style.cursor='wait';"> 
						Log Out
				</s:a>				
			</div>    
		</s:if>    
      </div>
      <%-- end of side menu --%>