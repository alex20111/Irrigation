package net.project.arduino;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.project.common.Constants;
import net.project.common.SendEmail;
import net.project.common.Utils;
import net.project.db.entities.Config;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.ConfigManager;
import net.project.db.manager.WorkerManager;
import net.project.exception.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

public class Arduino {

	private static Log log = LogFactory.getLog(Arduino.class);

	public static BlockingQueue<Cmd> queue = new ArrayBlockingQueue<Cmd>(10);

	public static String START 				= "z";
	public static String END 				= "e";

	private static WorkerManager sql = null;
	private static ConfigManager cfManager = null;

	private static Serial serial;

	private static Map<String, Integer> retries = new HashMap<String, Integer>();

	/**
	 * This example program supports the following optional command arguments/options:
	 *   "--device (device-path)"                   [DEFAULT: /dev/ttyAMA0]
	 *   "--baud (baud-rate)"                       [DEFAULT: 38400]
	 *   "--data-bits (5|6|7|8)"                    [DEFAULT: 8]
	 *   "--parity (none|odd|even)"                 [DEFAULT: none]
	 *   "--stop-bits (1|2)"                        [DEFAULT: 1]
	 *   "--flow-control (none|hardware|software)"  [DEFAULT: none]
	 */
	public static void start(){
		//
		// Please see this blog article for instructions on how to disable the OS console for this port:
		// https://www.cube-controls.com/2015/11/02/disable-serial-port-terminal-output-on-raspbian/

		// create an instance of the serial communications class
		serial = SerialFactory.createInstance();

		sql = new WorkerManager();
		cfManager = new ConfigManager();

		// create and register the serial data listener
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				//see commands in CMD class

				try {
					log.info("Data recieved: " + event.getAsciiString());
					
					List<String> serialData = new ArrayList<String>();
					
					try{		
						 serialData = readSerialData(event.getAsciiString());	
					}catch (ValidationException ve){
						//if any validation exception received, try to re-send the command.
						//the validation exception will only happend when we send a command.
						String cmdMsg = ve.getMessage();
						boolean retry = canRetry(cmdMsg);
						log.debug("Exception in sending message? : " + cmdMsg + " Can retry: " + retry);
						
						if (retry){
						
							try{
								Thread.sleep(500);
							}catch(InterruptedException ie){}
	
							//if transmit failed , retry for 2 times
							sendCommand(null,getRetryCommand(cmdMsg));

						}else{
							//inform manager that it was unsuccessful.
							log.debug("No more retries: " + cmdMsg + " Can retry: " + retry + " sending message to manager.");
							Cmd c = new Cmd();
							c.setFatalError(cmdMsg);
							queue.put(c);
						}			
					}
					
					if (serialData.size() > 0){
						for(String data : serialData){
							processSerialData(data);
						}
					}					
					
				} catch (Exception e) {
					log.error("Error in dataReceived " , e);
				}
			}
		});

		try {
			Config cfg = cfManager.loadConfig();

			// create serial config object
			SerialConfig config = new SerialConfig();

			// set default serial settings (device, baud rate, flow control, etc)
			//
			// by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
			// NOTE: this utility method will determine the default serial port for the
			//       detected platform and board/model.  For all Raspberry Pi models
			//       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
			//       model 3B is will return "/dev/ttyS0".
			config.device(cfg.getSerialPort()) //TODO serial port refresh not working
			.baud(Baud._9600)
			.dataBits(DataBits._8)
			.parity(Parity.NONE)
			.stopBits(StopBits._1)
			.flowControl(FlowControl.NONE);


			// display connection details
			System.out.println(" Connecting to: " + config.toString());


			// open the default serial device/port with the configuration settings
			serial.open(config); 			
		}
		catch(Exception ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			log.error("Error in start()", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
	}

	public static void stop() throws IllegalStateException, IOException{
		if (serial != null && serial.isOpen()){
			serial.close();
		}
	}
	public static void restart() throws IllegalStateException, IOException{
		if (serial != null){

			if (serial.isOpen()){
				serial.close();
			}

			//if the serial is closed, restart.
			if (serial.isClosed()){
				start();
			}
		}

	}
	/**
	 * Command length = 6
	 * @param cmd
	 * @param workerId
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	public static synchronized void sendCommand(Cmd cmd, String retryCommand) throws IllegalStateException, IOException{

		String command = "";
		
		if(cmd != null){
			command =  START + cmd.getCommand() + END;
		}else if (retryCommand != null){
			command =  retryCommand;
		}

		log.debug("sending command: " + command );
		// write a simple string to the serial transmit buffer
		serial.write(command);
		try {
			Thread.sleep(100);//to flush the data
		} catch (InterruptedException e) {} 
	}
	/**
	 * Pos 0 = Command
	 * Pos 1 = Sending code (always)
	 * Pos 2 = random letter for signal confirmation. To know that you receive the right information
	 * Pos 3,4,5 = worker id
	 * Pos 6 = data
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */	 
	public static synchronized List<String> readSerialData(String data) throws ValidationException, IllegalStateException, IOException{
		

		List<String> returnData = new ArrayList<String>();
//		StringBuilder sb = new StringBuilder();
		String processData = data.trim();		

		log.debug("Reading serial data : " + data + " TRIMED: " + processData);
		
		if (processData.indexOf(START) != -1 && processData.indexOf(END) != -1 
				&&  !processData.startsWith("failed") && !processData.startsWith("Incomplete") && processData.indexOf(":") == -1 ){
			//remove the start and end delimiters	
//			sb.append(processData.substring(1, processData.length() - 1));
			String[] cut = processData.split(END);
			
			for(String s : cut){
				returnData.add(s.substring(s.indexOf(START) + 1));
			}
			log.debug("Reading serial data  returnData: " + returnData);
		}else if (processData != null && processData.length() > 0 && processData.startsWith("failed")){
			log.debug("Failed, throw validation exception");
			addRetries(processData);
			throw new ValidationException(processData);
		}else if (processData != null && processData.length() > 0 && processData.startsWith("Incomplete")){
			addRetries(processData);
			throw new ValidationException(processData);
		}else{			
			throw new IllegalStateException("unknown exception in readSerialData: processData :  " + processData ) ;
		}				

		return returnData;			
	}
	private static synchronized void processSerialData(String dataReceived) throws IllegalStateException, IOException, ValidationException, InterruptedException, SQLException, ClassNotFoundException{

		Config cfg = null;	


		Cmd commandData = new Cmd(dataReceived); 							
		if (commandData.isConfirmation()){ //mean that the controller has sent a message and this is the confirmation.
			log.debug("isConfirmation " + commandData.getWorkerId());
			//put the message into the queue
			queue.put(commandData);
		}else if (commandData.isSelfIdentification()){
			log.debug("Self identification start " + commandData.getWorkerId());
			//register it to the DB						
			Worker w = new Worker();
			w.setWorkId(commandData.getWorkerId());
			w.setManaged(false);						
			Worker dbw = sql.loadWorkerById(w.getWorkId(), true, false);

			if (dbw == null){ //verify if worker exist 1st.
				log.debug("Self identification writing to db " + commandData.getWorkerId());			

				//add the status
				WorkerStatus status = new WorkerStatus();
				status.setConnected(true);
				status.setRecordedDate(new Date());
				status.setLightStatus(commandData.getLight());
				status.setRainStatus(commandData.getRain());
				status.setWorkerId(commandData.getWorkerId());
				if (commandData.getWatering() == 1){
					status.setWorkerWatering(true);
				}else{
					status.setWorkerWatering(false);
				}
				status.setBatteryLevel(commandData.getBatteryPercent());
				status.setSystemComment("New Worker self identified" );

				w.setStatus(status);
				sql.addWorker(w, true);
			}else{

				boolean watering = (commandData.getWatering() == 1 ? true : false);													

				updStatus(dbw, watering, commandData, "Worker self identified" );
			}
			Cmd sendCmd = new Cmd();
			sendCmd.buildCommand(Cmd.SELF_IDENTIFY, w);
			//tell the worker that it has been acknowledged 
			sendCommand(sendCmd, null);

			log.debug("Self identification end " + commandData.getWorkerId());

		}else if (commandData.isWaterManuallyTurnedOn()){
			//if the water is turned on manually on the worker side
			log.debug("Water turned on manually: " + commandData.getWorkerId());
			//register it to the DB								
			Worker dbw = sql.loadWorkerById(commandData.getWorkerId(), true, false);

			if (dbw != null){ //verify if worker exist 1st.						
				//add the status

				updStatus(dbw, true , commandData, "Water manually turned ON by Worker." );	
			}

		}else if (commandData.isWaterOffFromWorker()){
			//if the water is turned off automatically or manually by the worker.
			log.debug("Water turned Off: " + commandData.getWorkerId());
			//register it to the DB								
			Worker dbw = sql.loadWorkerById(commandData.getWorkerId(), true, false);

			if (dbw != null){ //verify if worker exist 1st.						
				//add the status
				updStatus(dbw, false , commandData, "Water turned off by Worker" );

			}
		}
		else if (commandData.notTurnOnBecauseOfRain()){
			//if the water is turned off automatically or manually by the worker.
			log.debug("Raining, did not turn on: " + commandData.getWorkerId());
			//register it to the DB								
			Worker dbw = sql.loadWorkerById(commandData.getWorkerId(), true, false);

			if (dbw != null){ //verify if worker exist 1st.						
				//add the status
				updStatus(dbw, false , commandData, Constants.onManuallyRain );

				cfg = new ConfigManager().loadConfig();

				if (cfg.isActivateWarning() && cfg.isNotiRainNotWatering()){
					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<body>");
					sb.append("<p> <strong>Worker: </strong>" +  dbw.getWorkId() + " Not Watering because it's raining </p>");
					sb.append("<p> Have a nice Day! </p>");
					sb.append("</body>");
					sb.append("</html>");
					try {
						SendEmail.send("Worker: " + dbw.getWorkId() + " Not Watering.",sb.toString(), cfg);
					} catch (Exception e) {
						log.error("Error sending mail notTurnOnBecauseOfRain: " + e);
					}
				}
			}
		}
		else if (commandData.lowBattery()){
			log.debug("low batt: " + commandData.getWorkerId());
			//register it to the DB								
			Worker dbw = sql.loadWorkerById(commandData.getWorkerId(), true, false);

			if (dbw != null){ //verify if worker exist 1st.						
				//add the status
				updStatus(dbw, false , commandData, "Low Battery. " + commandData.getBatteryPercent() + "%");

				cfg = new ConfigManager().loadConfig();

				if (cfg.isActivateWarning() && cfg.isNotiBattLow()){
					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<body>");
					sb.append("<p> <strong>Worker: </strong>" +  dbw.getWorkId() + " Low Battery </p>");
					sb.append("<p> <strong> Percent remaining: </strong> " + dbw.getStatus().getBatteryLevel());
					sb.append("<p> Next update in 30 minutes if still working.. :) </p>");
					sb.append("</body>");
					sb.append("</html>");
					try {
						SendEmail.send("Worker: " + dbw.getWorkId() + " Low Battery.",sb.toString(), cfg);
					} catch (Exception e) {
						log.error("Error sending mail lowBattery: " + e);;
					}
				}					
			}
		}
		else if (commandData.wakingUp()){
			log.debug("Worker woke up: " + commandData.getWorkerId());
			//register it to the DB								
			Worker dbw = sql.loadWorkerById(commandData.getWorkerId(), true, false);

			if (dbw != null){ //verify if worker exist 1st.						
				//add the status
				commandData.setSleeping(false);
				updStatus(dbw, false , commandData, "Worker out of power save mode" );	
			}

		}

	}
	private static void updStatus(Worker dbw, boolean watering, Cmd commandData, String comment ) throws SQLException, ValidationException, ClassNotFoundException{
		//add the status
		WorkerStatus status = dbw.getStatus();
		status.setConnected(true); //
		status.setRecordedDate(new Date()); //
		status.setWorkerWatering(watering); //
		status.setRainStatus(commandData.getRain());
		status.setLightStatus(commandData.getLight());
		status.setBatteryLevel(commandData.getBatteryPercent());
		status.setSleeping(commandData.isSleeping());
		status.setSystemComment(comment);

		dbw.setStatus(status);
		sql.updateWorker(dbw, true);


	}
	/**
	 * add command retry to map
	 * synchronized to keep threads in check
	 * @param data
	 */
	private static synchronized void addRetries(String data){	
		
		if (!retries.containsKey(data)){		
			retries.put(data, 0);
		}
	}
	
	private static synchronized boolean canRetry(String data){ //TODOOOOOO 
		Integer ret = retries.get(data);
		
		if (ret != null && ret.intValue() < 3){
			log.debug("Retries of command: " + data + " Retry : " + ret);
			ret++;
			retries.put(data, ret);
			return true;
		}else if (ret != null){
			log.debug("remove data: " + data);
			retries.remove(data); //remove it and no more retries.
		}
		
		return false;
		
	}
	private static String getRetryCommand(String data){
		return  data.substring(data.indexOf(":") + 1, data.length());
	}
}
