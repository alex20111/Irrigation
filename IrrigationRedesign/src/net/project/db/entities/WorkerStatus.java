package net.project.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import net.project.enums.BatteryStatus;
import net.project.enums.LightLevel;
import net.project.enums.WeatherStatus;

public class WorkerStatus {
	
	public static final String TABLE_NAME 	= "worker_status";
	public static final String ID 			= "id";
	public static final String WORKER_ON 	= "worker_on";
	public static final String RAIN_STATUS 	= "rain_status";
	public static final String WATER_CONS 	= "water_consumption";
	public static final String BATT_LVL 	= "battery_level";
	public static final String LIGHT_STATUS	= "light_status";
	public static final String REC_DATE 	= "recorded_date";
	public static final String CONNECTED 	= "connected";
	public static final String SYS_COMMENT 	= "system_comment";
	public static final String ERR_DETECTED	= "error_detected";
	public static final String WORKER_SLEEPING	= "worker_power_save";
	public static final String WORKER_ID_FK	= "worker_id_fk";
	
	
	private int id 					= -1;
	private boolean workerWatering 	= false;
	private WeatherStatus rainStatus     = WeatherStatus.NA;
	private int waterConsumption 	= -1;
	private LightLevel lightStatus	= LightLevel.NA;
	private double batteryLevel 	= 0.0;
	private Date recordedDate		= null;
	private boolean connected		= false;
	private String systemComment	= ""; //any comment that the system makes. reasons/things
	private String workerId 		= "000";
	private boolean errorDetected	= false; 
	private boolean sleeping		= false; // if the worker is sleeping in power saving mode.
	
	public WorkerStatus(){}
	public WorkerStatus(ResultSet rs) throws SQLException{
		this.id 				= rs.getInt(ID);
		this.workerWatering 	= rs.getBoolean(WORKER_ON); 
		this.rainStatus 		= WeatherStatus.valueOf(rs.getString(RAIN_STATUS)); 
		this.waterConsumption 	= rs.getInt(WATER_CONS); 
		this.lightStatus		= LightLevel.valueOf(rs.getString(LIGHT_STATUS)); 
		this.batteryLevel 		= rs.getInt(BATT_LVL); 
		this.recordedDate 		= rs.getTimestamp(REC_DATE);
		this.connected 			= rs.getBoolean(CONNECTED); 
		this.systemComment		= rs.getString(SYS_COMMENT);
		this.errorDetected		= rs.getBoolean(ERR_DETECTED);
		this.sleeping			= rs.getBoolean(WORKER_SLEEPING);
		this.workerId 			= rs.getString(WORKER_ID_FK); 
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isWorkerWatering() {
		return workerWatering;
	}
	public void setWorkerWatering(boolean workerWatering) {
		this.workerWatering = workerWatering;
	}
	public int getWaterConsumption() {
		return waterConsumption;
	}
	public void setWaterConsumption(int waterConsumption) {
		this.waterConsumption = waterConsumption;
	}
	public double getBatteryLevel() {
		return batteryLevel;
	}
	public void setBatteryLevel(double batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
	public Date getRecordedDate() {
		return recordedDate;
	}
	public void setRecordedDate(Date recordedDate) {
		this.recordedDate = recordedDate;
	}
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	public String getWorkerId() {
		return workerId;
	}
	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	
	public String getRainText(){			
		return (this.rainStatus != null ? this.rainStatus.getStatus() : "N/A");
	}	
	public boolean isRainingFromWorker(){
		return rainStatus == WeatherStatus.lightRain || rainStatus == WeatherStatus.raining;
	}
	
	public WeatherStatus getRainStatus() {
		return rainStatus;
	}
	public void setRainStatus(WeatherStatus rainStatus) {
		this.rainStatus = rainStatus;
	}
	public LightLevel getLightStatus() {
		return lightStatus;
	}
	public void setLightStatus(LightLevel lightStatus) {
		this.lightStatus = lightStatus;
	}
	public String getSystemComment() {
		return systemComment;
	}
	public void setSystemComment(String systemComment) {
		this.systemComment = systemComment;
	}
	public boolean isErrorDetected() {
		return errorDetected;
	}
	public void setErrorDetected(boolean errorDetected) {
		this.errorDetected = errorDetected;
	}
	
	public BatteryStatus getBatteryStatus() {
		//check if we got some battery level before
		if (batteryLevel > 0){
			if (batteryLevel >= 81){ //full
				return BatteryStatus.full;
			}else if(batteryLevel >= 56 && batteryLevel <= 80){ // 56-80
				return BatteryStatus.threeQuarter;
			}else if(batteryLevel >= 31 && batteryLevel <= 55){ // 31-55
				return BatteryStatus.half;
			}else if(batteryLevel >= 11 && batteryLevel <= 30){ // 11-30
				return BatteryStatus.oneQuarter;
			}else if(batteryLevel >= 0 && batteryLevel <= 10){ // 0-10
				return BatteryStatus.empty;
			}
		}
		return null;		
	}
	
	
	public boolean isSleeping() {
		return sleeping;
	}
	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
	}
	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TABLE_NAME + " (");
		create.append(ID + " INT PRIMARY KEY auto_increment");
		create.append(", " + WORKER_ON + " BOOLEAN" );
		create.append(", " + RAIN_STATUS + " VARCHAR(15)" ); 
		create.append(", " + WATER_CONS + " INT" ); 
		create.append(", " + BATT_LVL + " DOUBLE" ); 
		create.append(", " + LIGHT_STATUS + " VARCHAR(15)" ); 
		create.append(", " + REC_DATE + " TIMESTAMP" );
		create.append(", " + CONNECTED + " BOOLEAN" );
		create.append(", " + SYS_COMMENT + " VARCHAR(200)" );
		create.append(", " + WORKER_ID_FK + " VARCHAR(3)" ); 
		create.append(", " + ERR_DETECTED + " BOOLEAN" );
		create.append(", " + WORKER_SLEEPING + " BOOLEAN" );
		create.append(")");
		
		return create.toString();
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkerStatus [id=");
		builder.append(id);
		builder.append(", Connected=");
		builder.append(connected);
		builder.append(", workerWatering=");
		builder.append(workerWatering);
		builder.append(", Rain status=");
		builder.append(rainStatus);
		builder.append(", waterConsumption=");
		builder.append(waterConsumption);
		builder.append(", Light Status=");
		builder.append(lightStatus);
		builder.append(", batteryLevel=");
		builder.append(batteryLevel);
		builder.append(", recordedDate=");
		builder.append(recordedDate);
		builder.append(", Sys comment=");
		builder.append(systemComment);
		builder.append(", workerId=");
		builder.append(workerId);
		builder.append("]");
		return builder.toString();
	}
	
}
