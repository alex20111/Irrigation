package net.project.thread;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.ArduinoManager;
import net.project.db.manager.WorkerManager;
import net.project.exception.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PowerSavingSched implements Runnable {
	
	private static Log log = LogFactory.getLog(PowerSavingSched.class);
	
	WorkerManager wm = new WorkerManager();
	
	@Override
	public void run() {		
		
		try{
			Calendar now = Calendar.getInstance();
			
			int hour = now.get(Calendar.HOUR_OF_DAY);
			int currMin = now.get(Calendar.MINUTE);			
					
			int timeInMin = (int)TimeUnit.HOURS.toMinutes(hour) + currMin;	
			log.debug("Running PowerSavingSched. Time used: "+ hour + ":" + currMin  + "  timeInMin " + timeInMin + "Curr date: " + new Date());
			
			List<Worker> workers = wm.loadAllWorkers(true, true, false);
			
			log.debug("Workers in power saving: " + workers);
			if (!workers.isEmpty()){
				for(Worker worker: workers){
					if (worker.getStartSleepTime() == timeInMin){
						
						//it's time to sleep.. Send code
						try{
							//the command sent will remove 5 minute from the time.							
							log.debug("Putting worker: " + worker.getWorkId() );
							
							new ArduinoManager().putWorkerToSleep(worker, "Worker starting power saving mode.");
						}catch (ValidationException ve){
							WorkerStatus ws = worker.getStatus();
							ws.setSystemComment("Power saving mode Exception: " + ve.getMessage());
							ws.setConnected(false);
							ws.setErrorDetected(true);
							
							wm.updateWorker(worker, true);
						}
						
						
					}else if(worker.getStopSleepTime() == timeInMin){
						log.debug("Checking if worker: " + worker.getWorkId() + " is awake");
						
						Worker awakeWorker = wm.loadWorkerById(worker.getWorkId(), true, false);
						
						if (awakeWorker.getStatus().isSleeping()){
							//if we get a sleeping status when it should be awake.. throw an error.
							log.info("Worker is still asleep when it should be awake, Check worker if responsive. Worker: " + awakeWorker.getWorkId());
							
							WorkerStatus status = new WorkerStatus();//default object status will be used.
							status.setSystemComment("Worker still in power saving mode. Should be awake");
							status.setErrorDetected(true);
							status.setWorkerId(worker.getStatus().getWorkerId());
							
							wm.updateWorker(awakeWorker, true);							
						}						
					}
				}
			}			
		}catch(Throwable tr){
			log.error("Error in PowerSavingSched", tr);
		}
	}
	
//	private int calcTicker(int start, int end){
//		
//		if (start > end){
//			return (1440 - start +  end) / 30;
//		}else if (end > start){
//			return (end - start) / 30;
//		}
//		return -1;
//		
//	}

}
