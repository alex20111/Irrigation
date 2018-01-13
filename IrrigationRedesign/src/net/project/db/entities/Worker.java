package net.project.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import net.project.enums.SchedType;

public class Worker {

	//db fields
	public static final String TABLE_NAME 		= "worker";
	public static final String WORKER_ID 		= "worker_id";
	public static final String NAME 			= "name";
	public static final String DESCRIPTION 		= "description";
	public static final String SCHED_TYPE 		= "sched_type";
	public static final String START_TIME 		= "start_time";
	public static final String ELAPSE_TIME 		= "elapse_time";
	public static final String DO_NOT_WATER 	= "do_not_water";
	public static final String MANAGED 			= "managed";
	public static final String SCHEDULE			= "schedule_running";
	public static final String NEXT_WATERING	= "next_sched_watering";
	public static final String START_SLEEP_TIME	= "s_sleep_time";
	public static final String STOP_SLEEP_TIME	= "e_sleep_time";
	
	//
	private String workId 				= "000";
	private String name 			= "";
	private String description 		= "";
	private SchedType schedType;
	private String schedStartTime 	= "";
	private Date nextWateringBySched;
	private int waterElapseTime 	= 5;
	private boolean doNotWater 		= false;
	private boolean managed			= false;
	private boolean scheduleRunning = false;
	private int startSleepTime	= -1;
	private int stopSleepTime	= -1;
	
	//transient
	private WorkerStatus status;
	
	private WeatherWorker weatherWrk;
	
	//html result body
	private String resultBody = ""; 	
	
	public Worker(){}
	public Worker(ResultSet rs) throws SQLException{
		this.workId 		= rs.getString(WORKER_ID); 
		this.name 			= rs.getString(NAME);
		this.description 	= rs.getString(DESCRIPTION);
		this.schedType 		= SchedType.valueOf(rs.getString(SCHED_TYPE));
		this.schedStartTime = rs.getString(START_TIME);
		this.waterElapseTime= rs.getInt(ELAPSE_TIME);
		this.doNotWater 	= rs.getBoolean(DO_NOT_WATER);
		this.managed 		= rs.getBoolean(MANAGED);
		this.scheduleRunning= rs.getBoolean(SCHEDULE);
		this.nextWateringBySched= rs.getTimestamp(NEXT_WATERING);
		this.startSleepTime	= rs.getInt(START_SLEEP_TIME);
		this.stopSleepTime	= rs.getInt(STOP_SLEEP_TIME);
	}
	
	
	public String getWorkId() {
		return workId;
	}
	public void setWorkId(String workId) {
		this.workId = workId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public SchedType getSchedType() {
		return schedType;
	}
	public void setSchedType(SchedType schedType) {
		this.schedType = schedType;
	}
	public String getSchedStartTime() {
		return schedStartTime;
	}
	public void setSchedStartTime(String schedStartTime) {
		this.schedStartTime = schedStartTime;
	}
	public int getWaterElapseTime() {
		return waterElapseTime;
	}
	public void setWaterElapseTime(int waterElapseTime) {
		this.waterElapseTime = waterElapseTime;
	}
	public boolean isDoNotWater() {
		return doNotWater;
	}
	public void setDoNotWater(boolean doNotWater) {
		this.doNotWater = doNotWater;
	}
	
	public boolean isManaged() {
		return managed;
	}
	public void setManaged(boolean managed) {
		this.managed = managed;
	}
	
	public WorkerStatus getStatus() {
		return status;
	}
	public void setStatus(WorkerStatus status) {
		this.status = status;
	}
	public boolean isScheduleRunning() {
		return scheduleRunning;
	}
	public void setScheduleRunning(boolean scheduleRunning) {
		this.scheduleRunning = scheduleRunning;
	}
	public Date getNextWateringBySched() {
		return nextWateringBySched;
	}
	public void setNextWateringBySched(Date nextWateringBySched) {
		this.nextWateringBySched = nextWateringBySched;
	}
	public String getResultBody() {
		return resultBody;
	}
	public void setResultBody(String resultBody) {
		this.resultBody = resultBody;
	}
	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TABLE_NAME + " (");
		create.append(WORKER_ID + " VARCHAR(3) PRIMARY KEY");
		create.append(", " + NAME + " VARCHAR(200)" );
		create.append(", " + DESCRIPTION + " VARCHAR(3000)" ); 
		create.append(", " + SCHED_TYPE + " VARCHAR(200)" ); 
		create.append(", " + START_TIME + " VARCHAR(20)" ); 
		create.append(", " + ELAPSE_TIME + " INT" ); 
		create.append(", " + DO_NOT_WATER + " BOOLEAN" );
		create.append(", " + MANAGED + " BOOLEAN" );
		create.append(", " + SCHEDULE + " BOOLEAN" );
		create.append(", " + NEXT_WATERING + " TIMESTAMP" );
		create.append(", " + START_SLEEP_TIME + " INT" );
		create.append(", " + STOP_SLEEP_TIME + " INT" );
		
		create.append(")");
		
		return create.toString();
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Worker [");
		builder.append("workId=");
		builder.append(workId);
		builder.append(", name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", schedType=");
		builder.append(schedType);
		builder.append(", schedStartTime=");
		builder.append(schedStartTime);
		builder.append(", waterElapseTime=");
		builder.append(waterElapseTime);
		builder.append(", doNotWater=");
		builder.append(doNotWater);
		builder.append(", Managed=");
		builder.append(managed);
		builder.append(", Schedule Running=");
		builder.append(scheduleRunning);
		builder.append(", nextWateringBySched=");
		builder.append(nextWateringBySched);
		builder.append("]");
		return builder.toString();
	}
	public WeatherWorker getWeatherWrk() {
		return weatherWrk;
	}
	public void setWeatherWrk(WeatherWorker weatherWrk) {
		this.weatherWrk =weatherWrk;
	}
	public int getStartSleepTime() {
		return startSleepTime;
	}
	public void setStartSleepTime(int startSleepTime) {
		this.startSleepTime = startSleepTime;
	}
	public int getStopSleepTime() {
		return stopSleepTime;
	}
	public void setStopSleepTime(int stopSleepTime) {
		this.stopSleepTime = stopSleepTime;
	}

}
