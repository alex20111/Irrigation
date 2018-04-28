package net.project.web.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.project.common.Constants;
import net.project.common.Event;
import net.project.common.ResultBuilder;
import net.project.common.Utils;
import net.project.db.entities.User;
import net.project.db.entities.Weather;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.ArduinoManager;
import net.project.db.manager.WeatherManager;
import net.project.db.manager.WorkerManager;
import net.project.enums.LightLevel;
import net.project.enums.WeatherStatus;
import net.project.exception.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;


import com.opensymphony.xwork2.ActionSupport;

public class MainPageAction extends ActionSupport implements SessionAware {
	private Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	
	private List<Event> events;
	private InputStream waterStream; 
	private InputStream unmanagedStream;
	private InputStream workerStatusTxt;
	
	private Map<String, Object> session;
	
	
	private WorkerManager workerManager;
	private WeatherManager weatherManager;
	
	
	private List<Worker> workers = new ArrayList<Worker>();
	private Weather weather;
	
	private String workerId = "";
	
	
	public MainPageAction(){
		workerManager = new WorkerManager();
	}
	
	
	public String loadMainPageInformation(){
		
		try
		{
			//load current date into the user session
			workers = workerManager.loadAllWorkers(true, true, false);
			
			weatherManager = new WeatherManager();
			weather = weatherManager.loadWeatherInformation(false);
			
			
			//build result for the workers
			for (Worker w : workers)
			{
				w.setResultBody(new ResultBuilder(w).buildHtmlResult());		
			}		
			
		}catch(Exception ex){
			addActionError("Error while loading information, please see logs");
			log.error("Error in loadMainPageInformation.", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		return SUCCESS;
	}
	
	
	/**
	 * turn on and off the watering  through ajax.
	 * 
	 * @return
	 */
	public String turnWaterOnOff(){

		User user = (User) session.get(Constants.USER);

		try {
			if (user != null && user.canModify()){
				Worker worker = workerManager.loadWorkerById(workerId, true, false);	

				Worker updWork=null;

				if (worker != null){

					ArduinoManager amn = new ArduinoManager();

					if (!worker.isDoNotWater()){

						// send signal to the arduino to water.	
						if (worker.getStatus().isWorkerWatering()){
							try{
								updWork = amn.closeWater(worker);						
							}catch(ValidationException ve){
								workerUpdateDb(worker, "Problem when trying to turn off the water. Message: " + ve.getMessage());
							}
						}else{
							try{
								worker.setWaterElapseTime(999);
								updWork = amn.openWater(worker);

							}catch(ValidationException ve){

								workerUpdateDb(worker, "Problem when trying to start watering. Message: " + ve.getMessage());
							}
						}	
					}
				}			

				if(updWork != null){
					waterStream = new ByteArrayInputStream(new ResultBuilder(updWork).buildHtmlResult().getBytes("UTF-8"));
				}else{
					Worker worker2 = workerManager.loadWorkerById(workerId, true, false);
					waterStream = new ByteArrayInputStream(new ResultBuilder(worker2).buildHtmlResult().getBytes("UTF-8"));
				}
			}

		} catch (Exception e) {
			try {
				waterStream = new ByteArrayInputStream("Error".getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e1) {	}
			log.error("Error in turnWaterOnOff. " , e);
			Utils.sendErrorMessage(e); //send error message if requested.
		}

		return SUCCESS;
	}
	/**
	 * ajax call
	 * @return
	 */
	public String unmanagedWorkers(){
		try {			

			List<Worker> workers = workerManager.loadAllUnmanagedWorkers();

			StringBuilder text = new StringBuilder();

			if (workers.size() > 0){
				text.append("<ul class=\"list-group\">   <li class=\"list-group-item\"> ");
				text.append("<span class=\"badge\"> " +  workers.size() +  "</span> New Workers");
				text.append("</li> </ul>");
			}
			else
			{			
				text.append("<ul class=\"list-group\">   <li class=\"list-group-item\"> ");
				text.append("<span class=\"badge\">0</span> New Workers");
				text.append("</li> </ul>");
			}

			unmanagedStream = new ByteArrayInputStream(text.toString().getBytes("UTF-8"));

		} catch (Exception e) {
			try {
				unmanagedStream = new ByteArrayInputStream("Error !".getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e1) {	}
			log.error("Error in unmanagedWorkers. " , e);
			Utils.sendErrorMessage(e); //send error message if requested.
		}

		return SUCCESS;
	}
	
	/**
	 * detect that a new event has appear for that day. ajax call
	 * @return
	 */
	public String detectNewEvent(){		

		try {
			
			Calendar startDate = (Calendar)session.get("EventStartdate");		
			
			if (startDate == null){
				startDate = Calendar.getInstance();
				session.put("EventStartdate", startDate);
			}
			
			 events = new ArrayList<Event>();		
			
			if (startDate != null){				
				
				//to date put it at night
				Calendar end = Calendar.getInstance();
				end.set(Calendar.HOUR_OF_DAY, 23);
				end.set(Calendar.MINUTE, 59);
				end.set(Calendar.SECOND,59);	

				events = workerManager.loadAllEventsByDate(null, startDate.getTime(), end.getTime());
				
			}
						
			if (events.size() > 0){
			
				//get only the last 10 events.
				Collections.sort(events, new Comparator<Event>() {
					public int compare(Event o1, Event o2) {						
							return o1.getEventDate().compareTo(o2.getEventDate());
						
					}
				});
				Collections.reverse(events);
			}		
			
		} catch (Exception e) {
			log.error("Error in detectNewEvent. " , e);
			Utils.sendErrorMessage(e); //send error message if requested.
		}

		return SUCCESS;		
	}
	/**
	 * Refresh all workers or just 1 worker.
	 * @return
	 */
	public String refreshWorkers(){
		try {

			ArduinoManager am = new ArduinoManager();

			if (getWorkerId().length() == 0){
				workers = workerManager.loadAllWorkers(true, true, false);

				for(Worker worker : workers){
					try{
						am.checkWorkerStatus(worker,"Refreshing Worker: Worker found");

					}catch(ValidationException ve){
						//error for that worker
						workerUpdateDb(worker, "Exception for worker, Msg: " + ve.getMessage());
					}					

					worker.setResultBody(new ResultBuilder(worker).buildHtmlResult());
				}
			}
			else if (workerId.length() > 0){ //TODO this
				Worker worker = new Worker();
				worker.setWorkId("001");
				try{
					if (worker != null){
						am.checkWorkerStatus(worker, "Refreshing Worker: Worker found");					
					}
				}catch(ValidationException ve){
					//error for that worker
					workerUpdateDb(worker, "Exception for worker, Msg: " + ve.getMessage());
				}

				worker.setResultBody(new ResultBuilder(worker).buildHtmlResult());
			}	
		}
		catch (Exception e) {
			addActionError("Error while refreshing the workers, please see logs");
			log.error("Error in refreshWorkers. " , e);
			Utils.sendErrorMessage(e); //send error message if requested.
		}

		return SUCCESS;
	}
	public String displayCurrentWorkerStatus(){
		try{
			if (workerId != null && workerId.length() > 0){
				WorkerManager wm = new WorkerManager();

				Worker w = wm.loadWorkerById(workerId, true,false);

				WorkerStatus ws = w.getStatus();

				StringBuilder wssb = new StringBuilder();

				if (!ws.isWorkerWatering()){ //not watering , show stats

					wssb.append("<table class=\"table\">");
					wssb.append("<tr><td>Light</td>");
					wssb.append(" <td>" + ws.getLightStatus().getStatus() + "</td>");
					wssb.append("</tr>");

					//power saving
					wssb.append("<tr><td>Power Saving</td>");
					if (w.getStartSleepTime() > 0 && w.getStopSleepTime() > 0){
						wssb.append(" <td> Between " + getMinutesToHourMin(w.getStartSleepTime()) + " and "+ getMinutesToHourMin(w.getStopSleepTime()) + "</td>");
					}else{
						wssb.append(" <td>N/A</td>");
					}
					wssb.append("</tr>");

					wssb.append("<tr>");
					wssb.append(" <td>Schedule</td>");
					wssb.append(" <td>");

					if(w != null && w.isScheduleRunning()){				
						wssb.append("Running " + w.getSchedType().getDesc() + " at " + w.getSchedStartTime());
					}else{
						wssb.append("Stopped");
					}
					wssb.append(" </td>");
					wssb.append("</tr>");

					if (w != null && w.isScheduleRunning()){
						wssb.append("<tr>");
						wssb.append("<td>Duration</td>");
						wssb.append("<td>" + w.getWaterElapseTime() + " Minutes</td>");
						wssb.append("</tr>");
						wssb.append("<tr>");
						wssb.append("<td>Next schedule </td>");
						wssb.append("<td>start on " + new SimpleDateFormat("MMM dd HH:mm").format(w.getNextWateringBySched()) + " </td>");
						wssb.append("</tr>");
					}

					wssb.append("</table>");	
				}else if (ws.isWorkerWatering()){

					long minRemainingMillis = 0l;
					//verify if the schedule is set 

					if (w.isScheduleRunning()){ //TODO work by elapse time to display correct time
						//watering 
						Calendar endOfWatering = Calendar.getInstance();
						endOfWatering.setTime(w.getNextWateringBySched());
						endOfWatering.add(Calendar.MINUTE, w.getWaterElapseTime());	

						minRemainingMillis = endOfWatering.getTimeInMillis() - new Date().getTime();


					}else{
						minRemainingMillis = 59940000l;
					}

					if (minRemainingMillis > 0){

						String time = String.format("%02d:%02d:%02d",  TimeUnit.MILLISECONDS.toHours(minRemainingMillis),
								TimeUnit.MILLISECONDS.toMinutes(minRemainingMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(minRemainingMillis)),
								TimeUnit.MILLISECONDS.toSeconds(minRemainingMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(minRemainingMillis))  ) ;					

						wssb.append("<div class=\"text-center form-group\">");
						wssb.append("<i class=\"fa fa-clock-o fa-3x\"></i> <br/>");
						wssb.append(time);
						wssb.append("</div>");

						wssb.append("<table class=\"table\">");

						wssb.append("<tr><td>Light</td>");
						wssb.append(" <td>" + ws.getLightStatus().getStatus() + "</td>");
						wssb.append("</tr>");
						wssb.append("</table>");
					}

				}

				workerStatusTxt = new ByteArrayInputStream(wssb.toString().getBytes("UTF-8"));

			}
		}catch(Exception ex){
			log.error("Error fetching current worker status" , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return SUCCESS;
	}
	/**
	 * update worker on error.
	 * @param worker
	 * @param message
	 * @throws SQLException
	 * @throws ValidationException
	 * @throws ClassNotFoundException 
	 */
	private void workerUpdateDb(Worker worker, String message) throws SQLException, ValidationException, ClassNotFoundException{
		WorkerStatus ws = worker.getStatus();
		ws.setConnected(false);
		ws.setRecordedDate(new Date());
		ws.setLightStatus(LightLevel.NA);
		ws.setRainStatus(WeatherStatus.NA);
		ws.setBatteryLevel(0.0);
		ws.setSystemComment(message);	
		ws.setWorkerWatering(false);	
		ws.setErrorDetected(true);
		
		workerManager.updateWorker(worker, true);
	}
	
	private String getMinutesToHourMin(int min){
		String time = "N/A";
		
		if (min > 0){
		
			int h = min / 60;
			int m = min % 60;
	
			time = String.valueOf(h) + ":" +  (m < 10 ? "0" + m : m);
		}
		
		return time;
	}
	
	
	public List<Worker> getWorkers() {
		return workers;
	}
	public void setWorkers(List<Worker> workers) {
		this.workers = workers;
	}	
	public String getWorkerId() {
		return workerId;
	}
	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	public InputStream getWaterStream() {
		return waterStream;
	}
	public void setWaterStream(InputStream waterStream) {
		this.waterStream = waterStream;
	}
	public InputStream getUnmanagedStream() {
		return unmanagedStream;
	}
	public void setUnmanagedStream(InputStream unmanagedStream) {
		this.unmanagedStream = unmanagedStream;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	public Weather getWeather() {
		return weather;
	}
	public void setWeather(Weather weather) {
		this.weather = weather;
	}
	public InputStream getWorkerStatusTxt() {
		return workerStatusTxt;
	}
	public void setWorkerStatusTxt(InputStream workerStatusTxt) {
		this.workerStatusTxt = workerStatusTxt;
	}
}
