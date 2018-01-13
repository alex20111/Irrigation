package net.project.common;

import java.text.SimpleDateFormat;

import net.project.db.entities.Worker;
/*
* Main page result builder.
*/
public class ResultBuilder {


	
	private String body = "";
	private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");	
	public String buildHtmlResult(){
		
		return body;
	}
	
public  ResultBuilder(Worker worker){
	StringBuilder body = new StringBuilder();
	

	body.append("<div class=\"panel-heading\" style=\"background-color: #337ab6;color:white;\">");
	body.append("<div class=\"row\">");
	body.append("<div class=\"col-xs-9 \">");
	body.append("<h5><strong>" + worker.getName() +  "</strong></h5>");  // Name of worker
	body.append("</div>");
	body.append("<div class=\"col-xs-3 \">");		
	body.append("<div class=\"pull-right\">");
	
	//battery
	if (worker.getStatus().getBatteryStatus() != null){ //we have power
		String faIcon = worker.getStatus().getBatteryStatus().getFaIcon();
		int percent = Double.valueOf(worker.getStatus().getBatteryLevel()).intValue();
		
		body.append("<i class=\"fa " + faIcon + "\" style=\"padding-right:4px;cursor:pointer;\""
				  + "tabindex=\"0\" data-toggle=\"popover\" data-trigger=\"focus\" data-content=\"" + percent + "%\" data-placement=\"top\"  ></i>"); 
	}
	
	
	//signal .
	if (worker.getStatus().isConnected()){
		body.append("<i class=\"fa fa-signal\"></i>");  //signal type if connected or not.
	}else{		
		body.append("<i class=\"fa fa-signal\" id=\"parentContainer\">");  //not connected
		body.append("<i class=\"fa fa-ban\" id=\"nestedWifi\"></i>");
		body.append("</i>");		
	}
	body.append("</div>");
	body.append("</div>");
	body.append("</div>");
	body.append("<div class=\"row\">");
	body.append("<div class=\"col-xs-12 \">");
	body.append("<p class=\"text-right\"><small>" + sdf.format(worker.getStatus().getRecordedDate()) +  "</small></p>");		 //Recorded date 				
	body.append("</div>");	
	body.append("</div>");
	body.append("</div>");
	body.append("<div class=\"panel-body\"  style=\"padding-bottom:5px\" >");	
	body.append("<div class=\"row\">");		
	body.append("<div class=\"col-xs-4\">");
	//
	//icon type
	//
	String text = "";
	if (worker.getStatus().isErrorDetected()){
		body.append("<img src=\"images/error.png\"/>");
		text = "Problem: See alerts/info log page <br/> Last Status: " + worker.getStatus().getSystemComment();
	}else if (worker.getStatus().isSleeping()){	
		String time = "N/A";
		if (worker.getStopSleepTime() > 0){
			int h = worker.getStopSleepTime() / 60;
			int m = worker.getStopSleepTime() % 60;

			time = String.valueOf(h) + ":" +  (m < 10 ? "0" + m : m);	
		}
		body.append("<img src=\"images/sleeping.png\"/>");			
		text = "Worker Power Saving.<br/>Try at " + time;
	}
	else if (worker.isDoNotWater()){
		body.append("<img src=\"images/denied.png\"/>");			
		text = "Overide on.<br/>Do not water";
	
	}else if (worker.getStatus().isWorkerWatering() ){
		body.append("<img src=\"images/watering.png\"/>");
		text = "Watering";
		
	}else if (!worker.getStatus().isWorkerWatering() && 
			  !worker.getStatus().getSystemComment().equalsIgnoreCase(Constants.onByScheduleRain) && 
			  !worker.getStatus().getSystemComment().equalsIgnoreCase(Constants.onManuallyRain)){
		body.append("<img src=\"images/sleeping.png\"/>");
		
		if (worker.isScheduleRunning()){
			text = "Next Schedule:<br/>" + sdf.format(worker.getNextWateringBySched()) ;				
		}else{
			text = "Not watering";
		}	
	}else if (!worker.getStatus().isWorkerWatering() &&
			( worker.getStatus().getSystemComment().equalsIgnoreCase(Constants.onByScheduleRain) ||
			  worker.getStatus().getSystemComment().equalsIgnoreCase(Constants.onManuallyRain) )){
		body.append("<img src=\"images/rain.png\"/>");
		text = "Raining.<br/>Watering stopped";			
	}		
	
	body.append("</div>");
	body.append(" <div class=\"col-xs-8 \">");
	
	//Text beside the icon.. ie: not watering, next schedule , raining.......
	body.append("<div style=\"margin-top: 20px\">"+ text+"</div>"); // next schedule
	
	body.append("</div>");
	body.append("</div>");
	body.append("</div>");
	
	this.body = body.toString();
		
	}
	
	
	


	
	
	
}
