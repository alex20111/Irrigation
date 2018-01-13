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
                    <h1 class="page-header">Weather</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
				
			<!--  message and errors jsp -->
			<jsp:include page="/jsp/MsgError.jsp" />
			
			<%-- main form  --%>
			<s:form theme="simple" action="saveWeather" cssClass="submit-once"> 
			
			<s:hidden name="weather.id"/>
				
			<div class="row">
                <div class="col-lg-6">
                	<div class="alert alert-info"> 
                			<div class="fa fa-info-circle"></div>
                			<div>If enabled, the system will not turn on the water if it is raining based on the weather forecasting.</div>
                			
                	</div>
                </div>		
	      	</div>
			
			<div class="row">
                <div class="col-lg-3">                	

                	<div class="checkbox">
						<label><s:checkbox name="weather.active" id="selActivateId"/>Select to activate</label>
					</div>
					
                
                	<%-- Weather Provider --%>
                	<div class="form-group">                			
                		<label for="providerId">Weather Provider:</label>
                		<s:if test="fieldErrors['provider'] != null" >
							<div class="alert alert-danger">
							   	<p>
							   		<s:fielderror escape="false"><s:param>provider</s:param></s:fielderror>
								</p> 
							</div>
						</s:if> 
                		<s:select id="providerId"  
	                			  name="weather.provider" list="@net.project.enums.WeatherProvider@values()" 
	                			  cssClass="form-control disbComp" 
	                			  listValue="getProviderName()"	    
	                			  onchange="emptyCityLoc()"	                			 
	                			  />    
                	</div>
                	
                	<div class="form-group" id="apiKeyId" style="display:none">  
              			<label>Api Key:</label><br/>
              			<i><small>An API key is required by the provider selected. You can find the API when you search for the provider API key on google.</small></i>
               			<s:textfield name="weather.apiKey" cssClass="form-control" id="apiKeyIdVal" />               			
              		</div> 
                	
                	
              		<div class="form-group">  
              			<label>City:</label>
               			<s:textfield name="userInputCity" cssClass="form-control" id="userCityInputId" /> 
               			<button type="button" id="loadLocsId" onclick="loadLocations(false)" class="btn btn-primary btn-sm">Load</button>
              		</div>               	
                	
                	
                	<div class="form-group">  
                		<label for="currLocId">Select Current Location:</label>
                		<s:select id="currLocId"  
	                			  name="weather.location" 
	                			  list="#{'NoLoc':'Please Select' }" 
	                			  cssClass="form-control "    			 
	                			 
	                			  /> 
                	</div>
                	
                	<div class="form-group">                			
                		<label for="refreshMinId">Refresh:</label>  
                		<s:if test="fieldErrors['refresh'] != null" >
							<div class="alert alert-danger">
							   	<p>
							   		<s:fielderror escape="false"><s:param>refresh</s:param></s:fielderror>
								</p> 
							</div>
						</s:if>             
                		<s:textfield  name="weather.refresh" cssClass="form-control" 
                						maxlength="10" placeholder="Minutes" id="refreshMinId" onchange="checkNumeric('refreshMinId')"		/>	
                		<p class="help-block"  id="ecrNbrHelpId">
							The refresh is used to display the weather on the main page. Each worker will call the weather option before turning on.
						</p>					 
                	</div>
               		
               	</div>
			</div> 
			 
			<div class="row">
                <div class="col-lg-8">
               		<h3 class="page-header">Enabled Workers</h3>
               		<div class="row">
               			 <div class="col-lg-4">
               			 	<label for="accessType1">Add worker:</label>
               			 	<s:if test="fieldErrors['worker'] != null" >
								<div class="alert alert-danger">
								   	<p>
								   		<s:fielderror escape="false"><s:param>worker</s:param></s:fielderror>
									</p> 
								</div>
							</s:if> 
               			 	<div id="msgDivId" class="alert alert-danger" style="display:none" >Please select a worker.</div>
		               		<div class="input-group">		               			
		                		<s:select id="workerListId"  
			                			  list="#session.allWorkersWeather" 
			                			  listKey="workId"
			                			  cssClass="form-control disbComp" 
			                			  listValue="name"
			                			  headerKey="-1"
			                			  headerValue="Please Select"
			                			  />   
								<span class="input-group-btn">
   									<button type="button" id="workerAddBtnId" class="btn btn-primary btn-sm">Add</button>
								</span> 
			                	
		               		</div>
		               		
		               	</div>
               		</div>
               		
               		<div class="clearfix"></div>
               		
                	<div class="table-responsive">
						<table class="table table-striped table-hover">                	
	                		<thead>
	                			<tr>	
	                				<th>Worker Name</th>
	                				<th>Active</th>
	                				<th></th>
	                			</tr>
	                		</thead>
	                		<tbody id="tableBody">
	                			<s:if test="weather != null && weather.getWorkers().size() >0">
		                			<s:iterator value="weather.getWorkers()"  status="status">
		                				
			                			<tr id="rowId<s:property value="%{#status.index}"/>">	
			                				<s:hidden name="wthWorkers[%{#status.index}].weatherId" value="%{weatherWrk.weatherId}" id="tableWeatherId%{#status.index}"/>
		                					<s:hidden name="wthWorkers[%{#status.index}].workerId" value="%{workId}" id="tableWorkerId%{#status.index}"/>
			                				<td id="workerNameId<s:property value="%{#status.index}"/>"><s:property value="name"/></td>
			                				<td>
			                					<s:if test="weatherWrk.active">
			                						Yes
			                					</s:if>
			                					<s:else>
			                						No
			                					</s:else>
			                				</td>
			                				<td>
			                					<button type="button" class="btn btn-danger btn-sm" onclick="removeWeatherWorker(<s:property value="%{#status.index}"/>)">Remove</button>
			                				</td>
			                			</tr>
		                			</s:iterator>
	                			</s:if>
	                			<s:else>
	                				<tr id="noWorkerInTableId">
	                					<td >
	                						- No weather enabled worker found, please add a worker.
	                					</td>
	                				</tr>
	                			</s:else>
	                		</tbody>
	                	</table>
                	</div>          
              		</div>
			</div>    
               
                	<s:submit type="button" name="btnSave" value="Save" cssClass="btn btn-primary" id="saveFormId" onclick="return validateForm();buttonSpinner('saveFormId');" style="display:none"/>
      
      		</s:form>		
		<!-- MAIN CONTENT STOP HERER-->	
		</div>	
	 </div>
	 
	 <script>
	 
	 $(document).ready(function() {
		 
		 //disabe the buttons if the weather is not enabled.
		<s:if test="weather == null || !weather.active">
			toggleDisable();
		</s:if>
		<s:else>
			loadLocations(true);
			showApiKey();
			$("#saveFormId").show();
						
			
		</s:else>
	 });
	 
	 //add new worker to the table.
	 var size = <s:if test="weather.getWorkers() != null"> <s:property value="weather.getWorkers().size()"/>; </s:if> <s:else>0;</s:else>
	 $("#workerAddBtnId").click(function(){ //button add
		 var value = $('#workerListId').find('option:selected').text(); //dropdown of workers
		 var key = $('#workerListId').find('option:selected').val(); ///same
		 
		 if (key !== "-1"){
			 
			 if ($("#noWorkerInTableId").length > 0) {
				 $("#noWorkerInTableId").remove();
			 }
			 
			 $("#msgDivId").hide(); 
			 $("#tableBody").append( "<tr id=\"rowId"+size+"\"> <input type=\"hidden\" name=\"wthWorkers["+size+"].weatherId\" value=\"<s:property value='-1'/>\" id=\"tableWeatherId"+size+"\"/>  " 
					 			+ " <input type=\"hidden\" name=\"wthWorkers["+size+"].workerId\" value=\""+key+"\" id=\"tableWorkerId"+size+"\"/>    " 
					 			+	"<td id=\"workerNameId"+size+"\">"+value+"</td><td>No</td><td><button type=\"button\" class=\"btn btn-danger btn-sm\" onclick=\"removeWeatherWorker("+size+")\"  id=\"removeWorkerId"+size+"\">Remove</button></td></tr>");
			 
			 $("#workerListId option[value='"+key+"']").remove();
			 
			 size ++;
		 }else{
			$("#msgDivId").show(300); 
		 }
	 
	 });
	 
	 
	 function removeWeatherWorker(idx){
		 
		 var name = $("#workerNameId" + idx).text();
		 var key = $("#tableWorkerId" + idx).val();
		 
		 //add the remove worker to the drop down.
		 $('#workerListId').append('<option value="'+key+'" selected="selected">' + name + '</option>');		 
		 
		 $("#rowId" + idx).remove();	
		 
		 if ($("#tableBody > tr").length == 0){
			 $("#tableBody").append(" <tr id=\"noWorkerInTableId\"><td > - No weather enabled worker found, please add a worker.	</td></tr>");
		 }		 
	 }
	 
	 $("#selActivateId").click(function(){	
		 
		 if ($('#selActivateId').is(":checked")){		 
		 	$("#saveFormId").show();
		 }
		 else
		{
			 $("#saveFormId").hide();
			 removeWeather();
		 }
		 //toggle between enabled and disabled.
		 toggleDisable();	
		 
	 });
	 
	 function toggleDisable(){
		 $('#providerId').prop('disabled', function(i, v) { return !v; });
		 $('#refreshMinId').prop('disabled', function(i, v) { return !v; });
		 $('#workerListId').prop('disabled', function(i, v) { return !v; });
		 $('#currLocId').prop('disabled', function(i, v) { return !v; });
		 $('#workerAddBtnId').prop('disabled', function(i, v) { return !v; });
		 $('#userCityInputId').prop('disabled', function(i, v) { return !v; });		 
	 };	 
	 
	 function validateForm(){
		
		 var errorDetected = false;
		 
		 if (!$('#userCityInputId').prop('disabled')){
			 var refreshMin = isNaN(parseInt($('#refreshMinId').val())) ? 0 : parseInt(parseInt($('#refreshMinId').val()));			 
			 
			 if(verifyProviderInput()){
				 errorDetected = true;
			 }
			
			 if (verifyLocationInput()){
				 errorDetected = true;
			 }

			 if (verifyCityInput()){
				 errorDetected = true;
			 }			
			 
			 if (refreshMin == 0 ){
				 if ($("#wrongRefresh").text().length == 0){
				 	$("#refreshMinId").before('<p class="alert alert-danger" id="wrongRefresh">Please enter a refresh number of 10 min or more.</p>');
				 }
				 errorDetected = true;
			 }
			 if (refreshMin < 10){
				 if ($("#wrongRefresh").text().length == 0){
				 	$("#refreshMinId").before('<p class="alert alert-danger" id="wrongRefresh">Please enter a refresh number of 10 min or more.</p>');
				 }
				 errorDetected = true;
			 }else{
				 $("#wrongRefresh").remove();
			 }			 
			 
			 if ($("#apiKeyId").is(":visible")){
				 
				 if ($("#apiKeyIdVal").val().length === 0){					 
					 if ($("#apiKeyIdMissing").text().length == 0){
						 $("#apiKeyIdVal").before('<p class="alert alert-danger" id="apiKeyIdMissing">Please enter a refresh number of 10 min or more.</p>'); 
					 }
					 errorDetected = true;
				 }else if ($("#apiKeyIdMissing").text().length > 0){
					 $("#apiKeyIdMissing").remove();
				 }
			 }else if ($("#apiKeyIdMissing").text().length > 0){
				 $("#apiKeyIdMissing").remove();
			 }
		 }
		 
		 if (errorDetected){
			 return false;
		 }
		 return true;		 
	 };
	 
	 function loadLocations(fromPageLoad){
		 
		var prov = $("#providerId").val();
		var cityIput =  $('#userCityInputId').val();
		var api  =  $('#apiKeyIdVal').val();
		
		 if ($("#wrongLocation").length > 0){
			 $("#wrongLocation").remove();
		 }
		
		//get the provider
		 if (prov !== 'noSelected' && cityIput.trim().length > 0){
			 
			 $('#currLocId').html("<option value='NoLoc ' selected > Loading </option>");
			 $.getJSON("fetchLocations", {provider :  prov, userInputCity : cityIput, apiKey: api }, function(data){			
			  		
					if(data != null && data.length > 0){
						var str = "";												
						for(var i = 0; i < data.length ; i++){
							str += "<option value='"+data[i].key + "' selected > "+ data[i].nameEn+ " </option>";
						}							
						 $('#currLocId').html(str);
					}	
					else
					{
						$('#currLocId').html("<option value='NoLoc' selected> No Location Found </option>");
					}
					
					if (fromPageLoad){
						var locFromLoadingPage = "<s:property value="weather.location" />";
						if (locFromLoadingPage.length > 0){
							//set the correct value in the select dropdown.
							$('#currLocId').val(locFromLoadingPage).change();
						}
					}
			  	}); 
		 }else{
			 
			 
		 }	 
		
		 //verify if we need to clear out error messages
		 verifyCityInput();
		 verifyProviderInput();		 
	 }; 
	 
	 function emptyCityLoc(){
		 $('#userCityInputId').val("");
		 $('#currLocId').html("<option value='NoLoc' selected> Please Select </option>");
		 if ($("#wrongProvider").length > 0){
			 $("#wrongProvider").remove();
		 }
		 if ($("#wrongLocation").length > 0){
			 $("#wrongLocation").remove();
		 }
		 
		showApiKey();
		 
	 }
	 
	 function verifyProviderInput(){
		 if ($("#providerId").val() === 'noSelected'){
			 if ($("#wrongProvider").length == 0){
			 	$("#providerId").before('<p class="alert alert-danger" id="wrongProvider">Please Select a provider.</p>');
			 }
			 return true;
		 }else{
			 $("#wrongProvider").remove();
		 }
		 return false;
	 }
	 
	 function verifyCityInput(){
		 if ( $('#userCityInputId').val().trim().length == 0){
			 if ($("#wrongCity").length == 0){
				 	$("#userCityInputId").before('<p class="alert alert-danger" id="wrongCity">Please enter a city name.</p>');
				 }
				 return true;
		 }else{
			 $("#wrongCity").remove();
		 }
		 return false;
	 }

	 function verifyLocationInput(){
		 if ( $('#currLocId').val() === 'NoLoc'){
			 if ($("#wrongLocation").length == 0){
				 	$("#currLocId").before('<p class="alert alert-danger" id="wrongLocation">Please Select a Location.</p>');
				 }
				 return true;
		 }else{
			 $("#wrongLocation").remove();
		 }
		 return false;
	 }
	 
	 
	 function removeWeather(){
		 
		 $(document.body).css({'cursor' : 'wait'});
		 
		 $.get( "saveWeather", function( data ) {
			 $(document.body).css({'cursor' : 'default'});
			  document.getElementById("loadWeatherId").click()
			});
	 
	 }
	 
	 function showApiKey(){
		 //show api if a certain provider is selected
		 if ($("#providerId").val() === 'WTHUndergrnd'){
			 $("#apiKeyId").show(300);
		 }else{
			 $("#apiKeyId").hide(300);
		 }
	 }
	 
	 </script>
	 
    <!-- /#wrapper -->
<!-- START Top Nav Menu -->
<jsp:include page="/jsp/footer.jsp" />
<!-- END Footer , End of file -->

