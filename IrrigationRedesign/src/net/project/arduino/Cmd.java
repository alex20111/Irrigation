package net.project.arduino;

import java.util.Calendar;
import net.project.common.Utils;
import net.project.db.entities.Worker;
import net.project.enums.LightLevel;
import net.project.enums.WeatherStatus;
import net.project.exception.ValidationException;

public class Cmd {

	//from controller
	public static String OPEN_WATER 		= "o";  //command to send to open the valve
	public static String CLOST_WATER 		= "a";
	public static String GET_SENSOR_DATA 	= "g";  //command to send to get the status
	public static String SLEEP			 	= "p";  //command to send to put worker to sleep	
	
	//from worker
	public static String SELF_IDENTIFY 		= "i"; 	//code that the worker is self identifying to the controller.
	public static String WATER_MANUAL_OVR	= "m"; 	//code that the worker send that tells the controller that the water has been turned on manually
	public static String WATER_TURNED_OFF	= "w"; 	//code that the worker send that tells the controller that the water has been turned off.
	public static String RAIN_NO_ON			= "r"; 	//code that the worker send that tells the controller that no watering is done since it's raining
	public static String BATT_ALERT			= "b"; 	//code that the worker send that tells the controller that the battery is low
	public static String WOKE_UP			= "d"; 	//code that the worker send that tells the controller that the worker is out of power save mode.
	
	public static String REVC_CONFIRM_CODE 	= "c";	//confirmation from the worker.
	public static String SENDING_CODE 		= "s";	//confirmation from the worker.
	
	//private String msgId	= "";
	private String command 	= "";
	
	//message body
	private String cmdCode 			= "";	
	private String sendOrConfirmCd 	= ""; //send or confirm code
	private String workerId  			= "000";
	private String data 			= "";
	
	private boolean sleeping		= false;
	
	//sensors values
	private LightLevel light 	= LightLevel.NA;
	private WeatherStatus rain	= WeatherStatus.NA;
	private int watering		= -1;
	private double batteryPercent = 0.0;
	
	
	private String fatalError = ""; //if any fatal error occurs, this will be filled out
	
