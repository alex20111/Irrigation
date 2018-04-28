package net.project.db.manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.arduino.Arduino;
import net.project.arduino.Cmd;
import net.project.common.Constants;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.exception.ValidationException;

public class ArduinoManager {

	private Log log = LogFactory.getLog(ArduinoManager.class);
	private boolean openWaterFirstTry = true;
	private boolean closeWaterFirstTry = true;
	private boolean getSensorFirstTry = true;
	private boolean checkWorkerFirstTry = true;
	private boolean sleepFirstTry		= true;

	/**
	 * 
	 * @param worker
	 * @return
	 * @throws ValidationException
	 * @throws SQLException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException 
	 */
	public Worker openWater(Worker worker) throws ValidationException, SQLException, IllegalStateException, IOException, InterruptedException, ClassNotFoundException{

		if (!worker.getStatus().isSleeping()){ 
			if (!worker.isDoNotWater()){

				Cmd cmdSend = new Cmd();
				cmdSend.buildCommand(Cmd.OPEN_WATER, worker);

				Arduino.sendCommand(cmdSend, null);

				Cmd cmdMsg = Arduino.queue.poll(7, TimeUnit.SECONDS); //poll the queue for 5 seconds.		

				log.debug("openWater - Got answer from queue");

				//command receieved and no fatal error
				if (cmdMsg != null && cmdMsg.getFatalError().length() == 0){

					log.debug("openWater : CMD sent: " + cmdSend  + " Recieved: " + cmdMsg );

					if (cmdMsg.getCmdCode().equals(Cmd.OPEN_WATER) || cmdMsg.getCmdCode().equals(Cmd.RAIN_NO_ON)){

						WorkerManager wm = new WorkerManager(); //loading worker here since we need the original status and water time
						Worker w = wm.loadWorkerById(worker.getWorkId(), true, false);

						//write the status
						if (cmdMsg.getCmdCode().equals(Cmd.RAIN_NO_ON)){
							updateWorkerStatus(w, cmdMsg, Constants.onByScheduleRain);
						}else{
							updateWorkerStatus(w, cmdMsg, "Watering");
						}


						//if 999, it comes from turning the water manually on from the dashboard.. keep that timer
						if (worker.getWaterElapseTime() == 999){
							w.setWaterElapseTime(999);
						}

						return w;
					}else{
						log.error("Unknow command code in openWater: " + cmdMsg);
					}	
				}else if ( openWaterFirstTry  &&  ( cmdMsg == null  ||  cmdMsg != null && cmdMsg.getFatalError().length() > 0 )  ){

					log.debug("Fatal error, trying to turn on water. one more time: " + (cmdMsg == null ? "Null cmdMsg" : cmdMsg.getFatalError() ) );

					openWaterFirstTry = false;

					//error --> call the open Water one more time to see if we can get through
					openWater(worker);			

				}else{
					throw new ValidationException("Timeout did not received answer from worker");
				}
			}
			else
			{
				throw new ValidationException("Worker override on , cannot water");
			}
		}
		return null;
	}

	public Worker closeWater(Worker worker) throws ValidationException, SQLException, IllegalStateException, IOException, InterruptedException, ClassNotFoundException{

		if (!worker.getStatus().isSleeping()){ 
			if (!worker.isDoNotWater()){
				Cmd cmdSend = new Cmd();
				cmdSend.buildCommand(Cmd.CLOST_WATER, worker);

				Arduino.sendCommand(cmdSend, null);

				Cmd cmdMsg = Arduino.queue.poll(7, TimeUnit.SECONDS); //poll the queue for 5 seconds.		

				if (cmdMsg != null && cmdMsg.getFatalError().length() == 0){

					if (cmdMsg.getCmdCode().equals(Cmd.CLOST_WATER)){

						//write the status

						updateWorkerStatus(worker, cmdMsg, "Water turned off");

						return worker;

					}else{
						log.error("Unknow command code in closeWater: " + cmdMsg);
					}	
				}else if  ( closeWaterFirstTry  &&  ( cmdMsg == null  ||  cmdMsg != null && cmdMsg.getFatalError().length() > 0 )  ){

					log.debug("Fatal error, trying to turn off water. one more time: " + (cmdMsg == null ? "Null cmdMsg" : cmdMsg.getFatalError() ) );

					closeWaterFirstTry = false;

					//error --> call the open Water one more time to see if we can get through
					closeWater(worker);			

				}else{
					throw new ValidationException("Timeout did not received answer from worker");
				}
			}
			else
			{
				throw new ValidationException("Worker override on , cannot water");
			}
		}
		return null;
	}

