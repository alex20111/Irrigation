<%@ taglib prefix="s" uri="/struts-tags" %>



 
<s:url id="manaWorkerUrl" action="manageWorkers" />
<s:url id="eventAlertUrl" value="loadEvents"/>  
<s:url id="adduser" action="addEditUser" />      
<s:url id="listAllUsersUrl" action="userList" />
<s:url id="logsUrl" action="loadLogs" />
<s:url id="changepass" action="changepassword" />
<s:url id="refreshUrl" value="/refresh.action">	</s:url>
<s:url id="weatherUrl" value="/loadWeatherListPage.action">	</s:url>



<div class="navbar-default sidebar " role="navigation">
                <div class="sidebar-nav navbar-collapse collapse">
                    <ul class="nav" id="side-menu">
                        <li class="sidebar-search">
                        	<s:a href="%{MainPageUrl}" title="Home" ><i class="fa fa-dashboard fa-fw"></i> Dash Board</s:a>
                        </li>
                        
                        <s:if test="#session.user.access == @net.project.enums.AccessEnum@REGULAR || #session.user.access == @net.project.enums.AccessEnum@ADMIN" >
	                        <li>
	                        	<a href="#"><i class="fa fa-briefcase fa-fw"></i> Workers <span class="fa arrow"></span></a>
	                            <ul class="nav nav-second-level">
	                            	<li><s:a href="%{manaWorkerUrl}" title="Manage workers">
						   					<i class="fa fa-briefcase fa-fw"></i> Manage Workers
					    				</s:a>                              <!-- fa-gavel -->
						            </li>
						            <li> 
						            	<s:a href="%{weatherUrl}" title="Weather" cssClass="link-submit-once" id="loadWeatherId">
						            		<i class="fa fa-cloud fa-fw"></i> Weather
						            	</s:a>
						            </li>
						             <li> 
						            	<s:a href="%{refreshUrl}" title="Refresh" cssClass="link-submit-once">
						            		<i class="fa fa-refresh fa-fw"></i> Refresh All Workers
						            	</s:a>
						            </li>
						                          
	                            </ul>					    		
	                        </li>
							<li>           
	                            <s:a href="%{eventAlertUrl}" title="Events"  onclick="resetEvents()" >
								   	<i class="fa fa-table fa-fw"></i>All Events
							    </s:a>
	                        </li>
                        </s:if>
                        
                        <s:if test="#session.user.access == @net.project.enums.AccessEnum@ADMIN" >
	                        <li>
	                            <a href="#"><i class="fa fa-cog fa-fw"></i> Admin <span class="fa arrow"></span></a>
	                            <ul class="nav nav-second-level">
	                            	<li><s:a href="%{listAllUsersUrl}" title="List all users">
						            		<i class="fa fa-users fa-fw"></i>List Users
						            	</s:a>
						            </li>
						            <li><s:a href="%{adduser}" title="Add new User" >
						            		<i class="fa fa-user-plus fa-fw"></i>Add user
						            	</s:a>
						            </li>
						            <li><s:a href="%{logsUrl}" title="Browse Logs" >
						            		<i class="fa fa-book fa-fw"></i>Browse logs
						            	</s:a>
						            </li>                        
	                            </ul>
	                            <!-- /.nav-second-level -->
	                        </li>
                        </s:if>
                        <s:if test="#session.user != null" >
	                        <li>
	                        	<s:a href="%{changepass}" title="Change Password" > 
									<i class="fa fa-edit fa-fw"></i>Change Password
								</s:a>
	                        </li>                        
                        </s:if>
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            <!-- /.navbar-static-side -->