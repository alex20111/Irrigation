package net.project.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.project.enums.WeatherProvider;
import net.project.enums.WeatherStatus;

/**
 * class responsible to hold the weather information.
 * @author axb161
 *
 */
public class Weather {

	//db fields
	public static final String TABLE_NAME 		= "weather";
	public static final String ID 				= "id";
	public static final String PROVIDER 		= "provider";
	public static final String LOCATION 		= "location";
	public static final String SEARCHED_CITY	= "searched_city";
	public static final String REFRESH 			= "refresh";
	public static final String ACTIVE 			= "active";
	public static final String WEATHER_STATUS	= "weather_status";
	public static final String TEMP				= "current_temp";
	public static final String LAST_UPDATE		= "last_update";
	public static final String API_KEY			= "api_key";

	
	private int id 						= -1;
	private WeatherProvider provider	= WeatherProvider.noSelected;
	private String searchedCity			= ""; //save searched city by the user
	private String location				= "on-118"; // store the user selectedd location (EX: ottawa is 118-on");
	private int refresh	 				= 20;
	private boolean active				= false;
	private WeatherStatus weatherStatus = WeatherStatus.NA;
	private String currTemp				= "";
	private Date lastUpdated;
	private String apiKey				= "";
	
	private List<Worker> workers;	
	
	public Weather(){}
	public Weather(ResultSet rs) throws SQLException{
		this.id 			= rs.getInt(ID); 
		this.provider		= WeatherProvider.valueOf(rs.getString(PROVIDER));
		this.location		= rs.getString(LOCATION);
		this.searchedCity	= rs.getString(SEARCHED_CITY);
		this.refresh 		= rs.getInt(REFRESH);
		this.active			= rs.getBoolean(ACTIVE);
		this.weatherStatus	= WeatherStatus.valueOf(rs.getString(WEATHER_STATUS));
		this.currTemp		= rs.getString(TEMP);
		this.lastUpdated	= rs.getTimestamp(LAST_UPDATE);
		this.apiKey			= rs.getString(API_KEY);

	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public WeatherProvider getProvider() {
		return provider;
	}
	public void setProvider(WeatherProvider provider) {
		this.provider = provider;
	}
	public int getRefresh() {
		return refresh;
	}
	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	
	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TABLE_NAME + " (");
		create.append(ID + " INT PRIMARY KEY auto_increment");
		create.append(", " + PROVIDER + " VARCHAR(30)" );
		create.append(", " + LOCATION + " VARCHAR(30)" );
		create.append(", " + SEARCHED_CITY + " VARCHAR(45)" );
		create.append(", " + REFRESH + " INT" );
		create.append(", " + ACTIVE + " BOOLEAN" );
		create.append(", " + WEATHER_STATUS + " VARCHAR(30)" ); 
		create.append(", " + TEMP + " VARCHAR(10)" );
		create.append(", " + LAST_UPDATE + " TIMESTAMP" );
		create.append(", " + API_KEY + " VARCHAR" );
		
		create.append(")");
		
		return create.toString();
	}
	public List<Worker> getWorkers() {
		return workers;
	}
	public void setWorkers(List<Worker> workers) {
		this.workers = workers;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public WeatherStatus getWeatherStatus() {
		return weatherStatus;
	}
	public void setWeatherStatus(WeatherStatus weatherStatus) {
		this.weatherStatus = weatherStatus;
	}
	public String getCurrTemp() {
		return currTemp;
	}
	public void setCurrTemp(String currTemp) {
		this.currTemp = currTemp;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * set the weather based on the weather provider text.
	 * @param wth
	 */
	public void setWeather(String wth){
		if (wth != null){
			
			if (wth.toLowerCase().contains("drizzle")){
				this.weatherStatus = WeatherStatus.lightDrizzle;
			}
			else if (wth.toLowerCase().contains("light")){
				this.weatherStatus = WeatherStatus.lightRain;
			}else if (wth.toLowerCase().contains("rain")){
				this.weatherStatus = WeatherStatus.raining;
			}else if (wth.toLowerCase().contains("partly cloudy")){
				this.weatherStatus = WeatherStatus.partlyCloudy;								
			}else if (wth.toLowerCase().contains("cloud")){
				this.weatherStatus = WeatherStatus.cloudy;
			}else{
				this.weatherStatus = WeatherStatus.sunny;
			}
		}	
	}
		
	public boolean isRaining(){
		
		if (this.weatherStatus == WeatherStatus.lightRain ||
				this.weatherStatus == WeatherStatus.raining){
			return true;
		}		
		return false;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getSearchedCity() {
		return searchedCity;
	}
	public void setSearchedCity(String searchedCity) {
		this.searchedCity = searchedCity;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Weather [id=");
		builder.append(id);
		builder.append(", provider=");
		builder.append(provider);
		builder.append(", searchedCity=");
		builder.append(searchedCity);
		builder.append(", location=");
		builder.append(location);
		builder.append(", refresh=");
		builder.append(refresh);
		builder.append(", active=");
		builder.append(active);
		builder.append(", weatherStatus=");
		builder.append(weatherStatus);
		builder.append(", currTemp=");
		builder.append(currTemp);
		builder.append(", lastUpdated=");
		builder.append(lastUpdated);
		builder.append(", workers=");
		builder.append(workers);
		builder.append("]");
		return builder.toString();
	}

	
}
