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
		<h2>Workers</H2> 		
			<p>		
				<table>
					<tr>
						<th style="text-align:center">Id</th>
						<th style="text-align:center">status</th>
						<th style="text-align:center">Name</th>
						<th style="text-align:center">Schedule</th>
						<th style="text-align:center">Watering<br/> time</th>
						<th style="text-align:center">Override</th>
						<th style="text-align:center">Start/Stop<br/>schedule</th>
						<th style="text-align:center"></th>
						<th style="text-align:center"></th>
						<th style="text-align:center"></th>									
					</tr>
					<s:iterator value="workers" status="index">
						 <s:url id="modWokerUrl" action="modifyWorker" >
  								<s:param name="worker.workId"><s:property value="workId"/></s:param>
  								<s:param name="worker.managed"><s:property value="isManaged()"/></s:param>
  						</s:url> 
  						<s:url id="scheduleUrl" action="schedule" >
  									<s:param name="worker.workId"><s:property value="workId"/></s:param>  									
  									<s:param name="startStop"><s:if test="isScheduleRunning()">stop</s:if><s:else>start</s:else></s:param>
  								
  						</s:url> 
  						<s:url id="chartStatUrl" action="loadChart" >
  								<s:param name="workerId"><s:property value="workId"/></s:param>
  						</s:url>
  						<s:url id="deleteUrl" action="delete" >
  								<s:param name="worker.workId"><s:property value="workId"/></s:param>
  						</s:url>  						
					
						<tr>
							<td><s:property value="workId"/></td>
							<td><s:if test="isManaged()"> managed </s:if> <s:else>Unmanaged</s:else></td>
							<td><s:property value="name"/>
								<span id="name<s:property value="#index.count"/>desc" style="display:none" class="showHide" ><br/>
									<strong>Description:</strong> <br/>
									<s:property value="description"/>
								</span>
							</td>
							<td><s:property value="schedType.getDesc()"/><br/> <s:property value="schedStartTime"/> </td>

							<td><s:property value="waterElapseTime"/> Min</td>
							<td><s:if test="doNotWater" >Do Not Water</s:if><s:else>Off</s:else></td>
							<td><s:if test="isScheduleRunning() && isManaged()">
									<s:a href="%{#scheduleUrl}" title="Stop">Stop</s:a>									
								</s:if>
								<s:elseif test="!isScheduleRunning() && isManaged()">
									<s:a href="%{#scheduleUrl}" title="Start">Start</s:a>
								</s:elseif>
							</td>
								
							<td><s:a href="%{#modWokerUrl}" title="Add or Edit worker">
			            			<s:if test="isManaged()">Edit </s:if> <s:else>Add</s:else>
				    			</s:a>	
				    									
							</td>
							<td><s:a href="%{#chartStatUrl}" title="Chart or status">
			            			Status
				    			</s:a>
				    		</td>
				    		<td><s:a href="%{#deleteUrl}" title="Delete worker" cssClass="confirmation">
			            			Delete
				    			</s:a>
				    		</td>
						</tr>	
					</s:iterator>		
				</table>
			</p>			
			
      </div>
      
      <script>
		$('.showSingle').click(function(){
        	$('#'+ this.id + "desc").toggle(200);
        	$('#'+ this.id).toggleClass('showSingle hideSingle');
   		});
		$('.confirmation').on('click', function () {
	        return confirm('Are you sure?');
	    });
	</script>
      
    </div>
    
    <jsp:include page="/jsp/footer.jsp" />
    
