package net.project.thread;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Constants;
import net.project.common.SendEmail;
import net.project.common.Utils;
import net.project.db.entities.Config;
import net.project.db.entities.Weather;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.ArduinoManager;
import net.project.db.manager.ConfigManager;
import net.project.db.manager.WeatherManager;
import net.project.db.manager.WorkerManager;
import net.project.enums.LightLevel;
import net.project.enums.WeatherStatus;
import net.project.exception.ValidationException;
import net.project.scheduler.ScheduleManager;


public class SchedThread implements Runnable {

	private Log log = LogFactory.getLog(getClass());

	public String workerId = "000";
	private boolean restart = true;
	public boolean running = false;
	
	private WorkerManager mn = null;
	private ConfigManager cf = null;
	private ArduinoManager amn = null;
	
	private Worker worker = null;
	private Config config;
	
	private boolean turnOnPrevented = false; //if a n error occur in the turn on water.. do not sleep do not check worker since the error will already be generated
	
	public SchedThread(String workerId){
		this.workerId = workerId;
	}

	@Override
	public void run() {
		
		running = true; // tell the scheduler that the job is running
		
		log.debug("----Start of the sched thread loop----  " + new Date());
		mn = new WorkerManager();
		cf = new ConfigManager();		
		amn = new ArduinoManager();	
	
		
		try{			

			try {	
				worker = mn.loadWorkerById(workerId, true, true);
				config = cf.loadConfig();

				//do not turn on water if already watering
				if (worker.getStatus() != null && !worker.getStatus().isWorkerWatering())
				{	
					turnOnWater();
				}
				//if we are watering , sleep until the end.
				if (worker.getStatus() != null && worker.getStatus().isWorkerWatering()){
					//wait
					try {
						log.debug("Sleeping: " + (worker.getWaterElapseTime() * 60000 ) + "  " +worker.getWaterElapseTime());
						Thread.sleep(worker.getWaterElapseTime() * 60000);

					} catch (InterruptedException e) {}
				}

				if (!turnOnPrevented){ //if nothing prevented to turn on the water, then try to shut it down.
					config = cf.loadConfig();
					//check if operator has already turned off the water.
					worker = mn.loadWorkerById(workerId, true, false);

					log.debug("woke up, water status: " + worker.getStatus().isWorkerWatering());

					if (worker.getStatus().isWorkerWatering()){
						turnOffwater();
					}else { //if water is off and no errors from the turnOnWater() method, check the worker.					
						//if water is already off, check the worker. Ex: water manually turned off while thread was sleeping.					
						checkWorker("Scheduled finished. Water was already turned off. Checking worker");
					}
				}
				
			} catch (Exception e) {
				log.error("Error in scheduled thread" , e);
				Utils.sendErrorMessage(e); //send error message if requested.

			}finally{
				log.debug("restart Schedule thread: " + restart);
				if (restart){
					long delay = ScheduleManager.newSchedule(worker,  false);
					//re-save the delay
					worker.setNextWateringBySched(new Date(new Date().getTime() + delay));
					mn.updateWorker(worker, false);
				}				
			}
		}catch(Throwable tr){
			log.error("Fatal error in scheduled thread" , tr);
			Utils.sendErrorMessage(tr); //send error message if requested.
		}
		
		running = false; // tell the scheduler that the job has finished executing
	}

