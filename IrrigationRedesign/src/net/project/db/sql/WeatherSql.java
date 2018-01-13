package net.project.db.sql;

import home.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Constants;
import net.project.db.entities.Weather;
import net.project.db.entities.WeatherWorker;
import net.project.db.entities.Worker;
import net.project.enums.WeatherStatus;

public class WeatherSql {
	
	private static Log log = LogFactory.getLog(WeatherSql.class);
	

	public Weather loadWeatherInformation(boolean loadWorkers) throws SQLException{
		
		Weather weather = null;

		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);

		String query = "SELECT * FROM " + Weather.TABLE_NAME;

		ResultSet rs = con.createQuery(query)
				.getSelectResultSet();

		while (rs.next()) {
			weather = new Weather(rs);	
			
			//load workers outside
			if (loadWorkers){
				
				List<WeatherWorker> ww = new ArrayList<WeatherWorker>();
				
				String workerQuery = "SELECT * FROM " + WeatherWorker.TABLE_NAME + " WHERE " + WeatherWorker.WEATHER_ID + " = :weatherId"; 
				ResultSet rsWeather = con.createQuery(workerQuery).setParameter("weatherId", weather.getId()).getSelectResultSet();
				while (rsWeather.next()) {
					WeatherWorker ws = new WeatherWorker(rsWeather);
					ww.add(ws);
				}
				
				List<Worker> workers = new ArrayList<Worker>();
				
				if (ww.size() > 0){					
					//get the workers
					WorkerSql ws = new WorkerSql();
					for(WeatherWorker w : ww){
						Worker wrk = ws.loadWorkerById(w.getWorkerId(), true, false);
						
						wrk.setWeatherWrk(w);
						
						if (wrk != null){
							workers.add(wrk);
						}
					}
				}			
				
				weather.setWorkers(workers);
			}
		}
		
		
		con.close();
		
		return weather;
	}
	
	public void addWeatherInfo(Weather weather, List<WeatherWorker> weatherWks)  throws SQLException{
		
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);
		
		log.debug("adding weather : " + weather);
		
		int id = con.buildAddQuery(Weather.TABLE_NAME)
		.setParameter(Weather.PROVIDER, weather.getProvider().name())
		.setParameter(Weather.REFRESH, weather.getRefresh())
		.setParameter(Weather.ACTIVE, weather.isActive())
		.setParameter(Weather.LOCATION, weather.getLocation())
		.setParameter(Weather.SEARCHED_CITY, weather.getSearchedCity())
		.setParameter(Weather.TEMP, weather.getCurrTemp())
		.setParameter(Weather.WEATHER_STATUS, weather.getWeatherStatus().name())
		.setParameter(Weather.LAST_UPDATE, weather.getLastUpdated())
		.setParameter(Weather.API_KEY, weather.getApiKey())
		.add();
		
		
		if ( weatherWks != null && weatherWks.size() > 0){
			con.buildAddQuery(WeatherWorker.TABLE_NAME);
			for(WeatherWorker ww : weatherWks){				
				con.setParameter(WeatherWorker.WEATHER_ID,id)
				.setParameter(WeatherWorker.WORKER_ID, ww.getWorkerId())
				.setParameter(WeatherWorker.ACTIVE, ww.isActive());				
			}			
			con.addBatch();
		}
		

		con.close();	
		
	}
	
	public void updateWeather(Weather weather) throws SQLException{
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);
			
		con.buildUpdateQuery(Weather.TABLE_NAME)
		.setParameter(Weather.PROVIDER, weather.getProvider().name())
		.setParameter(Weather.REFRESH, weather.getRefresh())
		.setParameter(Weather.ACTIVE, weather.isActive())
		.setParameter(Weather.LOCATION, weather.getLocation())
		.setParameter(Weather.SEARCHED_CITY, weather.getSearchedCity())
		.setParameter(Weather.WEATHER_STATUS, weather.getWeatherStatus().name())
		.setParameter(Weather.TEMP, weather.getCurrTemp())
		.setParameter(Weather.LAST_UPDATE, weather.getLastUpdated())
		.setParameter(Weather.API_KEY, weather.getApiKey())
		

		.addUpdWhereClause("WHERE " + Weather.ID + " = :workerId", weather.getId())
		.update();

		con.close();	
	}
	
	public List<WeatherWorker> loadAllWeatherWorkers(int weatherId) throws SQLException{
		
		List<WeatherWorker> ww = new ArrayList<WeatherWorker>();
		
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);
		
		String workerQuery = "SELECT * FROM " + WeatherWorker.TABLE_NAME + " WHERE " + WeatherWorker.WEATHER_ID + " = :weatherId"; 
		ResultSet rsWeather = con.createQuery(workerQuery).setParameter("weatherId", weatherId).getSelectResultSet();
		while (rsWeather.next()) {
			WeatherWorker ws = new WeatherWorker(rsWeather);
			ww.add(ws);
		}
		
		con.close();
		
		return ww;
	}
	
	public void addWeatherWorker(WeatherWorker ww) throws SQLException{
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);
		
		con.buildAddQuery(WeatherWorker.TABLE_NAME)
		.setParameter(WeatherWorker.WEATHER_ID, ww.getWeatherId())
		.setParameter(WeatherWorker.WORKER_ID, ww.getWorkerId())
		.setParameter(WeatherWorker.ACTIVE, ww.isActive())
		.add();
		
		con.close();
	}
	
	public void updateWeatherWorker(WeatherWorker ww) throws SQLException{
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);

		con.buildUpdateQuery(WeatherWorker.TABLE_NAME)
		.setParameter(WeatherWorker.WEATHER_ID, ww.getWeatherId())
		.setParameter(WeatherWorker.WORKER_ID, ww.getWorkerId())
		.setParameter(WeatherWorker.ACTIVE, ww.isActive())

		.addUpdWhereClause("WHERE " + WeatherWorker.WORKER_ID + " = :workerId", ww.getWorkerId())
		.update();

		con.close();	
	}
	public void removeWeatherWorkers(List<WeatherWorker> wthWrkList) throws SQLException{
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);

		StringBuilder delete = new StringBuilder("DELETE FROM " + WeatherWorker.TABLE_NAME + " where ( " );
		
		boolean first = true;
		for(int i=0 ; i < wthWrkList.size() ; i ++) {
			
			if(first){
				delete.append( WeatherWorker.WORKER_ID + "  = :id"+i );
				first = false;
			}else{
				delete.append(" OR " + WeatherWorker.WORKER_ID + " = :id"+i );
			}
		}
		delete.append( " ) ");
		
		con.createQuery(delete.toString());
		
		for(int i=0 ; i < wthWrkList.size() ; i ++){
			con.setParameter("id"+i, wthWrkList.get(i).getWorkerId());
		}
		con.delete();

		con.close();
	}
	
	public Weather weatherActive() throws SQLException{
		Weather w = null;
		
		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);
		
		String weatherQuery = "SELECT "+Weather.WEATHER_STATUS + "," + Weather.ACTIVE + " FROM " + Weather.TABLE_NAME; 
		
		ResultSet rsWeather = con.createQuery(weatherQuery).getSelectResultSet();
		
		while (rsWeather.next()) {
			w = new Weather();
			w.setWeatherStatus(WeatherStatus.valueOf(rsWeather.getString(Weather.WEATHER_STATUS)));
			w.setActive(rsWeather.getBoolean(Weather.ACTIVE));
			
		}
		
		con.close();
		
		return w;
	}
	
}
