package net.project.thread;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Utils;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.ArduinoManager;
import net.project.db.manager.WorkerManager;
import net.project.enums.LightLevel;
import net.project.enums.WeatherStatus;
import net.project.exception.ValidationException;

/**
 * Thread that will verify if a worker is responsive.
 * @author
 */
public class CheckWorkers implements Runnable {

	private Log log = LogFactory.getLog(CheckWorkers.class);
	
	public boolean jobDone = false;
	
	@Override
	public void run() {

		log.debug("Checking connected workers");
		WorkerManager wm = new WorkerManager();
		ArduinoManager am = new ArduinoManager();
		try {

			List<Worker> workers = wm.loadAllWorkers(true, true, false);

			if (workers.size() > 0){
				log.debug("We have workers, checking them... Wow");
				
				for(Worker w : workers){
					try{
						am.checkWorkerStatus(w, "Auto verifying Worker status:  Worker Found.\nUpdating data.");										
					}catch(ValidationException ve){
						//error for that worker
						updateDb(w,wm, "Exception for worker : " + w.getWorkId() + ". Msg: " + ve.getMessage());
					}
				}
			}
		} catch (Exception ex){
			log.error("Error in CheckWorkers.", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		
		jobDone = true;
	}
	
	private void updateDb(Worker w, WorkerManager wm, String msg) throws SQLException, ValidationException{
		WorkerStatus ws = w.getStatus();
		ws.setConnected(false);
		ws.setRecordedDate(new Date());
		ws.setLightStatus(LightLevel.NA);
		ws.setRainStatus(WeatherStatus.NA);
		ws.setWorkerWatering(false);
		ws.setBatteryLevel(0.0);
		ws.setSystemComment(msg);	
		ws.setErrorDetected(true);		
		
		wm.updateWorker(w, true);
	}
}
