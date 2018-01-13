package net.project.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class responsible to verify if the worker react to weather events.
 * COMPOSITE KEY
 * @author 
 *
 */
public class WeatherWorker {

	public static final String TABLE_NAME 		= "weather_worker";
	public static final String WEATHER_ID		= "weather_id";
	public static final String WORKER_ID		= "worker_id";
	public static final String ACTIVE 			= "active";
	
	
	private int weatherId				= -1;
	private String workerId				= "";
	private boolean active				= false;
	
	
	public WeatherWorker(){}
	public WeatherWorker(ResultSet rs) throws SQLException{
		this.weatherId 		= rs.getInt(WEATHER_ID); 
		this.workerId		= rs.getString(WORKER_ID); 
		this.active 		= rs.getBoolean(ACTIVE);

	}
	public int getWeatherId() {
		return weatherId;
	}
	public void setWeatherId(int weatherId) {
		this.weatherId = weatherId;
	}
	public String getWorkerId() {
		return workerId;
	}
	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TABLE_NAME + " (");
		
		create.append(WEATHER_ID + " INT ");
		create.append(", " + WORKER_ID + " VARCHAR(3)" );
		create.append(", " + ACTIVE + " BOOLEAN " );
		create.append(", PRIMARY KEY( " + WEATHER_ID + " , " + WORKER_ID + ") " ); 
		create.append(" ) ");
		
		return create.toString();
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WeatherWorker [weatherId=");
		builder.append(weatherId);
		builder.append(", workerId=");
		builder.append(workerId);
		builder.append(", active=");
		builder.append(active);
		builder.append("]");
		return builder.toString();
	}

	
	
	
}
