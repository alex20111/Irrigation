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
		
		<s:url id="uWorkersUrl" value="/unattatchedWorkers.action"/>
		 <s:url id="eventAlertUrl" value="/eventAjax.action"></s:url>

		 <!-- Event alert -->
		<sj:div id="eventAlert" href="%{eventAlertUrl}"	 updateFreq="5000"  cssStyle="float:left;">
			
	 	</sj:div>
				
		<!-- New worker notification -->				
		<sj:div id="dateDiv" href="%{uWorkersUrl}"	 updateFreq="10000" indicator="tmpLoading" cssStyle="float:right;">
			<img id="tmpLoading" src="<%=request.getContextPath()%>/images/loader.gif" alt="Loading..." style="display:none"/>
		 </sj:div>
		 
		 <!--  Refresh action  DONE!!!  -->
							<s:if test="#session.user != null" >
								<s:url id="refreshUrl" value="/refresh.action">	</s:url>
								<s:a href="%{#refreshUrl}" title="Refresh worker" onclick="document.body.style.cursor='wait';">
									<img src="<%=request.getContextPath()%>/images/reload-16.png" alt="Refresh" title="Refresh"/>
					    		</s:a> 
				    		</s:if>
		 
		<s:iterator value="workers" status="status">
		
		
		
		<s:if test="managed">
			<s:url id="ajax" value="/waterOnOff.action">
					<s:param name="workerId"><s:property value="workId"/></s:param>
			</s:url>
		
  
			<div id="workerdivId<s:property value='%{#status.index}'/>" 
				style="color:black;border-radius: 15px;width:380px;height:180px;background-color:#E5E4D7;padding: 10px 20px;margin-bottom: 10px;" >
				
				<div id="result<s:property value='%{#status.index}'/>" >  <%-- Result can be refresh by ajax. is loaded from the ResultBuilder.java--%>
					<s:property value="resultBody"   escapeHtml="false"/>
				</div>					
			 					
				
				<div  id="workerInfoId<s:property value='%{#status.index}'/>" style="display:none" > 
				
					<table style="text-align:right; background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">
						<tr style="padding: 1px 1px;">
							<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">
								<b>Name: </b>
							</td>
							<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">
								 <s:property value="name"/><br/>
							</td>
						</tr>
						<tr style="padding: 1px 1px;">
							<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">
								<b>Light:</b> 
							</td>
							<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">
								<s:property value="status.lightStatus.getStatus()" />
							</td>
						</tr>
						<tr style="padding: 1px 1px;">
							<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">						
								<b>Schedule: </b>
							</td> 
							<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">
								 <s:if test="scheduleRunning" > 
										Running <s:property value="schedType.getDesc()"/> at <s:property value="schedStartTime"/>
								 </s:if>
								 <s:else>
								      	 Stopped
								 </s:else>							
							</td>
						</tr>
						
						<s:if test="scheduleRunning" >
							<tr style="padding: 1px 1px;">
								<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">			
									<b>Duration: </b>
								</td> 
								
					 			<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;">		
					 				<s:property value="waterElapseTime"/> Minutes<br/> <!-- that line also disapear when  the schedule is stopped -->				 					
					 			</td>
					 			
					 		</tr>
					 		<tr style="padding: 1px 1px;">
					 			<td style="background-color:rgba(0, 0, 0, 0);padding: 1px 1px;" colspan="2">	
					 				Next schedule start on <s:date name="nextWateringBySched" format="MMM dd HH:mm" /> <!-- that line also disapear when  the schedule is stopped, also update it when the schedule is started. -->
					 			</td>
					 		</tr>
						 </s:if>								
					</table>				
								
				</div>
				<s:if test="#session.user != null && #session.user.canModify()">
					<img id="tmpLoading<s:property value='%{#status.index}'/>" src="<%=request.getContextPath()%>/images/loader.gif" alt="Loading..." style="display:none"/> 	
					<div class="btnHideShow" style="float:left">
					    <sj:a id="ajaxlink%{#status.index}" 
						    	href="%{ajax}" 
						    	targets="result%{#status.index}" 
						    	button="true" 
								buttonIcon="ui-icon-power"
								buttonText="false"
								title="Turn water on/off"
								indicator="tmpLoading%{#status.index}"	
								onClickTopics="before" 
								onCompleteTopics="complete"
						    >								
					    </sj:a>
					   </div>
					 </s:if>
				<div  id="moreToSeeId<s:property value='%{#status.index}'/>" style="float:right;padding:5px 5px 5px 5px;cursor: pointer;" onclick="toggleWorkerInfo(<s:property value='%{#status.index}'/>);" >  more</div>
			</div>
		
		</s:if>
		</s:iterator>	
		
		<script>
			function toggleWorkerInfo(sel){

				if ($("#workerInfoId" + sel).is(':hidden')){
					$("#workerdivId"+ sel).animate({'height':310},{duration:'slow'});
					$("#workerInfoId"+ sel).show(900);
					$("#moreToSeeId"+ sel).text("less");
				}else{
					$("#workerdivId"+ sel).animate({'height':180},{duration:'slow'});
					$("#workerInfoId"+ sel).hide(400);
					$("#moreToSeeId"+ sel).text("more");
				}
		
			}
			
			
	     $.subscribe('before', function(event,data) {
           $('.btnHideShow').hide();
       });
       $.subscribe('complete', function(event,data) {           
           $('.btnHideShow').show();
       });
		</script>	
			
      </div>
    </div>
    
    <jsp:include page="/jsp/footer.jsp" />