	public Cmd(){}
	/**
	 * 
	 * SEE document
	 * 
	 * !!!!!!!!Old data not good anymore!!!!!!!!!!
	 * receivedData
	 * Pos 0 = Command
	 * Pos 1 = Sending code (always)
	 * Pos 2 = random letter for signal confirmation. To know that you receive the right information(not used when sendring from worker)
	 * Pos 3,4,5 = worker id
	 * Pos 6 = data 
	 * 			1- light
	 * 			2- rain
	 * 			3- watering
	 * 			4- battery percent
	 * 
	 * x = random number
	 * Data is build this way: from controller 
	 * osx133xxx --> data sent to open water --> xxx = number of minutes the water must stay on
	 * ocx133xxx -- confirmation received with sensor data. xxx=Sensor Data
	 * 
	 * asx133 --> data sent to turn off water 
	 * acx133xxx -- confirmation received . xxx= SensorData
	 * 
	 * gsx133 --> request for sensor data
	 * gcx133111 --> data received .. the 1st 1 is the ldr and the 2nd 1 is the rain, Pos- 3 is for watering status(1 on, 0 off) . 4= battery status (0 = 100% , 9 = 90%, 8 = 80%, e=No Reading ).
	 * 
	 * from worker data 
	 * isx122xxx --> data received for self identification from worker
	 * icx122 --> data sent to worker notifying that the identifier was received.
	 * 
	 * msx122xxx --> water turned on manually.
	 * mcx122 --> confirmation sent from the controller to worker.
	 *
	 * wsx122xxx --> water turned off from worker. 
	 *  
	 * (xxx) is the data
	 * @param data
	 * @throws ValidationException 
	 */
	public Cmd(String receivedData) throws ValidationException{	
		
		if(receivedData.length() > 0 ){
		
			String trimmedData = receivedData.trim();
			
			//chech 1st for errors
			validate(trimmedData);
			
			this.cmdCode 		= trimmedData.substring(0, 1);
			this.sendOrConfirmCd 	= trimmedData.substring(1, 2);
			this.workerId  		= trimmedData.substring(2, 5);
			this.data 			= trimmedData.substring(5, trimmedData.length());
			
			if (this.data != null && this.data.length() > 0 ){
				int light = Integer.parseInt(this.data.substring(0,1));
				int rain = Integer.parseInt(this.data.substring(1,2));
				this.watering = Integer.parseInt(this.data.substring(2,3));				
				
				if (light == 0){
					this.light = LightLevel.Dark;
				}else if (light >= 1 && light <= 3){
					this.light = LightLevel.LowLight;
				}else if (light >= 4 && light <= 6){
					this.light = LightLevel.MediumLight;
				}else if (light >= 7){
					this.light = LightLevel.Sunny;
				}
				
				if (rain == 2){
					this.rain = WeatherStatus.sunny;					
				}else if (rain == 1){
					this.rain = WeatherStatus.lightRain;
				}else if (rain == 0){
					this.rain = WeatherStatus.raining;
				}
				
				try{
					String battPercent = this.data.substring(3,5);
					
					if (("n").equals(battPercent)){
						batteryPercent = -1.1;
					}else{						
						batteryPercent = Double.parseDouble(battPercent); 
					}
				}catch(StringIndexOutOfBoundsException e){
					batteryPercent = -1.0;
				}
			}	
			
		}
		else{
			throw new ValidationException("No data");
		}
	}
	/**
	 * Pos 0 = Command
	 * Pos 1 = Sending code (always)
	 * Pos 2,3,4 = worker id
	 * Pos 5,6,7 (only for open water) number of minutes the water must stay on up to 999

	 * @param action
	 * @param workerId
	 * @throws ValidationException
	 */
	public void buildCommand(String action, Worker worker) throws ValidationException{
		if (action != null && action.length() > 0 && worker != null && worker.getWorkId().length() > 0){

			if (action == OPEN_WATER){
				this.command = action + SENDING_CODE + worker.getWorkId() + Utils.addIntLeftZeroPadding(worker.getWaterElapseTime());
			}else if (action == SLEEP){
				int wakeUpAt = worker.getStopSleepTime() - 5; //remove 5 min

				int h = wakeUpAt / 60;
				int m = wakeUpAt % 60;

				String time =  (h < 10 ? "0" + h : h) + "" +  (m < 10 ? "0" + m : m);			

				this.command = action + SENDING_CODE + worker.getWorkId() + time;
			}else if(action == SELF_IDENTIFY){
				Calendar cal = Calendar.getInstance();				
				StringBuilder date = new StringBuilder (String.valueOf(cal.get(Calendar.YEAR)).substring(2,4));
				date.append( (cal.get(Calendar.MONTH) + 1) < 10 ? "0" + String.valueOf(cal.get(Calendar.MONTH) + 1) : String.valueOf(cal.get(Calendar.MONTH) + 1) ) ;
				date.append( cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) : String.valueOf(cal.get(Calendar.DAY_OF_MONTH) ) ) ;
				date.append( cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) : String.valueOf(cal.get(Calendar.HOUR_OF_DAY) ) ) ;
				date.append( cal.get(Calendar.MINUTE) < 10 ? "0" + String.valueOf(cal.get(Calendar.MINUTE)) : String.valueOf(cal.get(Calendar.MINUTE) )  );
				date.append( cal.get(Calendar.SECOND) < 10 ? "0" + String.valueOf(cal.get(Calendar.SECOND)) : String.valueOf(cal.get(Calendar.SECOND) ) ) ;

				this.command = action + SENDING_CODE + worker.getWorkId() + date.toString();

			}else{
				this.command = action + SENDING_CODE +  worker.getWorkId();
			}			

		}else
		{
			throw new ValidationException("One value is null. Action: " + action + " workerId: " + workerId);
		}		
	}
	
	
	public String getCmdCode() {
		return cmdCode;
	}
	public void setCmdCode(String cmdCode) {
		this.cmdCode = cmdCode;
	}
	public String getWorkerId() {
		return workerId;
	}
	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public boolean isConfirmation(){
		return (this.sendOrConfirmCd.equals(REVC_CONFIRM_CODE) ? true : false);
	}	
	public boolean isSelfIdentification(){
		return (cmdCode.equals(SELF_IDENTIFY) ? true : false);
	}
	public boolean isWaterManuallyTurnedOn(){
		if (cmdCode.equals(WATER_MANUAL_OVR) && sendOrConfirmCd.equals(SENDING_CODE))
		{	
			return true;
		}
		return false;
	}
	public boolean isWaterOffFromWorker(){
		if (cmdCode.equals(WATER_TURNED_OFF) && sendOrConfirmCd.equals(SENDING_CODE))
		{	
			return true;
		}
		return false;
	}	
	public boolean isWaterOff(){
		return (cmdCode.equals(CLOST_WATER) ? true : false);
	}	
	
	public boolean notTurnOnBecauseOfRain(){
		return (cmdCode.equals(RAIN_NO_ON) && sendOrConfirmCd.equals(SENDING_CODE) ? true : false);
	}
	
	public boolean lowBattery(){
		return (cmdCode.equals(BATT_ALERT) && sendOrConfirmCd.equals(SENDING_CODE) ? true : false);
	}
	public boolean wakingUp(){
		return (cmdCode.equals(WOKE_UP) && sendOrConfirmCd.equals(SENDING_CODE) ? true : false);
	}
	
	public LightLevel getLight(){
		return this.light;
	}	
	public boolean isRaining(){
		if (this.rain != null && this.rain == WeatherStatus.raining)
		{	
			return true;
		}
		return false;
	}
	public void setRain(WeatherStatus rain){
		this.rain = rain;
	}
	public WeatherStatus getRain(){
		return this.rain;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}

	public String getSendOrConfirmCd() {
		return sendOrConfirmCd;
	}
	public void setSendOrConfirmCd(String sendOrConfirmCd) {
		this.sendOrConfirmCd = sendOrConfirmCd;
	}
	/**
	 * verify for errors.
	 * @param msg
	 * @throws ValidationException
	 */
	private void validate(String msg) throws ValidationException{
		if ("failed".equals(msg)){
			throw new ValidationException("Transmission failed");
		}
			
	}
	
	public int getWatering() {
		return watering;
	}
	public double getBatteryPercent() {
		return batteryPercent;
	}
	public void setBatteryPercent(double batteryPercent) {
		this.batteryPercent = batteryPercent;
	}
	public String getFatalError() {
		return fatalError;
	}
	public void setFatalError(String fatalError) {
		this.fatalError = fatalError;
	}
	public boolean isSleeping() {
		return sleeping;
	}
	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Cmd [msgId=");
//		builder.append(msgId);
		builder.append(", command=");
		builder.append(command);
		builder.append(", confChar=");
//		builder.append(confChar);
		builder.append(", cmdCode=");
		builder.append(cmdCode);
		builder.append(", sendOrConfirmCd=");
		builder.append(sendOrConfirmCd);
		builder.append(", workerId=");
		builder.append(workerId);
		builder.append(", data=");
		builder.append(data);
		builder.append(", light=");
		builder.append(light);
		builder.append(", rain=");
		builder.append(rain);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}

