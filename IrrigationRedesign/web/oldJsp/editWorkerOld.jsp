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
		<h2>Edit Worker <s:property value="worker.workId" /></H2> 		
		
			
		<s:form name="form1" action="updateWorker" theme="simple">
			<s:token/>
			<div class="form_settings">
				<s:hidden name="worker.workId"/>
				<s:hidden name="worker.scheduleRunning"/>
				<p>
					<s:fielderror escape="false">
						<s:param>name</s:param>
					</s:fielderror>
			  		<strong>Name:</strong> <br/>
			  		<s:textfield name="worker.name" /> <br/>
			 	</p>
			 	<p>
			  		<strong>Description:</strong> <br/>
			  		<sj:textarea resizable="true"
								resizableGhost="true"
								resizableHelper="ui-state-highlight"
								id="echo"
								name="worker.description"
								rows="7"
								cols="80"/>  		  		
			  		<br/>
		  		</p>
		  		<p>
			  		<strong>Schedule Type:</strong> <br/>
			  		<s:select name="worker.schedType" list="@net.project.enums.SchedType@values()" listValue="getDesc()" /> <br/>
			  	</p>
			  	<p>
			  		<strong>Schedule time:</strong> <br/>
			  		<sj:datepicker id="date0" timepicker="true" name="worker.schedStartTime" timepickerOnly="true" label="Schedule Time" size="6"/> <br/>
				</p>
				<p>
			  		<strong>How long to water (in minutes):</strong> <br/>
			  		<s:textfield name="worker.waterElapseTime" size="5"/>  <br/>
				</p>
				<p>
			  		<strong>Override, do not water:</strong> <s:checkbox name="worker.doNotWater"/> <br/>
			  	</p>	
			  		
			  	<s:submit value="Save" cssClass="submit"/>
	  		</div>
  		</s:form>			
						
			
       </div> <%-- do not delete --%>
    </div>
    <jsp:include page="/jsp/footer.jsp" />