	public boolean getSensorData(Worker worker) throws ValidationException, SQLException, IllegalStateException, IOException, InterruptedException, ClassNotFoundException{

		if (!worker.getStatus().isSleeping()){ 
			Cmd cmdSend = new Cmd();
			cmdSend.buildCommand(Cmd.GET_SENSOR_DATA, worker);

			Arduino.sendCommand(cmdSend, null);

			Cmd cmdMsg = Arduino.queue.poll(7, TimeUnit.SECONDS); //poll the queue for 5 seconds.		

			if (cmdMsg != null && cmdMsg.getFatalError().length() == 0){

				if (cmdMsg.getCmdCode().equals(Cmd.GET_SENSOR_DATA)){

					updateWorkerStatus(worker, cmdMsg, "Gathering sensor data from worker");
					return true;

				}else{
					throw new ValidationException("Recieved wrong code from worker");
				}	
			}else if ( getSensorFirstTry  &&  ( cmdMsg == null  ||  cmdMsg != null && cmdMsg.getFatalError().length() > 0 )  ){

				log.debug("Fatal error, trying to get Sensor data. one more time: " + (cmdMsg == null ? "Null cmdMsg" : cmdMsg.getFatalError() ) );

				getSensorFirstTry = false;

				//error --> call the open Water one more time to see if we can get through
				getSensorData(worker);		
			}		
			else{
				throw new ValidationException("Timeout did not received answer from worker");
			}
		}
		return false;	
	}

	public void checkWorkerStatus(Worker worker, String msg) throws ValidationException, SQLException, IllegalStateException, IOException, InterruptedException, ClassNotFoundException{

		if (!worker.getStatus().isSleeping()){ //if not sleeping check.. 
			Cmd cmdSend = new Cmd();
			cmdSend.buildCommand(Cmd.GET_SENSOR_DATA, worker);

			Arduino.sendCommand(cmdSend, null);

			Cmd cmdMsg = Arduino.queue.poll(10, TimeUnit.SECONDS); //poll the queue for 5 seconds.		

			if (cmdMsg != null && cmdMsg.getFatalError().length() == 0){

				if (cmdMsg.getCmdCode().equals(Cmd.GET_SENSOR_DATA)){ 
					//add info to DB
					updateWorkerStatus(worker, cmdMsg, msg);

				}else{
					throw new ValidationException("Recieved wrong code from worker");
				}	
			}else if ( checkWorkerFirstTry  &&  ( cmdMsg == null  ||  cmdMsg != null && cmdMsg.getFatalError().length() > 0 )  ){

				log.debug("Fatal error, trying to check status. one more time: " + (cmdMsg == null ? "Null cmdMsg" : cmdMsg.getFatalError() ) );

				checkWorkerFirstTry = false;

				//error --> call the open Water one more time to see if we can get through
				checkWorkerStatus(worker, msg);
			}else{
				throw new ValidationException("Timeout did not received answer from worker");
			}	
		}
	}
	public void putWorkerToSleep(Worker worker, String msg) throws ValidationException, SQLException, IllegalStateException, IOException, InterruptedException, ClassNotFoundException{

		if (!worker.getStatus().isSleeping()){ //if not sleeping put it to sleep 
			Cmd cmdSend = new Cmd();
			cmdSend.buildCommand(Cmd.SLEEP, worker);

			Arduino.sendCommand(cmdSend, null);

			Cmd cmdMsg = Arduino.queue.poll(10, TimeUnit.SECONDS); //poll the queue for 5 seconds.		

			if (cmdMsg != null && cmdMsg.getFatalError().length() == 0){

				if (cmdMsg.getCmdCode().equals(Cmd.SLEEP)){ 
					//add info to DB
					cmdMsg.setSleeping(true);
									
					updateWorkerStatus(worker, cmdMsg, msg);

				}else{
					throw new ValidationException("Recieved wrong code from worker");
				}	
			}else if ( sleepFirstTry  &&  ( cmdMsg == null  ||  cmdMsg != null && cmdMsg.getFatalError().length() > 0 )  ){

				log.debug("Fatal error, trying to put the worker to sleep. one more time: " + (cmdMsg == null ? "Null cmdMsg" : cmdMsg.getFatalError() ) );

				sleepFirstTry = false;

				//error --> call the open Water one more time to see if we can get through
				putWorkerToSleep(worker, msg);
			}else{
				throw new ValidationException("Timeout did not received answer from worker");
			}	
		}else{
			throw new ValidationException("Worker Already sleeping");
		}
	}
	
	

	private Worker updateWorkerStatus(Worker worker, Cmd cmdMsg, String msg) throws SQLException, ValidationException, ClassNotFoundException{
		WorkerStatus status = worker.getStatus();
		status.setConnected(true);
		status.setLightStatus(cmdMsg.getLight());
		status.setRainStatus(cmdMsg.getRain());
		status.setRecordedDate(new Date());
		status.setBatteryLevel(cmdMsg.getBatteryPercent());
		status.setErrorDetected(false);
		if (cmdMsg.getWatering() == 1){
			status.setWorkerWatering(true);
		}else{
			status.setWorkerWatering(false);
		}		
		status.setSleeping(cmdMsg.isSleeping());
		
		status.setSystemComment(msg);

		worker.setStatus(status);

		WorkerManager wm = new WorkerManager();
		wm.updateWorker(worker, true);
		//

		return worker;
	}
}