	private void turnOnWater() throws Exception{

		boolean water = true;

		log.debug("Turning water on... " + new Date() + " Worker id: " + workerId + " Is override on? " + worker.isDoNotWater() + "  weather: " 
				+ ( worker.getWeatherWrk() != null && worker.getWeatherWrk().isActive() ? " Weather active ": " Do not check weather" )) ;

		//CHECK if we can water
		//things that can prevent watering: Override - Power saving - Weather
		
		if (worker.isDoNotWater()){//check override			
			log.info("tried to water but the override is on. Worker id: " + worker.getWorkId());
			
			water = false;
			checkWorker("Override is on. No watering");
		}else if (worker.getStatus().isSleeping()){
			log.info("tried to water but the worker is in power saving mode. Worker id: " + worker.getWorkId());
			water = false;
		}else if (worker.getWeatherWrk() != null && worker.getWeatherWrk().isActive()){ //check for weather activated.

			log.debug("Weather active : Worker id: " + worker.getWorkId() );
			WeatherManager wthMan = new WeatherManager();
			Weather wth = wthMan.loadWeatherInformation(false);

			log.debug("Weather active : Worker id: " + worker.getWorkId() + " Weather info: " + wth);

			if (wth != null && wth.isActive()){
				wth = wthMan.loadCurrentWeatherFromWeb(wth);				

				if (wth != null && wth.isRaining()){
					water = false;
					log.debug("It is currently raining. Worker id: " + worker.getWorkId());
					checkWorker(Constants.onByScheduleRain);

					if(config.isActivateWarning() && config.isNotiRainNotWatering()){
						sendEmailNotWateringRain();
					}
				}
			}
		}			

		if(water){ 	

			try{

				worker = amn.openWater(worker);

			}catch(ValidationException ve){
				String msg = "";
				if ("Timeout did not received answer from worker".equals(ve.getMessage())){					
					log.info("Turning on water. Cannot contact worker, Will try  next window in the schedule. Worker id: " + worker.getWorkId());
				}else if(ve.getMessage().startsWith("Unknow command code in openWater")){
					msg = "Error , see logs";
					log.info("Unknow command: " + ve.getMessage() + " With worker id: " + worker.getWorkId());
				}	
				updateDb(msg, false);

				//reset the elapsed time to bypass the sleep thread.
				turnOnPrevented = true;
				
				if (config.isActivateWarning() && config.isNotiErrors()){
					msg = "Cannot connect to the worker to turn on water  Worker id: " + worker.getWorkId();
					SendEmail.send("Timeout from Worker", msg, config);
				}			
			}
		}else{
			turnOnPrevented = true;			
		}

	}
	
	private void turnOffwater() throws Exception{

		log.debug("Turning water off... " + new Date() + " Worker id: " + workerId);	
		if(!worker.isDoNotWater()){
			try{
				amn.closeWater(worker);

			}catch(ValidationException ve){
				String msg = "";
				if ("Timeout did not received answer from worker".equals(ve.getMessage())){
					msg = "Cannot connect to the worker to close the valve on Worker id: " + worker.getWorkId();
					if (config.isActivateWarning()){
						SendEmail.send("Worker not active", msg, config);
					}
					log.info("Turning OFF water. Cannot contact worker , Will try  next window in the schedule.Worker id: " + worker.getWorkId());
				}else if(ve.getMessage().startsWith("Unknow command code in closeWater")){
					msg = "Error , see logs";
					log.info("Unknow command: " + ve.getMessage() + " With worker id: " + worker.getWorkId());
				}	
				updateDb(msg, false);
			}
		}
		else
		{
			log.info("tried to turn off the water but the override is on. Worker id: " + worker.getWorkId());
			checkWorker("Override is on. Won't turn off");
		}


	}
	
	private void checkWorker(String msg) throws SQLException, IllegalStateException, IOException, InterruptedException, ValidationException, ClassNotFoundException{
		//still go check on the worker to see what caused the water to turn off.(could have been done manually)
		try{
			amn.checkWorkerStatus(worker, msg);
		}catch(ValidationException ve){
			//error for that worker
			log.info("Cannot contact worker in VersionException: " + worker.getWorkId());
			updateDb( "Exception for worker : " + worker.getWorkId() + ". Msg: " + ve.getMessage(), false);
		}
	}
	
	//value that specify that after the schedule has ran a restart is needed.
	public void setRestart(boolean restart) {
		this.restart = restart;
	}
	
	private void updateDb(String msg, boolean connected) throws SQLException, ValidationException, ClassNotFoundException{
		WorkerStatus ws = worker.getStatus();
		ws.setConnected(connected);
		ws.setRecordedDate(new Date());
		ws.setLightStatus(LightLevel.NA);
		ws.setRainStatus(WeatherStatus.NA);
		ws.setWorkerWatering(false);
		ws.setBatteryLevel(-1.0);
		ws.setSystemComment(msg);		
		
		mn.updateWorker(worker, true);
	}
	
	private void sendEmailNotWateringRain(){
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<body>");
		sb.append("<p> <strong>Worker: </strong>" +  worker.getWorkId() + " Not Watering because it's raining </p>");
		sb.append("<p> Have a nice Day! </p>");
		sb.append("</body>");
		sb.append("</html>");
		
		try {
			SendEmail.send("Worker: " + worker.getWorkId() + " Not Watering, raining",sb.toString(), config);
		} catch (Exception e) {
			log.error("Error sending mail notTurnOnBecauseOfRain: " , e);
		}
	}

}
