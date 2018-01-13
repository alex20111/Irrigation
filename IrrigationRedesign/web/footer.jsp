<%@ taglib prefix="s" uri="/struts-tags" %>

	<script src="js/prettydate.min.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="js/bootstrap.min.js"></script>

    <!-- Metis Menu Plugin JavaScript -->
    <script src="js/metisMenu.min.js"></script>

    <!-- Custom Theme JavaScript -->
    <script src="js/sb-admin-2.js"></script>
    
    <!-- Custom javascript -->
    <script src="js/buttons-cst.js"></script>
    
    <script>
    	$(document).ready(function() {
	      
	      <s:if test="#session.user != null">   
	      
	    	var interval = 20000; // interval for events

	    	//1st call the query right after 1 seconds. (Events)
	    	setTimeout(ajax_call, 1000);
	    	
	    	//then call it every 20 seconds.
	    	setInterval(ajax_call, interval);
	      </s:if>
	      
	      //remove the alert from the top nav
	      $("#alertTopMenuId").click(function(){
	    	  if ($("#bellId").css('color') == "rgb(255, 0, 0)")
	    		  {
	    		  	$('#bellId').css('color', 'rgb(90, 122, 182)');	
	    		  	sessionStorage.setItem("status", "noAlert"); //remove alert marker	    		 
	    		  }
	    	  
	      });     
	      
	      //put the read for alert if still in alert.
	      if (typeof(Storage) !== "undefined") {
	    	  var stat = sessionStorage.getItem("status");
	    	  if (stat != null && stat === 'inAlert' ){
	    		  $('#bellId').css('color', 'red');
	    	  }	    		  
	      }	   
	    });
	    
    	//Function to search for new events
	    var ajax_call = function() {
	  	  
	  	  var stringText = "";	  	  
	  	  
		  	$.getJSON("eventAjax", {}, function(data){			
		  		
				if(data != null && data.length > 0){							
					for(var i = 0; i < data.length; i++){
						//only keep 5 new events.
						if (i < 5){
	
							stringText += '<li> <a href="#"><div>'+
	                                				'<i class="fa fa-comment fa-fw"></i> '  + data[i].event +
	                                				'<span class="pull-right text-muted small prettydate"> ' + data[i].eventDate + '</span>' +
	                            				'</div></a>' +
	                    					'</li>' +
	                    				'<li class="divider"></li>';
						}else{
							break;
						}
					}
					
					//store event in session
					if (typeof(Storage) !== "undefined") {
						var currItem = data.length;
						var prevItem = sessionStorage.getItem("prevItems");

					    // Store
					    sessionStorage.setItem("curritems", String(currItem));

					    if (prevItem != null && currItem > parseInt(prevItem)){
	
					    	$('#bellId').css('color', 'red');			
					    	sessionStorage.setItem("prevItems", String(currItem));
					    	sessionStorage.setItem("status", "inAlert");
					    }else if(prevItem == null || "rst" === prevItem || "null" === prevItem){
					    	sessionStorage.setItem("prevItems", String(currItem));
		
					    	$('#bellId').css('color', 'red');
					    	sessionStorage.setItem("status", "inAlert");
					    }
					}			
					
				}else{
						stringText = '<li> <a href="#"> <div>' +
                             '<i class="fa fa-comment fa-fw"></i> No new events' +
                             '<span class="pull-right text-muted small">now</span>' +
                             '</div> </a></li>' +
                 			 '<li class="divider"></li>'; 
				}	
				
				stringText += '<li>' +
        		 '<a href="loadEvents" title="Events" class="text-center" onclick="resetEvents()" id="viewAllEventsId" >' +
             	 '<strong>See All Alerts</strong>' +
             	 '<i class="fa fa-angle-right"></i>'+
         		 '</a> </li>';
				
				$("#eventsTopId").html(stringText);
				
				$(".prettydate").prettydate();				
		  	});
	  	};
    
	  	 function resetEvents(){
	    	  sessionStorage.setItem("prevItems", "rst");	    	  
	      };
    
	    function toggleHelpText(id){			
			 $("#" + id).toggle(300);
		}    
    
	    //for form to only submit the form once
	    $('form.submit-once').submit(function(e){
	  	  if( $(this).hasClass('form-submitted') ){
	  	    e.preventDefault();
	  	    return;
	  	  }
	  	  $(this).addClass('form-submitted');
	  	});
	    
	    $('.link-submit-once').click(function(e){
			 if( $(this).hasClass('link-submitted') ){
		  		   e.preventDefault();
		  	    return;
		  	  }
		  	  $(this).addClass('link-submitted');
		 });
	    
	    //check if textfield is numeric
	    function checkNumeric(id){
	    	if ($.isNumeric( $("#" + id).val() )){
	    		
	    		var nbr = parseFloat($("#" + id).val());
	    		if(!isNaN(nbr)){
	    			$("#"+id).val(nbr);
	    			$("#wrongNumber"+id).remove();
	    			return true;
	    		}
	    	}
	    	
	    	$("#"+id).val("");
	    	if($("#wrongNumber" + id).length == 0) {
	    		$("#"+id).before('<p class="alert alert-danger" id="wrongNumber' + id + '">Please enter a numeric value.</p>');
	    	}
	    }	    
    </script>

</body>

</html>