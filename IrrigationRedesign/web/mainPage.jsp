
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<!-- START Head -->
<jsp:include page="/jsp/header.jsp" />

<style>
			.popover-content{
				color:black;
			}

			#parentContainer {
			  position: relative
			}

			#nestedWifi {
			  position: absolute;
			  top: -4px;
			  left: -2px;
			  font-size: 170%;
			  color: rgba(217, 83, 79, 0.7);
			}
	</style>

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
		
		
		<s:url id="uWorkersUrl" value="/unattatchedWorkers.action"/>
			
			
		<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />		
		
		
		<div class="row"  style="padding-top:5px">
			
			<%-- Display unmanaged workers 	fa-gavel  --%>
			<sj:div id="dateDiv" href="%{uWorkersUrl}"	 updateFreq="10000" indicator="tmpLoading" cssClass="col-sm-3" cssStyle="max-width: 200px;">
					<img id="tmpLoading" src="<%=request.getContextPath()%>/images/loader.gif" alt="Loading..." style="display:none"/>
			 </sj:div>
		
		
			<s:if test="weather != null && weather.isActive()">
				<div class="col-sm-3 pull-right" >
					<ul class="list-group">
					  <li class="list-group-item text-center">
						<strong>Weather</strong><br/>
						Temp: <s:property value="weather.currTemp"/> - Condition: <s:property value="weather.weatherStatus.getStatus()"/> <br/>
						Last updated: <s:date name="weather.lastUpdated" format="yyyy-MM-dd HH:mm" />
					  </li>
					</ul>
				</div>
			</s:if>
		</div>
				
			
				
	<%-- Display managed  workers --%>

		<s:iterator value="workers" status="status">
			
			<s:if test="managed">
			
			<s:url id="ajax" value="/waterOnOff.action">
					<s:param name="workerId"><s:property value="workId"/></s:param>
			</s:url>
			
		
			
					
			<div class="row">
				<div class="col-sm-12">
					<div class="panel panel-primary" style="max-width: 320px;">
					
                      <div id="result<s:property value='%{#status.index}'/>" >  <%-- Result can be refresh by ajax. is loaded from the ResultBuilder.java--%>
						<s:property value="resultBody"   escapeHtml="false"/>
					  </div>	
					  
				                       	 
                       	<s:if test="#session.user.canModify() && !status.sleeping">
	                       	 <div class="row">
									<div class="col-xs-12 ">
										<div style="margin-right: 15px; margin-bottom: 40px"> 								
											<img id="tmpLoading<s:property value='%{#status.index}'/>" src="<%=request.getContextPath()%>/images/loader.gif" alt="Loading..." style="display:none" class="pull-right"/> 	
											<div class="btnHideShow pull-right" >
											    <sj:a id="ajaxlink%{#status.index}" 
												    	href="%{ajax}" 
												    	targets="result%{#status.index}" 
														cssClass="btn btn-success btn-circle"
														buttonText="false"
														title="Turn water on/off"
														indicator="tmpLoading%{#status.index}"	
														onClickTopics="before" 
														onCompleteTopics="complete">
																
														    <i class="fa fa-power-off"></i>
														    						
												</sj:a>
											 </div>
										</div>							
									</div>
							   </div> 
					   </s:if>
                       <div class="panel-footer">
						 <a href="#"  data-toggle="collapse" data-target="#demo<s:property value='%{#status.index}'/>" 
						 		 onclick="getWorkerStatus('<s:property value="workId"/>', <s:property value='%{#status.index}'/>)">
                              <span class="pull-left">Details</span>
                              <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
                              <div class="clearfix"></div>
						 </a>
						    <div id="demo<s:property value='%{#status.index}'/>" class="collapse closed"  > 
						   
						    	<img id="tmpLoadingId<s:property value='%{#status.index}'/>" src="<%=request.getContextPath()%>/images/loader.gif" alt="Loading..." style="display:none"/>
						    
						    
						    	<div id="resultWsId<s:property value='%{#status.index}'/>" style="display:none"></div>
						    	
								
							</div>
                        </div>				  
                   </div>
				</div>
			</div>
			</s:if>
			
		</s:iterator>
		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>
			
		
	 </div>
	 <script>
	$(document).ready(function(){
		$('[data-toggle="popover"]').popover();   
	});
	
	  $.subscribe('before', function(event,data) {
          $('.btnHideShow').hide();
      });
      $.subscribe('complete', function(event,data) {           
          $('.btnHideShow').show();
      });
      var myVar = new Array(15);
      function getWorkerStatus(workId, idx){
    	  
    	  var isClosed = false;
    	  
    	  if ( $("#demo"+idx).hasClass("closed")){
    		  isClosed = true;
    		  $("#demo"+idx).removeClass("closed");    		  
    	  }else{
    		  isClosed = false;
    		  $("#demo"+idx).addClass("closed"); 
    	  }
    	  
    	  if (isClosed){	    
    		  
    		  loadStatusForWorker(workId, idx);
    		  
    		  myVar[idx] = setInterval(loadStatusForWorker, 5000, workId, idx);    	  
    	  } else
    	  {
    		  clearTimeout(myVar[idx]);
    	  }
    	  
      }
      
      function loadStatusForWorker(workId, idx){
    	  $("#tmpLoadingId"+idx).show();
    	  
    	  $.get("workerStatusDisplay", {workerId :  workId}, function(data, status){
    	       if (data != null){
    	    	   
    	    	   $("#resultWsId"+idx).html(data);
    	    	   
    	    	   $("#resultWsId"+idx).show();
    	    	   
    	       }
    	       
    	       $("#tmpLoadingId"+idx).hide();
    	    });
      }
	 </script>
	 
    <!-- /#wrapper -->
<!-- START Footer -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->