
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:url id="MainPageUrl" action="mainPageAction" />
<s:url id="signInUrl" action="displayLogin" />
<s:url id="logOutUrl" action="logOut" />
<s:url id="optionsUrl" action="displayOptions" />
<s:url id="configUrl" action="displayOptions" >
	<s:param name="page">config</s:param>
</s:url>
<s:url id="notificationUrl" action="displayOptions" >
	<s:param name="page">notification</s:param>
</s:url>
<s:url id="eventAlertUrl" value="/eventAjax.action"></s:url>

<s:url id="softUpdateUrl" action="updateSoftware" />
<s:url id="editCurrUserUrl" action="addEditUser" >
	<s:param name="userId"><s:property value="#session.user.id"/></s:param>
</s:url>  

<div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <s:a href="%{MainPageUrl}" title="Home" cssClass="navbar-brand"><s:property value="#application.pageTitle"/></s:a>
            </div>
            <!-- /.navbar-header -->

            <ul class="nav navbar-top-links navbar-right">		
			
				<!-- Server options -->
				<s:if test="#session.user != null && #session.user.canModify()" >
	                <li class="dropdown">
	                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
	                        <i class="fa fa-gear fa-fw"></i> <i class="fa fa-caret-down"></i>
	                    </a>
	                    <ul class="dropdown-menu dropdown-messages">
	                    	<s:if test="#session.user.access == @net.project.enums.AccessEnum@ADMIN" >
		                        <li>
		                        	<s:a href="%{optionsUrl}" title="Home" >
		                        		<div><i class="fa fa-server fa-fw"></i>Options</div></s:a>		                        
		                        </li> 
		                        <li>
		                        	<s:a href="%{softUpdateUrl}" title="Home" ><div><i class="fa fa-upload fa-fw"></i>Software Update</div></s:a>		                        
		                        </li>                   
	                        </s:if>	
	                        <li>
		                       	<s:a href="%{configUrl}" title="Home" ><div><i class="fa fa-cogs fa-fw"></i>Configuration</div></s:a>		                        
		                     </li>  
		                     <li>
		                      	<s:a href="%{notificationUrl}" title="Home" ><div><i class="fa fa-exclamation-circle fa-fw"></i>Notification</div></s:a>		                        
		                     </li>  
	                                               
	                    </ul>
	                    <!-- /.dropdown-user -->
	                </li>
				</s:if>
                     
                 <!-- Event alert -->
                <s:if test="#session.user != null"  >           
	               
	                <li class="dropdown">
	                    <a class="dropdown-toggle" data-toggle="dropdown" href="#" title="Alerts" id="alertTopMenuId">
	                        <i class="fa fa-bell fa-fw" id="bellId"></i> <i class="fa fa-caret-down"></i>
			
	                    </a>
	                    <ul class="dropdown-menu dropdown-messages" id="eventsTopId">
	                        <li>
	                            <a href="#">
	                                <div>
	                                    <i class="fa fa-comment fa-fw"></i> No new events
	                                    <span class="pull-right text-muted small">now</span>
	                                </div>
	                            </a>
	                        </li>
	                        <li class="divider"></li>                      
	                        <li>
	                            <a href="loadEvents" title="Events" class="text-center viewAllEventsClass"  onclick="resetEvents()"  id="viewAllEventsId">
	                                <strong>See All Alerts</strong>
	                                <i class="fa fa-angle-right"></i>
	                            </a>
	                        </li>
	                    </ul>
	                    <!-- /.dropdown-alerts -->
	                </li>        
                </s:if>
               
                <s:if test="#session.user != null">
               	 <!-- /.dropdown -->
	                <li class="dropdown">
	                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
	                        <i class="fa fa-user fa-fw"></i> <i class="fa fa-caret-down"></i>
	                    </a>
	                    <ul class="dropdown-menu dropdown-messages">
	                        <li>                      
		                        <s:a href="%{#editCurrUserUrl}" >
					            	<i class="fa fa-user fa-fw"></i> User Profile
						    	</s:a>
	                        </li>	                        
	                        <li class="divider"></li>
	                        <li>
	                        	<s:a href="%{logOutUrl}" title="Log out" onclick="document.body.style.cursor='wait';"> 
									<i class="fa fa-sign-out fa-fw"></i>Log Out
								</s:a>	     
	                        </li>
	                    </ul>
	                    <!-- /.dropdown-user -->
	                </li>
                </s:if>
                <s:else>
	                 <!-- sign in -->
					<li>
						<s:a href="%{signInUrl}" title="Sign In" > 
							<i class="fa fa-sign-in fa-fw"></i>
						</s:a>					
	                </li>
                </s:else>
                

            </ul>
            <!-- /.navbar-top-links -->